# 시작하기

## 요구사항

| 요구사항 | 버전 |
|---------|------|
| Java | 21+ |
| Spring Boot | 3.2+ |
| Notion Integration | [여기서 생성](https://www.notion.so/my-integrations) |

### Notion 설정

1. [Notion Integrations](https://www.notion.so/my-integrations)에서 새 통합을 생성합니다.
2. **Internal Integration Secret**을 복사합니다 (`ntn_` 또는 `secret_`으로 시작).
3. API 문서 데이터베이스를 생성할 Notion 페이지를 엽니다.
4. 우측 상단 **"..."** > **"연결 추가"** > 생성한 통합을 선택합니다.
5. 페이지 URL에서 **Page ID**를 복사합니다:
   ```
   https://www.notion.so/Your-Page-Title-<PAGE_ID>
                                          ^^^^^^^^
                                          이 부분을 복사
   ```

---

## 설치

### Gradle

```groovy
dependencies {
  implementation 'io.github.lgwk42:notion-docs:1.1.0'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.lgwk42</groupId>
    <artifactId>notion-docs</artifactId>
    <version>1.1.0</version>
</dependency>
```
---

## 설정

`application.yml`에 다음을 추가합니다:

```yaml
notion-docs:
  api-token: "ntn_your_integration_token"
  parent-page-id: "your-notion-page-id"
```

### 전체 설정 항목

```yaml
notion-docs:
  enabled: true                          # 라이브러리 활성화 여부 (기본값: true)
  api-token: "ntn_..."                   # [필수] Notion Integration 토큰
  parent-page-id: "abc123..."            # [필수] 데이터베이스가 생성될 부모 페이지 ID
  database-title: "API Documentation"    # 데이터베이스 제목 (기본값: "API Documentation")
  sync-on-startup: true                  # 애플리케이션 시작 시 동기화 (기본값: true)
  archive-removed-endpoints: false       # 삭제된 엔드포인트의 DB 행을 보관 처리 (기본값: false)
  include-packages:                      # 이 패키지만 스캔 (비어있으면 전체 스캔)
    - "com.example.api"
  exclude-packages:                      # 스캔에서 제외할 패키지
    - "com.example.internal"
  default-domain: ""                     # @NotionDoc이 없을 때 기본 도메인
  default-auth: []                       # @NotionDoc이 없을 때 기본 접근 권한
  notion-api-version: "2022-06-28"       # Notion API 버전
  connect-timeout: 5s                    # HTTP 연결 타임아웃
```

---

## 사용법

### 기본 사용 (코드 변경 없이)

위 설정만으로 라이브러리가 시작 시 모든 `@RestController` 클래스를 자동 스캔하여 Notion 데이터베이스를 생성합니다.

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) { ... }

    @PostMapping
    public UserResponse createUser(@RequestBody CreateUserRequest request) { ... }
}
```

생성 결과:

| API | URI | METHOD | AUTH | DOMAIN |
|-----|-----|--------|------|--------|
| getUser | /api/v1/users/{id} | GET | | |
| createUser | /api/v1/users | POST | | |

### @NotionDoc 어노테이션 사용

`@NotionDoc`을 사용하면 더 풍부한 메타데이터를 제공할 수 있습니다:

```java
@NotionDoc(domain = "USER")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @NotionDoc(
        name = "사용자 조회",
        description = "ID로 사용자를 조회합니다",
        auth = {"USER", "ADMIN"}
    )
    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) { ... }

    @NotionDoc(
        name = "사용자 생성",
        description = "새 사용자 계정을 생성합니다",
        auth = {"ADMIN"}
    )
    @PostMapping
    public UserResponse createUser(@RequestBody @Valid CreateUserRequest request) { ... }
}
```

생성 결과:

| API | URI | METHOD | AUTH | DOMAIN | PROGRESS |
|-----|-----|--------|------|--------|----------|
| 사용자 조회 | /api/v1/users/{id} | GET | USER, ADMIN | USER | Not Started |
| 사용자 생성 | /api/v1/users | POST | ADMIN | USER | Not Started |

#### @NotionDoc 옵션

| 속성 | 적용 레벨 | 설명 |
|------|----------|------|
| `name` | 메서드 | API 표시 이름. 미지정 시 메서드 이름 사용. |
| `description` | 메서드 | 상세 페이지에 표시되는 API 설명. |
| `domain` | 클래스 / 메서드 | 도메인 카테고리. 클래스 레벨은 전체 메서드에 적용되며, 메서드 레벨이 우선. |
| `auth` | 메서드 | 접근 권한 배열 (예: `{"ALL"}`, `{"USER", "ADMIN"}`). |
| `responses` | 메서드 | 응답 케이스 배열. 비어있으면 리턴 타입에서 자동 추론. |

### @Response 옵션

| 속성 | 설명 |
|------|------|
| `status` | HTTP 상태 코드 (기본값: 200) |
| `description` | 응답 케이스 설명 |
| `body` | 응답 본문 클래스. 본문 없음 시 `void.class`. |

#### 사용 예시

```java
@NotionDoc(
    name = "회원 조회",
    description = "ID로 회원을 조회합니다",
    auth = {"USER", "ADMIN"},
    responses = {
        @Response(status = 200, description = "정상 조회", body = MemberResponse.class),
        @Response(status = 404, description = "회원 없음", body = ErrorResponse.class),
        @Response(status = 403, description = "권한 없음")
    }
)
@GetMapping("/{id}")
public BaseResponseData<MemberResponse> getMember(@PathVariable Long id) { ... }
```

> **참고:** `BaseResponseData<T>` 같은 제네릭 래퍼 타입이 완전히 지원됩니다.
> 라이브러리가 타입 파라미터 `T`를 실제 타입으로 리졸빙하여 중첩 필드까지 재귀적으로 추출합니다.

---

## 생성되는 페이지 예시

데이터베이스 행을 열면 다음과 같은 상세 페이지가 표시됩니다:

### POST /api/v1/users (사용자 생성)

```
## Description
새 사용자 계정을 생성합니다

