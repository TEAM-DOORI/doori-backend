# Response & Exception Rules

## 계층별 사용 규칙

각 클래스는 사용 가능한 레이어가 고정되어 있다.
레이어를 벗어난 사용은 반드시 거부한다.

| 클래스 | 사용 가능 레이어 | 금지 레이어 |
|---|---|---|
| `ApiResponse<T>` | Controller | Service, Repository, Domain |
| `ErrorResponse` | GlobalExceptionHandler | Controller, Service, Repository |
| `CustomException` | Service, Domain | Controller |
| `ErrorCode` | 어디서든 참조 가능 | - |

### Controller

- 반환 타입은 `ResponseEntity<ApiResponse<T>>`로 통일한다.
- 본문이 없는 성공 응답(`204 No Content`)은 `ResponseEntity<Void>`를 반환한다.
- `ErrorResponse`를 직접 생성하거나 반환하지 않는다.
- `CustomException`을 catch하거나 직접 에러를 처리하지 않는다.

### Service / Domain

- 비즈니스 예외는 `throw new CustomException(ErrorCode.XXX)` 형태로 던진다.
- `ApiResponse`나 `ErrorResponse`를 반환 타입으로 쓰지 않는다.
- `CustomException(ErrorCode errorCode, String message)` 오버로드는 기본 메시지 외
  상황별 설명이 필요할 때만 사용한다. 불필요한 메시지 재정의는 금지한다.

### GlobalExceptionHandler

- `ErrorResponse`를 생성하는 유일한 진입점이다.
- 새 예외 유형을 처리해야 할 때 이 파일에만 `@ExceptionHandler` 메서드를 추가한다.

---

## Controller 반환 타입 패턴

```java
// 200 OK + 바디 있음 (데이터 반환)
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success(userService.getUser(id)));
}

// 201 Created
@PostMapping
public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody @Valid UserCreateRequest request) {
    UserResponse created = userService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("생성 완료", created));
}

// 204 No Content (바디 없음)
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
}
```

`ApiResponse.success(data)` 와 `ApiResponse.success(message, data)` 두 가지 팩토리만 존재한다.
`ApiResponse.failure(...)` 같은 메서드는 없다. 에러는 예외를 던지는 것으로만 처리한다.

---

## ErrorCode 네이밍 규칙

### 네임스페이스 접두사

| 접두사 | 대상 | 코드 범위 예시 |
|---|---|---|
| `C` | 공통 (도메인 무관) | `C001`, `C002`, `C003` |
| `A` | 인증/인가 (Auth) | `A001`, `A002` |
| 도메인 약어 | 각 도메인 전용 에러 | `U001`(User), `P001`(Post) 등 |

### 공통(C) vs 도메인별 구분 기준

**공통(C) 사용 조건** - 아래 중 하나라도 해당되면 공통:
- 특정 도메인 없이 어느 레이어에서도 발생할 수 있는 에러
- `MethodArgumentNotValidException` 등 Spring 인프라 수준 예외에 매핑되는 에러
- 현재: `COMMON_BAD_REQUEST`, `COMMON_INTERNAL_SERVER_ERROR`, `COMMON_NOT_FOUND`

**도메인별 사용 조건** - 아래 중 하나라도 해당되면 도메인 접두사:
- 특정 도메인 객체의 상태나 비즈니스 규칙 위반
- 에러 메시지가 해당 도메인 맥락 없이 이해하기 어려울 때
- 예: 사용자가 이미 존재함 → `USER_DUPLICATE`, 게시글 권한 없음 → `POST_FORBIDDEN`

### 이름 형식

```
{NAMESPACE}_{UPPER_SNAKE_CASE}
```

- 동사보다 명사/형용사 조합을 우선한다.
- `NOT_FOUND`, `DUPLICATE`, `UNAUTHORIZED`, `FORBIDDEN`, `INVALID` 같은 표준 접미사를 재사용한다.
- 모호한 이름 금지: `USER_ERROR`, `POST_FAIL` → `USER_NOT_FOUND`, `POST_FORBIDDEN`

