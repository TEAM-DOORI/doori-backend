---
name: project-errorcode-patterns
description: 프로젝트 ErrorCode 네임스페이스 현황 및 번호 할당 상태
metadata:
  type: project
---

현재 사용 중인 ErrorCode 접두사 및 번호 할당 현황 (2026-05-28 기준):

- C (공통): C001, C002, C003
- A (Auth): A001~A007, A009, A010 — **A008 결번** (삭제 또는 미발행)
- S (School): S001
- U (User): U001, U002, U003
- P (Post): P001, P002
- F (Favorite): F001, F002
- B (Block): B001, B002, B003 (feat/24-favorite-crud 브랜치에서 추가됨)

**Why:** A008 결번은 삭제된 에러코드 자리로, 재사용 금지 규칙에 따라 비어 있음. 새 Auth 에러 추가 시 A011부터 채번해야 한다.

**How to apply:** 새 에러코드 추가 리뷰 시 기존 번호 중복 및 결번 재사용 여부를 위 목록 기준으로 확인한다.
