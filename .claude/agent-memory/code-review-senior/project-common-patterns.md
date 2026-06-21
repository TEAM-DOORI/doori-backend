---
name: project-common-patterns
description: 프로젝트 전반의 공통 패턴 — Controller 구조, Service 헬퍼 메서드 중복, N+1 대응
metadata:
  type: project
---

## Controller 패턴
- `@RequestMapping` 없이 각 메서드에 전체 경로를 직접 작성하는 방식도 사용됨 (FavoriteController)
- PostController는 `@RequestMapping("/api/posts")`를 사용함 — 두 스타일이 혼재하므로 통일 권장

## Service 헬퍼 메서드 중복
- `findActiveMember(Long memberId)` 헬퍼 메서드가 PostService, FavoriteService, BlockService, UserProfileService, UserDiscoveryService에 동일하게 중복 존재 (5곳)
- 공통 유틸 또는 상위 Service로 추출할 여지 있음 — 이 중복은 리뷰마다 반복 지적 대상

## N+1 대응
- UserFavoriteRepository: `JOIN FETCH uf.target`으로 target(Member) 로딩
- PostFavoriteRepository: `JOIN FETCH pf.post p JOIN FETCH p.author`로 post, author 로딩
- roomImages(@ElementCollection)는 fetch join 미적용 — 찜한 게시글 N건에 대해 roomImages 쿼리 N번 발생 가능성 있음 (목록 응답에서 썸네일 1장만 필요하므로 실용적 트레이드오프)

## 테스트 패턴
- `@SpringBootTest + @Transactional`로 H2 인메모리 DB 사용, 각 테스트 후 롤백
- `SecurityMockMvcRequestPostProcessors.authentication()`으로 JWT 없이 인증 주입