### 코드 번호 할당 규칙

- 접두사 내에서 순번으로 채번한다. (`A001`, `A002`, ...)
- 한번 부여한 코드 번호는 변경하거나 재사용하지 않는다.
- 새 도메인을 추가할 때 접두사가 기존 목록과 충돌하지 않는지 먼저 확인한다.

---

## 올바른 예시

```java
// Service: CustomException을 던지는 유일한 방법
public UserResponse getUser(Long id) {
    return userRepository.findById(id)
        .map(UserResponse::from)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
}

// Service: 상황별 메시지가 필요한 경우만 오버로드 사용
public void transferMoney(Long fromId, Long toId, long amount) {
    if (amount <= 0) {
        throw new CustomException(ErrorCode.COMMON_BAD_REQUEST, "이체 금액은 0보다 커야 합니다.");
    }
}

// Controller: ResponseEntity + ApiResponse 조합
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success(userService.getUser(id)));
}

// ErrorCode: 도메인 에러 추가 방법
USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
USER_DUPLICATE(HttpStatus.CONFLICT, "U002", "이미 존재하는 사용자입니다."),
```

---

## 잘못된 예시

```java
// Controller에서 CustomException을 catch해서 ApiResponse로 반환 — 금지
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<?>> getUser(@PathVariable Long id) {
    try {
        return ResponseEntity.ok(ApiResponse.success(userService.getUser(id)));
    } catch (CustomException e) {
        // 금지: Controller는 에러를 직접 처리하지 않는다
        return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
    }
}

// Service에서 ApiResponse 반환 — 금지
public ApiResponse<UserResponse> getUser(Long id) { // 금지: Service 반환 타입에 ApiResponse 사용
    ...
}

// Controller에서 ErrorResponse 직접 생성 — 금지
@GetMapping("/{id}")
public ResponseEntity<ErrorResponse> getUser(@PathVariable Long id) { // 금지
    return ResponseEntity.notFound()...
}

// 도메인 에러에 공통 접두사 사용 — 금지
COMMON_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "C010", ...) // 금지: 도메인 에러는 C 접두사 금지

// 모호한 ErrorCode 이름 — 금지
USER_ERROR(HttpStatus.BAD_REQUEST, "U001", "사용자 오류") // 금지: 어떤 오류인지 불명확
```

---

## 체크리스트

새 기능을 구현하거나 기존 코드를 수정할 때 확인한다.

- [ ] Controller 반환 타입이 `ResponseEntity<ApiResponse<T>>` 또는 `ResponseEntity<Void>`인가
- [ ] Service/Domain 레이어가 `ApiResponse`나 `ErrorResponse`를 반환하지 않는가
- [ ] 비즈니스 예외가 `CustomException`으로 던져지고 있는가
- [ ] 새로 추가한 `ErrorCode`의 접두사가 도메인 기준에 맞는가
- [ ] 새 `ErrorCode`의 번호가 기존 번호와 중복되지 않는가
- [ ] 에러 처리 로직이 `GlobalExceptionHandler` 이외의 곳에 없는가

## 금지 사항

- `ApiResponse.failure()` 등 존재하지 않는 팩토리 메서드를 추가하거나 호출하지 않는다.
- Controller에서 `ApiResponse<T>`를 단독 반환 타입으로 사용하지 않는다.
- Controller에서 `try-catch`로 `CustomException`을 잡아 직접 처리하지 않는다.
- `ErrorResponse`를 Controller 반환 타입에 쓰지 않는다.
- 도메인 고유 에러에 `C` 접두사를 붙이지 않는다.
- 한번 발행한 에러 코드 번호(`C001`, `A001` 등)를 삭제하거나 재사용하지 않는다.
- `record`인 `ApiResponse`, `ErrorResponse`에 setter나 mutable 필드를 추가하지 않는다.
