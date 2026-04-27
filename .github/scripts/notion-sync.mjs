#!/usr/bin/env node

import fs from 'node:fs';

const REQUIRED_ENVS = ['NOTION_TOKEN', 'NOTION_DATABASE_ID', 'GITHUB_EVENT_PATH', 'GITHUB_EVENT_NAME'];
for (const envName of REQUIRED_ENVS) {
  if (!process.env[envName]) {
    throw new Error(`Missing required environment variable: ${envName}`);
  }
}

const NOTION_TOKEN = process.env.NOTION_TOKEN;
const NOTION_DATABASE_ID = process.env.NOTION_DATABASE_ID;
const NOTION_VERSION = process.env.NOTION_VERSION || '2022-06-28';

const PROPS = {
  issueNumber: process.env.NOTION_PROP_ISSUE_NUMBER || '이슈 번호',
  title: process.env.NOTION_PROP_TITLE || '제목',
  status: process.env.NOTION_PROP_STATUS || '상태',
  issueUrl: process.env.NOTION_PROP_ISSUE_URL || '이슈 URL',
  prUrl: process.env.NOTION_PROP_PR_URL || 'PR URL',
  summary: process.env.NOTION_PROP_SUMMARY || '요약',
};

const STATUS = {
  todo: process.env.NOTION_STATUS_TODO || '할 일',
  inProgress: process.env.NOTION_STATUS_IN_PROGRESS || '진행 중',
  inReview: process.env.NOTION_STATUS_IN_REVIEW || '리뷰 중',
  done: process.env.NOTION_STATUS_DONE || '완료',
};

const MAX_TEXT = 1800;

function truncate(value, maxLength = MAX_TEXT) {
  if (!value) {
    return '';
  }
  const normalized = String(value).replace(/\r/g, '').trim();
  if (normalized.length <= maxLength) {
    return normalized;
  }
  return `${normalized.slice(0, maxLength - 3)}...`;
}

function toTitle(content) {
  return {
    title: [{ text: { content: truncate(content, 200) || '제목 없음' } }],
  };
}

function toRichText(content) {
  const trimmed = truncate(content);
  if (!trimmed) {
    return { rich_text: [] };
  }
  return {
    rich_text: [{ text: { content: trimmed } }],
  };
}

async function notionRequest(path, method, body) {
  const response = await fetch(`https://api.notion.com/v1${path}`, {
    method,
    headers: {
      Authorization: `Bearer ${NOTION_TOKEN}`,
      'Notion-Version': NOTION_VERSION,
      'Content-Type': 'application/json',
    },
    body: body ? JSON.stringify(body) : undefined,
  });

  const raw = await response.text();
  let data;
  try {
    data = raw ? JSON.parse(raw) : {};
  } catch {
    data = { raw };
  }

  if (!response.ok) {
    const message = data?.message || raw || 'Unknown Notion API error';
    throw new Error(`Notion API ${method} ${path} failed (${response.status}): ${message}`);
  }

  return data;
}

async function findPageByIssueNumber(issueNumber) {
  const result = await notionRequest(`/databases/${NOTION_DATABASE_ID}/query`, 'POST', {
    filter: {
      property: PROPS.issueNumber,
      number: { equals: issueNumber },
    },
    page_size: 1,
  });

  return result.results?.[0] || null;
}

async function createPage(properties) {
  return notionRequest('/pages', 'POST', {
    parent: { database_id: NOTION_DATABASE_ID },
    properties,
  });
}

async function updatePage(pageId, properties) {
  return notionRequest(`/pages/${pageId}`, 'PATCH', { properties });
}

function normalizeIssueUrl(repositoryUrl, issueNumber) {
  if (!repositoryUrl || !issueNumber) {
    return null;
  }
  return `${repositoryUrl}/issues/${issueNumber}`;
}

