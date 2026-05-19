# Post API 구현 Roadmap

**이슈**: #22 — Post API 기본 CRUD 및 목록/상세 조회 기능 구현  
**브랜치**: `feat/22-post-api`  
**예상 총 소요 시간**: 3-4일

---

## 개발 단계

### Phase 1: 프로젝트 초기 설정 (골격 구축)

#### 작업 내용
- [ ] `ErrorCode.java`에 Post 도메인 에러 코드 추가
  - `POST_NOT_FOUND (P001)` — 게시글을 찾을 수 없습니다.
  - `POST_FORBIDDEN (P002)` — 게시글을 수정하거나 삭제할 권한이 없습니다.
- [ ] 디렉토리 구조 생성
  ```
  src/main/java/com/doori/doori_backend/post/
  ├── controller/
  ├── domain/
  ├── dto/
  │   ├── request/
  │   └── response/
  ├── repository/
  └── service/
  ```

#### 왜 이 순서로?
새로운 도메인의 기반이 되는 **에러 정의**와 **디렉토리 구조**를 먼저 구축해야 일관된 계층 구조를 유지할 수 있습니다. 이를 통해 이후 개발에서 규칙을 쉽게 따를 수 있습니다.

#### 예상 소요 시간
**30분 ~ 1시간**

#### 완료 기준
- [ ] ErrorCode에 P001, P002 정상 추가
- [ ] 5개 디렉토리 생성 완료
- [ ] `./gradlew clean build` 성공 (컴파일 에러 없음)

---

### Phase 2: 공통 모듈/컴포넌트 개발

#### 작업 내용
- [ ] **DTO 정의** (4개 클래스)
  - `PostCreateRequest.java` — 게시글 생성 요청
  - `PostUpdateRequest.java` — 게시글 수정 요청
  - `PostResponse.java` — 게시글 응답 (from() 변환 메서드 포함)
  - `PostListResponse.java` — 게시글 목록 응답
  
- [ ] **Enum 정의**
  - `PostType.java` — TRANSFER, SUBLEASE, WANTED

#### 왜 이 순서로?
DTO와 Enum은 엔티티, Repository, Service, Controller에서 공통으로 사용되는 **계약(Contract)**입니다. 먼저 정의하면 이후 계층들을 일관되게 구현할 수 있습니다.

#### 예상 소요 시간
**30분 ~ 45분**

#### 완료 기준
- [ ] 4개 DTO 클래스 작성 완료
- [ ] PostResponse.from() 메서드 구현
- [ ] PostListResponse.from() 메서드 구현
- [ ] PostType Enum 3가지 타입 정의
- [ ] `./gradlew clean build` 성공

---

### Phase 3: 핵심 기능 개발

#### 작업 내용

**3-1. 도메인 엔티티 구현** (45분 ~ 1시간)
- [ ] `Post.java` 엔티티 구현
  - 필드: postId, postType, title, region, university, monthlyRent, deposit, description, roomImages (ElementCollection)
  - 관계: ManyToOne으로 Member(author)
  - 메서드: `update()` (수정), `isOwnedBy()` (소유권 확인)
  - JPA Auditing: createdAt, updatedAt

**3-2. Repository 구현** (30분 ~ 45분)
- [ ] `PostRepository.java` 구현
  - 기본 CRUD: JpaRepository 상속
  - 커스텀 쿼리: `findAllByFilter(postType, pageable)` — postType 필터링

**3-3. Service 구현** (1시간 ~ 1시간 30분)
- [ ] `PostService.java` 구현
  - `createPost()` — 게시글 생성, 201 반환
  - `getPosts()` — 목록 조회 (필터, 페이지네이션)
  - `getPost()` — 단건 상세 조회
  - `updatePost()` — 소유권 검증 후 수정
  - `deletePost()` — 소유권 검증 후 삭제
  - 헬퍼 메서드: `findPost()`, `validateOwnership()`, `findActiveMember()`, `parsePostType()`
  - 모든 메서드에 `@Transactional` 적용

