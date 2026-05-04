인자 `$ARGUMENTS`에서 첫 번째 단어를 타입(TYPE), 두 번째 단어를 이슈번호(ISSUE_NO)로 해석해.

반드시 아래 규칙 파일을 우선 참고해:
- `.claude/rules/commit.md`
- `.claude/rules/response-exception.md`

작업 순서:
1. 타입이 `feat/fix/chore/docs/refactor` 외이거나 이슈번호가 없으면 실행 전에 알려주고 중단한다.
2. `gh issue view {ISSUE_NO}` 로 이슈 상세 정보 조회
3. 이슈 제목에서 브랜치 슬러그 생성
   - `[✨ Feat]` 형태의 이모티콘/접두사 제거
   - 한국어는 의미를 유지하며 영어로 번역
   - 영어 소문자 + 하이픈만 사용 (공백 → 하이픈, 특수문자/이모티콘 → 제거)
   - 예: `[✨ Feat] 로그인 API 구현` → `login-api`
4. 브랜치명 확정: `{TYPE}/{ISSUE_NO}-{슬러그}`
5. `git checkout -b {브랜치명}` 으로 브랜치 생성
   - 이미 존재하면 `git checkout {브랜치명}` 으로 전환 후 이미 존재함을 명시
6. 이슈 내용을 분석해 구현 계획 작성 — 코드는 구현하지 않는다

출력 형식:

## 브랜치
- 생성(또는 전환)한 브랜치명

## 이슈 요약
- 이슈 제목과 핵심 목표 한 줄 요약

## 구현 계획

### 변경할 파일
- 파일 경로 및 변경 이유

### 새로 만들 파일
- 파일 경로 및 역할

### 구현 순서
1. 
2. 

### response-exception.md 체크
- [ ] Controller 반환 타입이 `ResponseEntity<ApiResponse<T>>` 또는 `ResponseEntity<Void>`인가
- [ ] Service/Domain이 `ApiResponse`나 `ErrorResponse`를 반환하지 않는가
- [ ] 비즈니스 예외가 `CustomException`으로 던져지는가
- [ ] 새 `ErrorCode` 추가 시 접두사가 도메인 기준에 맞는가
- [ ] 새 `ErrorCode` 번호가 기존과 중복되지 않는가

### 주의사항
- 이슈에서 예측되는 예외 케이스
- 레이어 경계 위반 위험 포인트

주의:
- 이슈 내용이 부족하면 추측으로 채우지 마. 정보가 없는 항목은 "이슈에 명시되지 않음"으로 표기해.
- 계획 출력 후 코드를 바로 구현하지 마. 사용자 확인 후 진행 여부를 결정한다.
