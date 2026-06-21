---
name: project-auth-principal
description: JWT 인증에서 Authentication.getPrincipal()이 Long(memberId)를 반환하는 방식
metadata:
  type: project
---

JwtAuthenticationFilter에서 `UsernamePasswordAuthenticationToken(memberId, null, List.of(...))`로 principal에 Long(memberId)를 직접 주입한다.

따라서 Controller에서 `(Long) authentication.getPrincipal()` 캐스팅은 프로젝트 전체 규약상 안전하다. PostController와 FavoriteController 모두 동일한 패턴을 사용하며, 기존 프로젝트 코드에서 일관되게 적용 중.

**Why:** JwtProvider.validateAccessTokenAndGetMemberId()가 Long을 반환하고 필터가 이를 principal로 직접 설정하므로 ClassCastException 위험 없음.

**How to apply:** Controller 리뷰 시 `(Long) authentication.getPrincipal()` 패턴은 프로젝트 표준으로 인정하되, 필터 변경 시 모든 Controller에 영향을 주므로 주의.