**3-4. Controller 구현** (45분 ~ 1시간)
- [ ] `PostController.java` 구현
  - `POST /api/posts` → createPost() → 201 + ApiResponse
  - `GET /api/posts` → getPosts() → 200 + ApiResponse
  - `GET /api/posts/{postId}` → getPost() → 200 + ApiResponse
  - `PUT /api/posts/{postId}` → updatePost() → 204 Void
  - `DELETE /api/posts/{postId}` → deletePost() → 204 Void
  - 모든 메서드: `ResponseEntity<ApiResponse<T>>` 또는 `ResponseEntity<Void>` 패턴

#### 왜 이 순서로?
엔티티 → Repository → Service → Controller 순서는 **의존성 방향**을 따릅니다:
- Service는 Repository에 의존
- Controller는 Service에 의존

이 순서로 구현하면 각 계층을 테스트하면서 진행할 수 있고, 컴파일 에러를 단계적으로 해결할 수 있습니다.

#### 예상 소요 시간
**4시간 ~ 5시간**

#### 완료 기준
- [ ] Post 엔티티 구현 완료 (테스트 DB에서 스키마 자동 생성)
- [ ] PostRepository 기본 메서드 동작 확인
- [ ] PostService 비즈니스 로직 구현 완료
- [ ] PostController 5개 엔드포인트 구현 완료
- [ ] `./gradlew clean build` 성공 (컴파일 에러 없음)
- [ ] `./gradlew bootRun` 실행 시 서버 정상 시작

---

### Phase 4: 추가 기능 개발 (테스트 작성)

#### 작업 내용
- [ ] **통합 테스트 작성** — `PostControllerIntegrationTest.java`
  - 테스트 환경: `@SpringBootTest` + H2 + `addFilters=false`
  - 인증 모킹: `SecurityContextHolder` 수동 주입
  - 테스트 케이스 12개:
    ```
    [생성] createPost_success, createPost_missingField_returns400
    [목록] getPosts_noFilter_returnsAll, getPosts_withFilter, getPosts_invalidType_returns400
    [상세] getPost_exists_returns200, getPost_notFound_returns404
    [수정] updatePost_owner_returns204, updatePost_notOwner_returns403, updatePost_notFound_returns404
    [삭제] deletePost_owner_returns204, deletePost_notOwner_returns403, deletePost_notFound_returns404
    ```

#### 왜 이 순서로?
핵심 기능 구현 후 테스트를 작성합니다. 이를 통해:
- 실제 동작 검증
- 엣지 케이스 발견
- 리팩토링 안정성 확보

#### 예상 소요 시간
**1시간 30분 ~ 2시간**

#### 완료 기준
- [ ] PostControllerIntegrationTest 작성 완료
- [ ] 12개 테스트 케이스 모두 GREEN (통과)
- [ ] `./gradlew test` 전체 테스트 통과
- [ ] 테스트 커버리지 80% 이상 (선택사항)

---

### Phase 5: 최적화 및 배포

#### 작업 내용
- [ ] **코드 리뷰 및 정리**
  - response-exception.md 규칙 체크
  - 불필요한 임포트 정리
  - 로그 레벨 확인
  - 테스트 격리 확인 (@Transactional 롤백)

- [ ] **수동 테스트** (API 검증)
  - `./gradlew bootRun` 실행
  - Postman/curl로 5개 엔드포인트 모두 테스트
    ```bash
    # 생성 (201)
    curl -X POST http://localhost:8080/api/posts \
      -H "Authorization: Bearer {token}" \
      -H "Content-Type: application/json" \
      -d '{"postType":"TRANSFER","title":"...","region":"마포구",...}'
    
    # 목록 (200)
    curl http://localhost:8080/api/posts?postType=TRANSFER
    
    # 상세 (200 or 404)
    curl http://localhost:8080/api/posts/1
    
    # 수정 (204 or 403)
    curl -X PUT http://localhost:8080/api/posts/1 -H "Authorization: Bearer {token}" -d '{...}'
    
    # 삭제 (204 or 403)
    curl -X DELETE http://localhost:8080/api/posts/1 -H "Authorization: Bearer {token}"
    ```

- [ ] **PR 생성 및 리뷰**
  - 커밋 메시지: `feat: ✨ Post API 기본 CRUD 및 목록/상세 조회 구현 (#22)`
  - PR 본문에 Closes #22 포함
  - CI 통과 확인
  - 팀 리뷰 승인

- [ ] **main 브랜치 병합**
  - PR 머지
  - 배포 (필요시)

