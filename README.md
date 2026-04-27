![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen)
![GitHub Actions](https://img.shields.io/badge/CI-GitHub%20Actions-black)

# Doori Backend

팀 협업 규칙, 개발 환경 설정, Claude Code 사용법을 한 곳에 정리합니다.

---

## 목차

- [빠른 시작](#빠른-시작)
- [Claude Code 설정](#claude-code-설정)
- [협업 워크플로우](#협업-워크플로우)
- [네이밍 규칙](#네이밍-규칙)
- [Notion 자동화](#notion-자동화)
- [팀 운영 원칙](#팀-운영-원칙)

---

## 빠른 시작

> Java 21이 설치되어 있어야 합니다.

```bash
git clone <repo-url>
cd doori-backend
./gradlew clean build
./gradlew bootRun
```

- 로컬에서 최소 1회 `./gradlew clean build` 성공 후 작업 시작 권장

---

## Claude Code 설정

이 프로젝트는 팀 협업 규칙을 자동화하는 Claude Code 커스텀 커맨드를 제공합니다.

### 설치

> Node.js 18 이상 필요

```bash
npm install -g @anthropic-ai/claude-code
```

### 초기 인증

```bash
claude
```

최초 실행 시 Claude.ai 계정 로그인 또는 API 키 입력 안내가 표시됩니다.

### 슬래시 커맨드

프로젝트 루트에서 아래 커맨드를 사용할 수 있습니다.

| 커맨드 | 실행 위치 | 하는 일 |
|---|---|---|
| `/commit` | 프로젝트 루트 | 변경사항 확인 → 자동 검증 → 규칙에 맞는 커밋 메시지 제안 후 커밋 |
| `/pr` | 프로젝트 루트 | 브랜치 커밋 기준으로 PR 제목/본문 초안 자동 생성 |
| `/review` | 프로젝트 루트 | PR 올리기 전 사전 코드 리뷰 (심각도 분류 포함) |

**주의사항**

- 반드시 프로젝트 루트 디렉토리에서 `claude` 명령으로 실행해야 커스텀 커맨드가 로드됩니다.
- `/commit`은 `./gradlew test` 실행 여부를 판단합니다. 로컬 빌드가 가능한 상태여야 합니다.
- 각 커맨드의 상세 동작 규칙은 `.claude/rules/` 파일을 참조하세요. README와는 별개로 관리됩니다.

---

## 협업 워크플로우

```mermaid
flowchart LR
    A["📋 이슈 생성"] --> B["🌿 브랜치 생성"]
    B --> C["💻 개발 & 커밋"]
    C --> D["📝 PR 생성"]
    D --> E["🔍 CI & 리뷰"]
    E --> F["✅ 머지"]
    F --> G["🎉 완료"]
```

### 이슈 생성

- GitHub Issues에서 `Task` 템플릿으로 등록
- 이슈 생성 시 Notion DB에 일감 자동 생성 (상태: `시작전`)

### 브랜치 생성

- 이슈 번호가 포함된 브랜치로 생성

```bash
git checkout -b feat/12-login-api
```

포맷은 [네이밍 규칙](#브랜치-이름) 섹션 참조

### 개발 및 커밋

- 작업 시작 전 Notion 일감 속성 업데이트: 담당자, 마감일, 상태
- 커밋 메시지 포맷은 [네이밍 규칙](#커밋-메시지) 섹션 참조

> **Claude Code 팁**: `/commit` 커맨드로 변경사항 자동 검증 및 규칙에 맞는 커밋 메시지 생성

### PR 생성

- 처음에는 Draft PR 권장
- PR 템플릿의 연관 이슈 항목에 `Fixes #이슈번호` 입력
- Draft 상태에서 CI와 Auto Review 피드백 우선 반영

현재 CI 기준 기본 확인 명령:

| 항목 | 명령어 |
|---|---|
| Build + Test | `./gradlew clean build --no-daemon` |
| Test only | `./gradlew test` |

> **Claude Code 팁**: `/pr` 커맨드로 브랜치 커밋을 기반으로 PR 제목/본문 초안 자동 생성

### 코드 리뷰

- 최소 1명 Approve 후 머지
- 리뷰 반영 커밋 후 CI 재통과 확인

> **Claude Code 팁**: `/review` 커맨드로 PR 올리기 전 사전 셀프 리뷰 (심각도 분류 포함)

### 머지

- `main` 머지 시 Notion 상태 `완료`로 자동 동기화
- 연결 이슈 자동 닫힘

---

## 네이밍 규칙

### 이슈 제목

형식:

```text
[이모티콘 타입] 작업 내용
```

예시:

```text
[✨ Feat] 로그인 API 구현
[🔨 Fix] 홈 피드 조회 시 500 에러 수정
[🧹 Chore] 공통 예외 응답 포맷 정리
[📝 Docs] 배포 가이드 문서화
[♻️ Refactor] 인증 필터 구조 분리
```

타입 가이드:

| 타입 | 이모티콘 | 용도 |
|---|---|---|
| Feat | ✨ | 새 기능 |
| Fix | 🔨 | 버그 수정 |
| Chore | 🧹 | 설정/빌드/의존성/운영 작업 |
| Docs | 📝 | 문서 작업 |
| Refactor | ♻️ | 동작 변경 없는 구조 개선 |

### 브랜치 이름

형식:

```text
타입/이슈번호-작업-내용
```

예시:

```text
feat/12-login-api
fix/34-feed-500-error
chore/5-exception-format
```

규칙:

- 소문자와 하이픈(`-`)만 사용
- 이슈 번호는 필수 (Notion 추적 기준)
- 브랜치에는 이모티콘을 넣지 않음 (터미널/자동화 호환성)

### PR 제목

형식:

```text
[이모티콘 타입] 작업 내용 (#이슈번호)
```

예시:

```text
[✨ Feat] 로그인 API 구현 (#12)
[🔨 Fix] 홈 피드 조회 시 500 에러 수정 (#34)
```

규칙:

- 원칙적으로 이슈 제목과 동일한 문맥 유지
- PR 본문에 `Closes #이슈번호` 또는 `Fixes #이슈번호` 반드시 포함

### 커밋 메시지

형식:

```text
타입: 이모티콘 작업 내용
```

예시:

```text
feat: ✨ 로그인 API 구현
fix: 🔨 피드 조회 500 에러 수정
chore: 🧹 예외 응답 코드 정리
docs: 📝 README 업데이트
refactor: ♻️ 인증 로직 분리
```

타입은 이슈 제목과 동일하게 매핑합니다.

### 한눈에 보는 연결 예시

```text
이슈:   [✨ Feat] 로그인 API 구현
브랜치: feat/12-login-api
PR:     [✨ Feat] 로그인 API 구현 (#12)
커밋:   feat: ✨ 로그인 API 구현
```

---

## Notion 자동화

GitHub 이벤트와 Notion 상태를 자동으로 동기화합니다.

| 이벤트 | Notion 상태 |
|---|---|
| 이슈 생성 | `시작전` |
| PR 오픈 (Draft 포함) | `리뷰중` |
| PR 머지 | `완료` |
| 이슈를 `not planned`로 종료 | `취소됨` |
| `취소됨` 상태 이슈 재오픈 | `시작전` |

메모:

- `진행중` 전환은 수동 업데이트 (팀이 실제 작업 시작 시 직접 변경)
- 작업 시작 전에 담당자/마감일/상태를 정확히 맞추면 추적 품질이 크게 올라갑니다

### Notion DB 필드

| 필드명 | 타입 | 비고 |
|---|---|---|
| `작업 이름` | Title | 이슈/PR 제목 동기화 |
| `Github 이슈 번호` | Number | GitHub issue number 키 |
| `Github 이슈 URL` | URL | GitHub issue 링크 |
| `Github PR` | URL | GitHub PR 링크 |
| `상태` | Status/Select | `시작전/진행중/리뷰중/완료/취소됨` |
| `사람` | People | `NOTION_PEOPLE_MAP` 기반 동기화 |
| `마감일` | Date | 이슈 본문 `마감일 (YYYY-MM-DD)` 파싱 |

---

## 팀 운영 원칙

세 가지 핵심 원칙으로 팀 협업을 관리합니다.

1. **제목, 브랜치, PR 네이밍은 항상 규칙 우선**
   - 자동화가 읽을 수 있는 정보 형식 유지

2. **자동화가 읽을 수 있는 정보를 누락하지 않기**
   - 이슈 번호는 필수
   - PR 본문에 `Fixes/Closes #이슈번호` 필수
   - Notion 동기화 실패 방지

3. **머지 전 "리뷰 반영 + CI 통과"를 완료 기준**
   - 리뷰 지적사항 반영 후 재커밋
   - CI 재통과 확인 후 머지