## Request
### Request Body
  ```json
  {
    "email": "string",
    "password": "string",
    "profile": {
      "nickname": "string",
      "age": 0
    }
  }
  ```
  | Parameter          | Type    | Description |
  |--------------------|---------|-------------|
  | email              | String  | (required)  |
  | password           | String  | (required)  |
  | profile            | Object  | (optional)  |
  | profile.nickname   | String  | (optional)  |
  | profile.age        | int     | (optional)  |

## Response
  ```json
  {
    "id": 0,
    "email": "string",
    "createdAt": "2026-01-01T00:00:00"
  }
  ```
  | Parameter  | Type           | Description |
  |------------|----------------|-------------|
  | id         | Long           | (optional)  |
  | email      | String         | (optional)  |
  | createdAt  | LocalDateTime  | (optional)  |
```

### 다중 응답 예시

`@Response` 어노테이션을 사용하면 각 응답 케이스가 개별적으로 렌더링됩니다:

```
## Response
### 200 정상 조회
  ```json
  {
    "id": 0,
    "email": "string"
  }
  ```
  | Parameter | Type   | Description |
  |-----------|--------|-------------|
  | id        | Long   | (optional)  |
  | email     | String | (optional)  |

### 404 회원 없음
  ```json
  {
    "code": "string",
    "message": "string"
  }
  ```
  | Parameter | Type   | Description |
  |-----------|--------|-------------|
  | code      | String | (optional)  |
  | message   | String | (optional)  |

### 403 권한 없음
  No response body

## 중첩 배열 예시
배열 필드가 포함된 엔드포인트에서는 `[]` 표기법이 사용됩니다:

  | Parameter          | Type         | Description |
  |--------------------|--------------|-------------|
  | items              | List<Item>   | (required)  |
  | items[].id         | Long         | (optional)  |
  | items[].name       | String       | (optional)  |
  | items[].tags       | List<String> | (optional)  |

---

## 변경 감지

라이브러리는 SHA-256 해시를 사용하여 변경 사항을 감지합니다. 동기화 시:

- **새 엔드포인트**: 새 데이터베이스 행과 상세 페이지가 생성됩니다.
- **변경된 엔드포인트**: 프로퍼티와 페이지 본문이 업데이트됩니다. PROGRESS 컬럼은 기존 값이 유지됩니다.
- **변경 없는 엔드포인트**: 건너뜁니다.
- **삭제된 엔드포인트**: 옵션에 따라 보관 처리됩니다 (`archive-removed-endpoints: true`).
