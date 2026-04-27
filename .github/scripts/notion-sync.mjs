#!/usr/bin/env node

import fs from 'node:fs';

const REQUIRED_ENVS = ['NOTION_TOKEN', 'NOTION_DATABASE_ID', 'GITHUB_EVENT_PATH', 'GITHUB_EVENT_NAME'];
for (const envName of REQUIRED_ENVS) {
  if (!process.env[envName]) {
    throw new Error(`Missing required environment variable: ${envName}`);
  }
}

const NOTION_TOKEN = process.env.NOTION_TOKEN;
const NOTION_DATABASE_ID = normalizeNotionId(process.env.NOTION_DATABASE_ID, 'NOTION_DATABASE_ID');
const NOTION_VERSION = process.env.NOTION_VERSION || '2022-06-28';

const PROPS = {
  issueNumber: process.env.NOTION_PROP_ISSUE_NUMBER || '이슈 번호',
  title: process.env.NOTION_PROP_TITLE || '제목',
  status: process.env.NOTION_PROP_STATUS || '상태',
  issueUrl: process.env.NOTION_PROP_ISSUE_URL || '이슈 URL',
  prUrl: process.env.NOTION_PROP_PR_URL || 'PR URL',
  summary: process.env.NOTION_PROP_SUMMARY || '요약',
  assignee: process.env.NOTION_PROP_ASSIGNEE || '담당자',
  assigneeText: process.env.NOTION_PROP_ASSIGNEE_TEXT || '담당자(텍스트)',
  dueDate: process.env.NOTION_PROP_DUE_DATE || '마감일',
};

const STATUS = {
  todo: process.env.NOTION_STATUS_TODO || '시작전',
  inProgress: process.env.NOTION_STATUS_IN_PROGRESS || '진행중',
  inReview: process.env.NOTION_STATUS_IN_REVIEW || '리뷰중',
  done: process.env.NOTION_STATUS_DONE || '완료',
  canceled: process.env.NOTION_STATUS_CANCELED || '취소됨',
};

const PEOPLE_MAP = parsePeopleMap(process.env.NOTION_PEOPLE_MAP);
const ISSUE_SYNC_ACTIONS = ['opened', 'edited', 'reopened', 'closed', 'assigned', 'unassigned'];
const PR_SYNC_ACTIONS = ['opened', 'reopened', 'ready_for_review', 'closed'];
const ISSUE_NUMBER_PROPERTY_CANDIDATES = [
  PROPS.issueNumber,
  'Github 이슈 번호',
  'GitHub 이슈 번호',
  '이슈 번호',
  'Issue Number',
].filter(Boolean);
const MAX_TEXT = 1800;

function normalizeNotionId(rawValue, envName) {
  const value = String(rawValue || '').trim();
  if (!value) {
    throw new Error(`${envName} is empty`);
  }

  // Accept either raw UUID (with/without dashes) or copied Notion URL containing the ID.
  const matched = value.match(/[0-9a-fA-F]{32}|[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/);
  if (!matched) {
    throw new Error(`${envName} must contain a valid Notion ID (received: "${value}")`);
  }

  return matched[0].replace(/-/g, '');
}

function parsePeopleMap(rawValue) {
  if (!rawValue) {
    return {};
  }

  try {
    const parsed = JSON.parse(rawValue);
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) {
      throw new Error('NOTION_PEOPLE_MAP must be a JSON object');
    }

    const normalized = {};
    for (const [githubLogin, notionUserId] of Object.entries(parsed)) {
      if (typeof githubLogin !== 'string' || typeof notionUserId !== 'string') {
        continue;
      }

      const login = githubLogin.trim();
      const userId = notionUserId.trim();
      if (!login || !userId) {
        continue;
      }
      normalized[login] = userId;
    }

    return normalized;
  } catch (error) {
    console.warn(`Invalid NOTION_PEOPLE_MAP: ${error.message}`);
    return {};
  }
}

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

function toDate(dateString) {
  if (!dateString) {
    return { date: null };
  }
  return { date: { start: dateString } };
}

function isValidDateString(dateString) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(dateString)) {
    return false;
  }

  const [year, month, day] = dateString.split('-').map((value) => Number.parseInt(value, 10));
  const utcDate = new Date(Date.UTC(year, month - 1, day));

  return utcDate.getUTCFullYear() === year &&
    utcDate.getUTCMonth() + 1 === month &&
    utcDate.getUTCDate() === day;
}

function parseDueDateFromIssueBody(body) {
  if (!body) {
    return null;
  }

  const labeledMatch = body.match(/마감일\s*\(YYYY-MM-DD\)[^\n]*\n+([0-9]{4}-[0-9]{2}-[0-9]{2})/i);
  const genericMatch = body.match(/\b([0-9]{4}-[0-9]{2}-[0-9]{2})\b/);
  const candidate = labeledMatch?.[1] || genericMatch?.[1] || null;

  if (!candidate || !isValidDateString(candidate)) {
    return null;
  }
  return candidate;
}

function getAssigneeLogins(assignees) {
  if (!Array.isArray(assignees)) {
    return [];
  }

  return [...new Set(
    assignees
      .map((assignee) => assignee?.login)
      .filter((login) => typeof login === 'string' && login.trim())
      .map((login) => login.trim()),
  )];
}

