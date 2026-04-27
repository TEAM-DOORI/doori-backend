현재 브랜치 변경사항을 기준으로 PR 제목/본문을 작성해줘.

반드시 아래 규칙 파일을 우선 참고해:
- `.claude/rules/pr.md`
- `.github/pull_request_template.md`

작업 순서:
1. `git log --oneline origin/main..HEAD`로 PR 포함 커밋 확인
2. `git diff --stat origin/main...HEAD`로 변경 범위 요약
3. 규칙에 맞는 PR 제목 생성
4. 템플릿 형식에 맞는 PR 본문 생성
5. 본문에 테스트 실행 명령/결과를 반영

출력 형식:

## PR 제목

```text
[이모티콘 타입] 작업 내용 (#이슈번호)
```

## PR 본문

```md
(완성된 본문)
```

체크 포인트:
- `Closes #이슈번호` 또는 `Fixes #이슈번호`가 반드시 포함되어야 한다.
- API/DB 영향이 있으면 구현 상세에 반드시 언급한다.
- Notion 동기화에 필요한 정보(연관 이슈, 요약)가 누락되면 안 된다.
