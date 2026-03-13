# Contributing to Notion Docs

[🇰🇷 한국어 버전](docs/ko/contributing.md)

Thank you for your interest in contributing! This guide will help you get started.

## How to Contribute

1. **Fork** the repository
2. **Create** a feature branch from `main`
3. **Make** your changes
4. **Submit** a pull request

## Development Setup

### Prerequisites

| Requirement | Version |
|-------------|---------|
| JDK | 21+ |
| Gradle | 8.x (wrapper included) |
| Spring Boot | 3.2+ (test dependency) |

### Local Setup

```bash
git clone https://github.com/<your-fork>/notion-docs.git
cd notion-docs
./gradlew build
```

To run tests:

```bash
./gradlew test
```

> **Note:** Tests use MockWebServer — no Notion API key is required for development.

## Branch Naming

| Prefix | Purpose | Example |
|--------|---------|---------|
| `feature/` | New feature | `feature/add-status-column` |
| `fix/` | Bug fix | `fix/null-pointer-on-scan` |
| `docs/` | Documentation | `docs/add-korean-guide` |
| `refactor/` | Code refactoring | `refactor/extract-renderer` |
| `test/` | Test additions | `test/sync-service-coverage` |

## Commit Convention

We use **Gitmoji** for commit messages.

```
<emoji> <short description>
```

### Common Gitmoji

| Emoji | Code | Usage |
|-------|------|-------|
| ✨ | `:sparkles:` | New feature |
| 🐛 | `:bug:` | Bug fix |
| ♻️ | `:recycle:` | Refactor |
| 📝 | `:memo:` | Documentation |
| ✅ | `:white_check_mark:` | Add/update tests |
| 🔧 | `:wrench:` | Configuration |
| 🚚 | `:truck:` | Move/rename files |
| 🔥 | `:fire:` | Remove code/files |

### Examples

```
✨ add PROGRESS column to database schema
🐛 fix NPE when scanning controllers without @RequestMapping
♻️ extract block building logic to NotionBlockBuilder
📝 update getting-started guide with Maven coordinates
```

## Pull Request Process

1. Ensure all tests pass (`./gradlew test`)
2. Ensure the build succeeds (`./gradlew build`)
3. Fill out the PR template with a clear description
4. Link any related issues
5. Wait for review — maintainers will respond within a few days

### PR Guidelines

- Keep PRs focused — one feature or fix per PR
- Add tests for new functionality
- Update documentation if behavior changes
- Do not include unrelated formatting changes

## Code Style

- Follow standard Java conventions
- Use meaningful variable and method names
- Keep methods short and focused
- Add Javadoc for public APIs
- Use `record` for immutable data classes where appropriate

## Reporting Issues

When reporting a bug, please include:

- Java and Spring Boot version
- Notion API version
- Minimal reproduction steps
- Expected vs actual behavior
- Stack trace (if applicable)

For feature requests, describe the use case and expected behavior.

## Testing

- All new features must include unit tests
- Use `MockWebServer` for Notion API interaction tests
- Test both success and error paths
- Run the full test suite before submitting a PR

## License

By contributing, you agree that your contributions will be licensed under the [MIT License](LICENSE).
