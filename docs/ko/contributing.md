# Notion Docs 기여 가이드

기여에 관심을 가져주셔서 감사합니다! 이 가이드가 시작하는 데 도움이 될 것입니다.

## 기여 방법

1. 저장소를 **Fork** 합니다
2. `main`에서 **브랜치를 생성**합니다
3. 변경 사항을 **작업**합니다
4. **Pull Request**를 제출합니다

## 개발 환경 설정

### 요구사항

| 요구사항 | 버전 |
|---------|------|
| JDK | 21+ |
| Gradle | 8.x (wrapper 포함) |
| Spring Boot | 3.2+ (테스트 의존성) |

### 로컬 설정

```bash
git clone https://github.com/<your-fork>/notion-docs.git
cd notion-docs
./gradlew build
```

테스트 실행:

```bash
./gradlew test
```

> **참고:** 테스트는 MockWebServer를 사용합니다 — 개발에 Notion API 키가 필요하지 않습니다.

## 브랜치 네이밍

| 접두사 | 용도 | 예시 |
|--------|------|------|
| `feature/` | 새 기능 | `feature/add-status-column` |
| `fix/` | 버그 수정 | `fix/null-pointer-on-scan` |
| `docs/` | 문서 | `docs/add-korean-guide` |
| `refactor/` | 코드 리팩토링 | `refactor/extract-renderer` |
| `test/` | 테스트 추가 | `test/sync-service-coverage` |

## 커밋 규칙

**Gitmoji**를 사용합니다.

```
<이모지> <간단한 설명>
```

### 주요 Gitmoji

| 이모지 | 코드 | 용도 |
|--------|------|------|
| ✨ | `:sparkles:` | 새 기능 |
| 🐛 | `:bug:` | 버그 수정 |
| ♻️ | `:recycle:` | 리팩토링 |
| 📝 | `:memo:` | 문서 |
| ✅ | `:white_check_mark:` | 테스트 추가/수정 |
| 🔧 | `:wrench:` | 설정 |
| 🚚 | `:truck:` | 파일 이동/이름 변경 |
| 🔥 | `:fire:` | 코드/파일 삭제 |

### 예시

```
✨ add PROGRESS column to database schema
🐛 fix NPE when scanning controllers without @RequestMapping
♻️ extract block building logic to NotionBlockBuilder
📝 update getting-started guide with Maven coordinates
```

## Pull Request 절차

1. 모든 테스트 통과 확인 (`./gradlew test`)
2. 빌드 성공 확인 (`./gradlew build`)
3. PR 템플릿에 명확한 설명 작성
4. 관련 이슈 연결
5. 리뷰 대기 — 메인테이너가 며칠 내에 응답합니다

### PR 가이드라인

- PR은 하나의 기능 또는 수정에 집중
- 새 기능에는 테스트 추가
- 동작 변경 시 문서 업데이트
- 관련 없는 포매팅 변경 포함 금지

## 코드 스타일

- 표준 Java 컨벤션 준수
- 의미 있는 변수 및 메서드 이름 사용
- 메서드는 짧고 집중적으로 유지
- Public API에 Javadoc 추가
- 불변 데이터 클래스에는 `record` 사용 권장

## 이슈 리포트

버그 리포트 시 다음을 포함해 주세요:

- Java 및 Spring Boot 버전
- Notion API 버전
- 최소 재현 단계
- 예상 동작 vs 실제 동작
- 스택 트레이스 (해당 시)

기능 요청은 사용 사례와 예상 동작을 설명해 주세요.

## 테스트

- 모든 새 기능에는 단위 테스트 필수
- Notion API 테스트에는 `MockWebServer` 사용
- 성공 및 에러 경로 모두 테스트
- PR 제출 전 전체 테스트 스위트 실행

## 라이선스

기여함으로써 귀하의 기여가 [MIT 라이선스](../../LICENSE) 하에 라이선스됨에 동의합니다.
