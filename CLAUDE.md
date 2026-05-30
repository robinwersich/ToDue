# CLAUDE.md

## Project Overview

ToDue is an Android app — a hybrid between a todo list and calendar for planning days and managing tasks. Built with Jetpack Compose, focusing on non-standard UI patterns.

## Tech Stack

- **Language:** Kotlin (JVM target 1.8)
- **UI:** Jetpack Compose with Material 3
- **Database:** Room (with KSP)
- **State:** MVVM + sealed event interfaces (MVI-style), StateFlow
- **DI:** Manual container (`AppDataContainer`), no framework
- **Collections:** kotlinx-collections-immutable for Compose stability
- **Date/Time:** ThreeTen Extra + desugared java.time APIs
- **Formatting:** ktfmt (Google style)

## Build Commands

```bash
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease        # Release build
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests
```

## Architecture

Clean Architecture with three layers:

- **domain/** — Models, repository interfaces, business logic. No Android dependencies.
- **data/** — Room database, DAOs, repository implementations, entities.
- **ui/** — Compose screens, ViewModels, theme, custom Compose extensions.
- **utility/** — Shared extension functions and helpers.

Package root: `com.robinwersich.todue`

## Code Conventions

- ktfmt Google style formatting
- `sealed interface` for event types (e.g., `OrganizerEvent`)
- Data classes with `val` properties; immutable collections for Compose stability
- Composable functions take `Modifier` as trailing parameter with default `Modifier`
- ViewModels expose `StateFlow`, collected with `collectAsStateWithLifecycle`
- Repository interfaces in `domain/`, implementations in `data/`
- Extension functions preferred for utility code
- Custom operator overloading for domain types (TimeBlock ranges, date arithmetic)

## Key Domain Concepts

- **TimeBlock** — Day, Week, Month with range operators and arithmetic
- **Task** — Core todo item with due dates
- **Timeline** — Configurable time-based view of tasks
- **NavigationState** — Custom navigation (not NavController)

See [docs/organizer-architecture.md](docs/organizer-architecture.md) for the full organizer architecture: domain model type hierarchy, navigation state mechanics, split-view rendering, and data flow.

## Testing

- Unit tests: JUnit 4 + Google Truth assertions + kotlin-test
- UI tests: Compose UI Test + Espresso
- Test files mirror source structure under `test/` and `androidTest/`
