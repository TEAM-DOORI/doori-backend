# Doori Backend 협업 가이드

팀 프로젝트에서 이슈, 브랜치, PR, Notion 상태를 한 흐름으로 관리하기 위한 규칙입니다.

## 1. 네이밍 규칙

### 1-1. 이슈 제목

형식:

```text
[이모티콘 타입] 작업 내용
```

예시:

```text
[✨ Feat] 로그인 API 구현
[🐛 Fix] 홈 피드 조회 시 500 에러 수정
[🧹 Chore] 공통 예외 응답 포맷 정리
[📝 Docs] 배포 가이드 문서화
[♻️ Refactor] 인증 필터 구조 분리
```

타입 가이드:

| 타입 | 이모티콘 | 용도 |
| --- | --- | --- |
| `Feat` | ✨ | 새 기능 |
| `Fix` | 🐛 | 버그 수정 |
| `Chore` | 🧹 | 설정/빌드/의존성/운영 작업 |
| `Docs` | 📝 | 문서 작업 |
| `Refactor` | ♻️ | 동작 변경 없는 구조 개선 |

### 1-2. 브랜치 이름

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
- 이슈 번호는 필수 (`Notion` 추적 기준)
- 브랜치에는 이모티콘을 넣지 않음 (터미널/자동화 호환성)

### 1-3. PR 제목

형식:

```text
[이모티콘 타입] 작업 내용 (#이슈번호)
```

예시:

```text
[✨ Feat] 로그인 API 구현 (#12)
[🐛 Fix] 홈 피드 조회 시 500 에러 수정 (#34)
```

규칙:

- 원칙적으로 이슈 제목과 동일한 문맥 유지
- PR 본문에 `Closes #이슈번호` 또는 `Fixes #이슈번호` 반드시 포함

### 1-4. 한눈에 보는 연결 예시

```text
이슈:   [✨ Feat] 로그인 API 구현
브랜치: feat/12-login-api
PR:     [✨ Feat] 로그인 API 구현 (#12)
```

---

## 2. 개발 시작

```bash
git clone <repo-url>
cd doori-backend
./gradlew clean build
./gradlew bootRun
```

- Java 21 기준
- 로컬에서 최소 1회 `./gradlew clean build` 성공 후 작업 시작 권장

---

## 3. 협업 워크플로우

```text
Issue 생성 -> Branch 생성 -> 개발/커밋 -> Draft PR 오픈 -> CI/Auto Review 반영 -> 팀 리뷰 -> Merge
```

### 3-1. 이슈 생성

- GitHub Issues에서 `Task` 템플릿으로 등록
- 이슈 생성 시 Notion DB에 일감 자동 생성 (`시작전`)

### 3-2. 브랜치 생성

- 이슈 번호가 포함된 브랜치로 생성

```bash
git checkout -b feat/12-login-api
```

### 3-3. 개발 및 커밋

- 작업 시작 전 Notion 일감 속성 업데이트: 담당자, 마감일, 상태
- 커밋 메시지는 Conventional Commits + 이모티콘 권장

```text
feat: ✨ 로그인 API 구현
fix: 🐛 피드 조회 500 에러 수정
chore: 🧹 예외 응답 코드 정리
docs: 📝 README 업데이트
refactor: ♻️ 인증 로직 분리
```

### 3-4. PR 생성

- 처음에는 Draft PR 권장
- PR 템플릿의 연관 이슈 항목에 `Fixes #이슈번호` 입력
- Draft 상태에서 CI와 Auto Review 피드백 우선 반영

현재 CI 기준 기본 확인 명령:

| 항목 | 명령어 |
| --- | --- |
| Build + Test | `./gradlew clean build --no-daemon` |
| Test only | `./gradlew test` |

### 3-5. 코드 리뷰

- 최소 1명 Approve 후 머지
- 리뷰 반영 커밋 후 CI 재통과 확인

### 3-6. 머지

- `main` 머지 시 Notion 상태 `완료`로 동기화
- 연결 이슈 자동 닫힘

---

## 4. Notion 상태 자동화 규칙

| 이벤트 | Notion 상태 |
| --- | --- |
| 이슈 생성 | `시작전` |
| PR 오픈 (Draft 포함) | `리뷰중` |
| PR 머지 | `완료` |
| 이슈를 `not planned`로 종료 | `취소됨` |
| `취소됨` 상태 이슈 재오픈 | `시작전` |

메모:

- `진행중` 전환은 실무 기준으로 수동 업데이트
- 작업 시작 전에만 담당자/마감일/상태를 정확히 맞춰도 추적 품질이 크게 올라감

---

## 5. 팀 운영 원칙 (요약)

- 제목, 브랜치, PR 네이밍은 항상 규칙 우선
- 자동화가 읽을 수 있는 정보(`이슈 번호`, `Fixes/Closes`)를 누락하지 않기
- 머지 전에는 "리뷰 반영 + CI 통과"를 하나의 완료 기준으로 본다