#### 왜 이 단계가 필요한가?
완성도 있는 코드를 프로젝트에 통합하기 위해:
- 규칙 준수 확인
- 실제 사용 가능성 검증
- 팀 협업 프로세스 이행

#### 예상 소요 시간
**1시간 ~ 1시간 30분**

#### 완료 기준
- [ ] response-exception.md 체크리스트 모두 체크 완료
- [ ] 수동 테스트 5개 엔드포인트 모두 정상 동작 확인
- [ ] 모든 테스트 통과 (./gradlew test)
- [ ] PR 머지 완료
- [ ] Notion DB 자동 동기화 확인 (이슈 상태 변경)

---

## 타임라인 요약

| Phase | 작업 | 예상 시간 | 누적 시간 |
|-------|------|---------|---------|
| 1 | 초기 설정 (ErrorCode, 디렉토리) | 30-60분 | 30-60분 |
| 2 | DTO, Enum 정의 | 30-45분 | 1-1.75시간 |
| 3 | 엔티티, Repo, Service, Controller | 4-5시간 | 5-6.75시간 |
| 4 | 통합 테스트 작성 | 1.5-2시간 | 6.5-8.75시간 |
| 5 | 리뷰, 수동 테스트, PR 병합 | 1-1.5시간 | 7.5-10.25시간 |
| **총** | | | **7.5-10시간** |

**권장**: 2-3일에 걸쳐 진행 (하루 3-4시간 작업)

---

## 중요 주의사항

### 핵심 규칙 (반드시 준수)

1. **@ElementCollection 관리**
   - `roomImages` 필드의 값 변경은 Service 트랜잭션 내에서만
   - Controller에서 Post 엔티티 직접 접근 금지 → LazyInitializationException 위험

2. **계층 경계 준수**
   - Controller: `ResponseEntity<ApiResponse<T>>` 또는 `ResponseEntity<Void>`만 반환
   - Service: `CustomException` 사용, ApiResponse 미반환
   - Repository: 엔티티만 반환

3. **ErrorCode 네이밍**
   - Post 도메인 전용: `P` 접두사
   - 한번 부여한 코드 번호(P001, P002)는 변경/삭제 금지

4. **트랜잭션 관리**
   - `createPost()`, `updatePost()`, `deletePost()`: `@Transactional`
   - `getPost()`, `getPosts()`: `@Transactional(readOnly=true)`

### 테스트 시 주의

- SecurityContext 설정 후 테스트 실행
- 각 테스트 후 `SecurityContextHolder.clearContext()` 호출
- `@Transactional`로 테스트 간 데이터 격리

### 커밋 가이드

각 Phase마다 하나의 커밋으로 작성:

```bash
# Phase 1
git add src/main/java/com/doori/doori_backend/global/error/ErrorCode.java
git commit -m "chore: 🧹 Post 도메인 ErrorCode 추가 (P001, P002)"

# Phase 2
git add src/main/java/com/doori/doori_backend/post/dto/
git add src/main/java/com/doori/doori_backend/post/domain/PostType.java
git commit -m "feat: ✨ Post API DTO 및 PostType Enum 정의"

# Phase 3
git add src/main/java/com/doori/doori_backend/post/
git commit -m "feat: ✨ Post API 엔티티, Repository, Service, Controller 구현"

# Phase 4
git add src/test/java/com/doori/doori_backend/post/
git commit -m "test: 📝 Post API 통합 테스트 작성 (12개 케이스)"

# Phase 5
git commit -m "Merge pull request #22"
```

---

## 완료 체크리스트

### 구현 완료
- [ ] Phase 1: 초기 설정 완료
- [ ] Phase 2: DTO/Enum 완료
- [ ] Phase 3: 엔티티/Repo/Service/Controller 완료
- [ ] Phase 4: 통합 테스트 완료
- [ ] Phase 5: 코드 리뷰 및 수동 테스트 완료

### 규칙 준수
- [ ] response-exception.md 규칙 모두 체크
- [ ] commit.md 브랜치/커밋 네이밍 규칙 준수
- [ ] 테스트 모두 GREEN

### 배포 준비
- [ ] PR 작성 (Closes #22 포함)
- [ ] CI 통과
- [ ] 팀 리뷰 승인
- [ ] main 브랜치 병합

---

**Last Updated**: 2026-05-19  
**Status**: Ready for Implementation ✅