function extractIssueNumberFromText(text) {
  if (!text) {
    return null;
  }

  const closingKeywordMatch = text.match(/(?:close[sd]?|fix(?:e[sd])?|resolve[sd]?)\s+#(\d+)/i);
  if (closingKeywordMatch?.[1]) {
    return Number.parseInt(closingKeywordMatch[1], 10);
  }

  const genericHashMatch = text.match(/#(\d+)/);
  if (genericHashMatch?.[1]) {
    return Number.parseInt(genericHashMatch[1], 10);
  }

  return null;
}

function extractIssueNumberFromBranch(branchName) {
  if (!branchName) {
    return null;
  }

  const match = branchName.match(/(?:^|\/)(\d+)(?:[-_/]|$)/);
  if (!match?.[1]) {
    return null;
  }
  return Number.parseInt(match[1], 10);
}

function resolveIssueNumberFromPr(pr) {
  const fromBody = extractIssueNumberFromText(pr.body);
  if (fromBody) {
    return fromBody;
  }

  const fromTitle = extractIssueNumberFromText(pr.title);
  if (fromTitle) {
    return fromTitle;
  }

  const fromBranch = extractIssueNumberFromBranch(pr.head?.ref);
  if (fromBranch) {
    return fromBranch;
  }

  return null;
}

function determineStatusForPrEvent(action, pr) {
  if (action === 'ready_for_review') {
    return STATUS.inReview;
  }

  if (action === 'opened' || action === 'reopened') {
    return pr.draft ? STATUS.inProgress : STATUS.inReview;
  }

  if (action === 'closed') {
    return pr.merged ? STATUS.done : STATUS.inProgress;
  }

  return null;
}

function buildIssueSummary(issue) {
  if (!issue.body) {
    return `GitHub Issue #${issue.number}`;
  }
  return issue.body;
}

function buildPrSummary(pr) {
  const title = pr.title ? `PR: ${pr.title}` : 'PR 업데이트';
  const body = pr.body ? `\n\n${pr.body}` : '';
  return `${title}${body}`;
}

async function upsertByIssueNumber(issueNumber, properties, createDefaults = {}) {
  const existing = await findPageByIssueNumber(issueNumber);
  if (existing) {
    await updatePage(existing.id, properties);
    console.log(`Updated Notion page for issue #${issueNumber}: ${existing.id}`);
    return;
  }

  await createPage({
    ...createDefaults,
    ...properties,
  });
  console.log(`Created Notion page for issue #${issueNumber}`);
}

async function handleIssueEvent(payload) {
  const issue = payload.issue;
  if (!issue) {
    throw new Error('Missing issue payload');
  }

  const properties = {
    [PROPS.issueNumber]: { number: issue.number },
    [PROPS.title]: toTitle(issue.title),
    [PROPS.status]: { select: { name: STATUS.todo } },
    [PROPS.issueUrl]: { url: issue.html_url },
    [PROPS.summary]: toRichText(buildIssueSummary(issue)),
  };

  await upsertByIssueNumber(issue.number, properties);
}

async function handlePullRequestEvent(payload) {
  const pr = payload.pull_request;
  if (!pr) {
    throw new Error('Missing pull_request payload');
  }

  const issueNumber = resolveIssueNumberFromPr(pr);
  if (!issueNumber) {
    throw new Error('Cannot resolve issue number from PR body/title/branch. Add "Fixes #<number>" to the PR body.');
  }

  const status = determineStatusForPrEvent(payload.action, pr);
  const repositoryUrl = payload.repository?.html_url ||
    (process.env.GITHUB_SERVER_URL && process.env.GITHUB_REPOSITORY
      ? `${process.env.GITHUB_SERVER_URL}/${process.env.GITHUB_REPOSITORY}`
      : null);

  const properties = {
    [PROPS.issueNumber]: { number: issueNumber },
    [PROPS.title]: toTitle(pr.title),
    [PROPS.prUrl]: { url: pr.html_url },
    [PROPS.summary]: toRichText(buildPrSummary(pr)),
  };

  if (status) {
    properties[PROPS.status] = { select: { name: status } };
  }

  const issueUrl = normalizeIssueUrl(repositoryUrl, issueNumber);
  if (issueUrl) {
    properties[PROPS.issueUrl] = { url: issueUrl };
  }

  const createDefaults = {
    [PROPS.issueNumber]: { number: issueNumber },
    [PROPS.title]: toTitle(pr.title),
    [PROPS.status]: { select: { name: status || STATUS.inProgress } },
    [PROPS.issueUrl]: { url: issueUrl },
    [PROPS.prUrl]: { url: pr.html_url },
    [PROPS.summary]: toRichText(buildPrSummary(pr)),
  };

  await upsertByIssueNumber(issueNumber, properties, createDefaults);
}

async function main() {
  const payload = JSON.parse(fs.readFileSync(process.env.GITHUB_EVENT_PATH, 'utf8'));
  const eventName = process.env.GITHUB_EVENT_NAME;

  if (eventName === 'issues' && payload.action === 'opened') {
    await handleIssueEvent(payload);
    return;
  }

  if (eventName === 'pull_request' && ['opened', 'reopened', 'ready_for_review', 'closed'].includes(payload.action)) {
    await handlePullRequestEvent(payload);
    return;
  }

  console.log(`No-op for event: ${eventName}, action: ${payload.action}`);
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
