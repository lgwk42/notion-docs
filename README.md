# Notion Docs

A Spring Boot auto-configuration library that scans your REST controllers at startup and automatically generates structured API documentation in Notion.

Each endpoint becomes a **database row** with filterable/sortable properties (method, URI, domain, auth roles, progress) and a **detail page** with request/response examples and parameter tables.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.x (Auto-Configuration) |
| Build | Gradle / Maven |
| API | Notion REST API (2022-06-28) |
| HTTP Client | JDK HttpClient |
| JSON | Jackson Databind |
| Validation | Jakarta Validation API |

## How It Works

```
┌─────────────────────────────────────────────────────────┐
│                   Spring Boot Application                │
│                                                          │
│  @RestController ──┐                                     │
│  @RestController ──┼── ApplicationReadyEvent             │
│  @RestController ──┘          │                          │
│                               ▼                          │
│                     ┌─────────────────┐                  │
│                     │  NotionDocSync   │                  │
│                     │    Listener      │                  │
│                     └────────┬────────┘                   │
│                              │ (virtual thread)          │
└──────────────────────────────┼───────────────────────────┘
                               │
              ┌────────────────┼────────────────┐
              ▼                ▼                 ▼
     ┌──────────────┐ ┌──────────────┐ ┌──────────────────┐
     │   Scanner     │ │    Sync      │ │  Notion Client   │
     │               │ │              │ │                   │
     │ • Endpoint    │ │ • Change     │ │ • Create DB       │
     │   scanning    │ │   detection  │ │ • Create/Update   │
     │ • @NotionDoc  │ │   (SHA-256)  │ │   rows            │
     │   metadata    │ │ • DB manager │ │ • Append blocks   │
     │ • DTO field   │ │              │ │                   │
     │   extraction  │ │              │ │                   │
     └──────┬───────┘ └──────┬───────┘ └────────┬──────────┘
            │                │                   │
            ▼                ▼                   ▼
     ┌─────────────────────────────────────────────────────┐
     │                  Notion Render                       │
     │                                                      │
     │  • Database schema & row properties                  │
     │  • Page body blocks (headings, tables, code blocks)  │
     │  • JSON example generation                           │
     │  • Parameter table with dot notation flattening      │
     └─────────────────────────────────────────────────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │    Notion Database   │
                    │                     │
                    │ ┌─────────────────┐ │
                    │ │ API │ URI │ ... │ │
                    │ ├─────┼─────┼─────┤ │
                    │ │ Row → Detail Page│ │
                    │ │ Row → Detail Page│ │
                    │ └─────────────────┘ │
                    └─────────────────────┘
```

### Generated Notion Database Columns

| Column | Type | Description |
|--------|------|-------------|
| API | Title | API display name |
| URI | Rich Text | Endpoint path |
| METHOD | Select | GET / POST / PUT / PATCH / DELETE |
| AUTH | Multi-Select | Access roles (ALL, USER, ADMIN, OWNER, MASTER) |
| DOMAIN | Select | Domain category |
| PROGRESS | Select | Not Started / In Progress / Done |

### Generated Page Body Structure

```
## Description
  API description text

## Request
  ### Request Body          ← POST / PUT / PATCH
    { JSON example }
    | Parameter | Type | Description |

  ### Request Param         ← GET / DELETE (or additional query params)
    | Parameter | Type | Description |

## Response
  { JSON example }
  | Parameter | Type | Description |
```

## Documentation
- [English](docs/en/getting-started.md)
- [Korean](docs/ko/getting-started.md)

