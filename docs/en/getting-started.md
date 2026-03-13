# Getting Started

## Requirements

| Requirement | Version |
|------------|---------|
| Java | 21+ |
| Spring Boot | 3.2+ |
| Notion Integration | [Create one here](https://www.notion.so/my-integrations) |

### Notion Setup

1. Go to [Notion Integrations](https://www.notion.so/my-integrations) and create a new integration.
2. Copy the **Internal Integration Secret** (starts with `ntn_` or `secret_`).
3. Open the Notion page where you want the API documentation database to be created.
4. Click **"..."** (top-right) > **"Connect to"** > select your integration.
5. Copy the **Page ID** from the page URL:
   ```
   https://www.notion.so/Your-Page-Title-<PAGE_ID>
                                          ^^^^^^^^
                                          Copy this part
   ```

---

## Installation

### Gradle

```groovy
dependencies {
    implementation 'io.github.lgwk42:notion-docs:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.lgwk42</groupId>
    <artifactId>notion-docs</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## Configuration

Add the following to your `application.yml`:

```yaml
notion-docs:
  api-token: "ntn_your_integration_token"
  parent-page-id: "your-notion-page-id"
```

### Full Configuration Reference

```yaml
notion-docs:
  enabled: true                          # Enable/disable the library (default: true)
  api-token: "ntn_..."                   # [REQUIRED] Notion Integration token
  parent-page-id: "abc123..."            # [REQUIRED] Parent page ID for the database
  database-title: "API Documentation"    # Database title (default: "API Documentation")
  sync-on-startup: true                  # Sync on application startup (default: true)
  archive-removed-endpoints: false       # Archive DB rows for deleted endpoints (default: false)
  include-packages:                      # Only scan these packages (empty = scan all)
    - "com.example.api"
  exclude-packages:                      # Exclude these packages from scanning
    - "com.example.internal"
  default-domain: ""                     # Default domain when @NotionDoc is not present
  default-auth: []                       # Default auth roles when @NotionDoc is not present
  notion-api-version: "2022-06-28"       # Notion API version
  connect-timeout: 5s                    # HTTP connection timeout
```

---

## Usage

### Basic (Zero Code)

With just the configuration above, the library will automatically scan all `@RestController` classes at startup and create a Notion database.

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

This generates:

| API | URI | METHOD | AUTH | DOMAIN |
|-----|-----|--------|------|--------|
| getUser | /api/v1/users/{id} | GET | | |
| createUser | /api/v1/users | POST | | |

### With @NotionDoc Annotation

Use `@NotionDoc` to provide richer metadata:

```java
@NotionDoc(domain = "USER")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @NotionDoc(
        name = "Get User",
        description = "Retrieves a user by ID",
        auth = {"USER", "ADMIN"}
    )
    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) { ... }

    @NotionDoc(
        name = "Create User",
        description = "Creates a new user account",
        auth = {"ADMIN"}
    )
    @PostMapping
    public UserResponse createUser(@RequestBody @Valid CreateUserRequest request) { ... }
}
```

This generates:

| API | URI | METHOD | AUTH | DOMAIN | PROGRESS |
|-----|-----|--------|------|--------|----------|
| Get User | /api/v1/users/{id} | GET | USER, ADMIN | USER | Not Started |
| Create User | /api/v1/users | POST | ADMIN | USER | Not Started |

#### @NotionDoc Options

| Attribute | Level | Description |
|-----------|-------|-------------|
| `name` | Method | API display name. Defaults to method name. |
| `description` | Method | API description shown in the detail page. |
| `domain` | Class / Method | Domain category. Class-level applies to all methods; method-level overrides. |
| `auth` | Method | Access roles array (e.g. `{"ALL"}`, `{"USER", "ADMIN"}`). |

---

## Generated Page Example

When you open a database row, the detail page looks like this:

### POST /api/v1/users (Create User)

```
## Description
Creates a new user account

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

### Nested Array Example

For endpoints with array fields, the `[]` notation is used:

```
  | Parameter          | Type         | Description |
  |--------------------|--------------|-------------|
  | items              | List<Item>   | (required)  |
  | items[].id         | Long         | (optional)  |
  | items[].name       | String       | (optional)  |
  | items[].tags       | List<String> | (optional)  |
```

---

## Change Detection

The library uses SHA-256 hashing to detect changes. On each sync:

- **New endpoints** are created as new database rows with detail pages.
- **Changed endpoints** have their properties and page body updated. The PROGRESS column is preserved.
- **Unchanged endpoints** are skipped entirely.
- **Removed endpoints** are optionally archived (`archive-removed-endpoints: true`).