function buildAssigneeProperties(assignees) {
  const logins = getAssigneeLogins(assignees);
  const mappedIds = [];
  const unmappedLogins = [];

  for (const login of logins) {
    const notionUserId = PEOPLE_MAP[login];
    if (notionUserId) {
      mappedIds.push(notionUserId);
    } else {
      unmappedLogins.push(login);
    }
  }

  if (unmappedLogins.length > 0) {
    console.log(`Unmapped GitHub assignees: ${unmappedLogins.join(', ')}`);
  }

  const uniqueMappedIds = [...new Set(mappedIds)];
  return {
    [PROPS.assignee]: {
      people: uniqueMappedIds.map((id) => ({ id })),
    },
    [PROPS.assigneeText]: toRichText(unmappedLogins.join(', ')),
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
  let lastError = null;

  for (const propertyName of [...new Set(ISSUE_NUMBER_PROPERTY_CANDIDATES)]) {
    try {
      const result = await notionRequest(`/databases/${NOTION_DATABASE_ID}/query`, 'POST', {
        filter: {
          property: propertyName,
          number: { equals: issueNumber },
        },
        page_size: 1,
      });

      if (PROPS.issueNumber !== propertyName) {
        console.log(`Using Notion issue number property: ${propertyName}`);
        PROPS.issueNumber = propertyName;
      }

      return result.results?.[0] || null;
    } catch (error) {
      if (String(error?.message || '').includes('Could not find property with name or id')) {
        lastError = error;
        continue;
      }
      throw error;
    }
  }

  throw lastError || new Error(`Cannot find issue number property in Notion DB. Tried: ${ISSUE_NUMBER_PROPERTY_CANDIDATES.join(', ')}`);
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
  if (!repositoryUrl || !Number.isInteger(issueNumber) || issueNumber <= 0) {
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
  if (fromBody !== null) {
    return fromBody;
  }

  const fromTitle = extractIssueNumberFromText(pr.title);
  if (fromTitle !== null) {
    return fromTitle;
  }

  const fromBranch = extractIssueNumberFromBranch(pr.head?.ref);
  if (fromBranch !== null) {
    return fromBranch;
  }

  return null;
}

function isValidIssueNumber(issueNumber) {
  return Number.isInteger(issueNumber) && issueNumber > 0;
}

function determineStatusForIssueEvent(action, issue) {
  if (action === 'opened' || action === 'reopened') {
    return STATUS.todo;
  }

  if (action === 'closed' && issue?.state_reason === 'not_planned') {
    return STATUS.canceled;
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

  const dueDate = parseDueDateFromIssueBody(issue.body);
  const status = determineStatusForIssueEvent(payload.action, issue);
  const assigneeProperties = buildAssigneeProperties(issue.assignees);

  const properties = {
    [PROPS.issueNumber]: { number: issue.number },
    [PROPS.title]: toTitle(issue.title),
    [PROPS.issueUrl]: { url: issue.html_url },
    [PROPS.summary]: toRichText(buildIssueSummary(issue)),
    [PROPS.dueDate]: toDate(dueDate),
    ...assigneeProperties,
  };

  if (status) {
    properties[PROPS.status] = { select: { name: status } };
  }

  const createDefaults = {
    [PROPS.issueNumber]: { number: issue.number },
    [PROPS.title]: toTitle(issue.title),
    [PROPS.status]: { select: { name: status || STATUS.todo } },
    [PROPS.issueUrl]: { url: issue.html_url },
    [PROPS.summary]: toRichText(buildIssueSummary(issue)),
    [PROPS.dueDate]: toDate(dueDate),
    ...assigneeProperties,
  };

  await upsertByIssueNumber(issue.number, properties, createDefaults);
}

async function handlePullRequestEvent(payload) {
  const pr = payload.pull_request;
  if (!pr) {
    throw new Error('Missing pull_request payload');
  }

  const issueNumber = resolveIssueNumberFromPr(pr);
  if (!isValidIssueNumber(issueNumber)) {
    throw new Error('Cannot resolve valid issue number (>0) from PR body/title/branch. Add "Fixes #<number>" to the PR body.');
  }

  const status = determineStatusForPrEvent(payload.action, pr);
  const repositoryUrl = payload.repository?.html_url ||
    (process.env.GITHUB_SERVER_URL && process.env.GITHUB_REPOSITORY
      ? `${process.env.GITHUB_SERVER_URL}/${process.env.GITHUB_REPOSITORY}`
      : null);

  const issueUrl = normalizeIssueUrl(repositoryUrl, issueNumber);
  const assigneeProperties = buildAssigneeProperties(pr.assignees);

  const properties = {
    [PROPS.issueNumber]: { number: issueNumber },
    [PROPS.title]: toTitle(pr.title),
    [PROPS.prUrl]: { url: pr.html_url },
    [PROPS.summary]: toRichText(buildPrSummary(pr)),
    ...assigneeProperties,
  };

  if (status) {
    properties[PROPS.status] = { select: { name: status } };
  }

  if (issueUrl) {
    properties[PROPS.issueUrl] = { url: issueUrl };
  }

  const createDefaults = {
    [PROPS.issueNumber]: { number: issueNumber },
    [PROPS.title]: toTitle(pr.title),
    [PROPS.status]: { select: { name: status || STATUS.inProgress } },
    [PROPS.prUrl]: { url: pr.html_url },
    [PROPS.summary]: toRichText(buildPrSummary(pr)),
    ...assigneeProperties,
  };

  if (issueUrl) {
    createDefaults[PROPS.issueUrl] = { url: issueUrl };
  }

  await upsertByIssueNumber(issueNumber, properties, createDefaults);
}

async function main() {
  const payload = JSON.parse(fs.readFileSync(process.env.GITHUB_EVENT_PATH, 'utf8'));
  const eventName = process.env.GITHUB_EVENT_NAME;

  if (eventName === 'issues' && ISSUE_SYNC_ACTIONS.includes(payload.action)) {
    await handleIssueEvent(payload);
    return;
  }

  if (eventName === 'pull_request' && PR_SYNC_ACTIONS.includes(payload.action)) {
    await handlePullRequestEvent(payload);
    return;
  }

  console.log(`No-op for event: ${eventName}, action: ${payload.action}`);
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
