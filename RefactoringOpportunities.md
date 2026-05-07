# Refactoring Opportunities

This file records the refactoring candidates identified after the Ficha 4 testing phase. Every refactoring should be performed on a dedicated `Refactoring_XXXXXX` branch and validated with `mvn test` before and after the change.

## Ownership

| Member | Branch | Owned areas |
| --- | --- | --- |
| Eduardo `110894` | `Refactoring_110894` | `Game`, `Move`, `PdfExporter`, `GameSummary`, `MoveSummary`, `ShotSummary` |
| Vasco `111331` | `Refactoring_111331` | `Tasks`, `GameHistory` |
| Tiago `123026` | `Refactoring_123026` | `AppLanguage`, `Messages`, `LanguageSupport`, `Main` |

## Candidate backlog

| ID | Owner | Area | Candidate | Motivation | Related issue | Status |
| --- | --- | --- | --- | --- | --- | --- |
| R1 | Vasco | `GameHistory` | Replace duplicated `printStackTrace()` handling with a shared error-reporting strategy | Error handling is repeated in the constructor, `saveGame()` and `getHistory()` | `#23` | Done on `Refactoring_111331` |
| R2 | Eduardo | `Game` | Simplify `randomEnemyFire()` by extracting candidate filtering and shot selection | The method mixes filtering, random selection, console output and fallback padding in one block | `#21` | Done on `Refactoring_110894` |
| R3 | Eduardo | `Game` / `Move` | Centralize Jackson configuration used by `jsonShots()` and `processEnemyFire()` | JSON serialization currently recreates and configures `ObjectMapper` instances repeatedly | `#20` | Done on `Refactoring_110894` |
| R4 | Vasco | `Tasks` | Break `menu()` into command handlers | The command loop concentrates many responsibilities and is the most complex CLI method in the project | `#39` | Done on `Refactoring_111331` |
| R5 | Eduardo | `Game` | Extract summary-building helpers from `createSummary()` and `buildMoveSummary()` | Summary generation is correct but dense, which makes future changes harder to isolate | - | Done on `Refactoring_110894` |
| R6 | Eduardo | `PdfExporter` | Consolidate repetitive PDF line-writing steps into small section helpers | The exporter is stable but still writes every section procedurally in a single method | - | Done on `Refactoring_110894` |
| R7 | Tiago | `Messages` / `AppLanguage` | Extract and centralize default-language resolution paths | Language fallback logic is simple but duplicated conceptually across localization entry points | - | Pending |
| R8 | Tiago | `LanguageSupport` / `Main` | Isolate CLI language parsing from application startup | Entry-point concerns and language resolution can be made more explicit and easier to test | - | Pending |
| R9 | Vasco | `Tasks` | Extract history-printing and fleet-loading helpers from the CLI loop | `menu()` still mixes input orchestration with printing and persistence concerns | `#39` | Done on `Refactoring_111331` |
| R10 | Eduardo | `Move` | Split verbose text construction from JSON response construction | `processEnemyFire()` currently handles counting, message construction and serialization in one method | - | Done on `Refactoring_110894` |

## Execution rules

1. Stay inside the owned areas unless the team explicitly reassigns ownership.
2. Prefer IntelliJ refactoring operations where possible so the history looks natural and safe.
3. Apply between `5` and `10` refactorings per member, with different refactoring types.
4. Run `mvn test` before the first change and after every meaningful refactoring step.
5. Do not change external behavior; this phase is structural only.
