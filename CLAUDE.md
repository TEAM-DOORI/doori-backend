# CLAUDE.md

This file defines the minimum working contract for Claude Code in this repo.

## 우선순위 규칙

Claude는 아래 순서대로 규칙을 따른다.

1. `.claude/rules/commit.md`
2. `.claude/rules/pr.md`
3. `.claude/rules/code-review.md`
4. `.claude/rules/response-exception.md`
5. `README.md` (협업 정책 단일 소스)

실행 프롬프트는 아래를 사용한다.

- `.claude/commands/commit.md`
- `.claude/commands/pr.md`
- `.claude/commands/review.md`

## 프로젝트 최소 컨텍스트

- Java 21 / Spring Boot 4.0.6 / Gradle
- 테스트 DB: H2
- 주요 자동화:
  - `.github/workflows/issue-to-notion.yml`
  - `.github/workflows/pr-sync-notion.yml`
  - `.github/scripts/notion-sync.mjs`

## 필수 실행 명령

```bash
./gradlew clean build
./gradlew test
./gradlew bootRun
node --check .github/scripts/notion-sync.mjs
```

YAML 변경 시 문법 검증도 수행한다.

## Notion 연동 필수값

필수 GitHub Secrets:

- `NOTION_TOKEN`
- `NOTION_DATABASE_ID`
- `NOTION_PEOPLE_MAP`

현재 운영 DB 컬럼(백엔드 db):

- `작업 이름` (title)
- `Github 이슈 번호` (number)
- `Github 이슈 URL` (url)
- `Github PR` (url)
- `상태` (status)
- `사람` (people)
- `마감일` (date)

참고: 스크립트는 별칭 기반 자동 매핑을 지원하지만, 핵심 필드는 위 구조를 기준으로 유지한다.

## 금지/주의 사항

- PR 본문에 `Closes #N` 또는 `Fixes #N` 누락 금지
- `#0` 이슈 번호 사용 금지
- 작업 범위 밖 변경 커밋 금지
- 실패한 검증 숨기기 금지
- 템플릿/자동화 규칙은 `README.md`와 충돌되게 수정하지 않는다.
