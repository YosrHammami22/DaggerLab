# DaggerLab — rules for Claude

## Project
Android app in Kotlin, Jetpack Compose, Dagger 2 (no Hilt), KSP, Room, Coroutines/Flow.

## Architecture (Clean Architecture, follow it strictly)
- `domain/` : interfaces and business logic. No Android or Room imports.
- `data/` : implementations (Room entities, DAO, repositories).
- `ui/` : ViewModels and Compose screens. No business logic in composables.
- `di/` : all Dagger wiring.

## Dagger patterns to reuse
- Interface → implementation: `@Binds` in `RepositoryModule` (see existing examples).
- Third-party types (Room): `@Provides` in `DatabaseModule`.
- New ViewModel: `@Inject constructor` + `@Binds @IntoMap @ViewModelKey` in `ViewModelModule`.
- Never inject an Activity-scoped object into a ViewModel.

## Rules
- Do NOT add any new library or Gradle dependency.
- Reuse existing code before creating new classes (`TimeFormatter`, repositories, fakes).
- Every new logic class gets unit tests, in the style of `PlayerViewModelTest`:
  `MainDispatcherRule`, Turbine, hand-written fakes (no mocking library).
- Test packages mirror main source packages.
- Work step by step: do ONE step, show the changes, then STOP and wait for my approval.
- NEVER run `git commit` or `git push`. I commit myself.
- Explain briefly why you made each choice.

## Commands
- Build: `./gradlew :app:assembleDebug`
- Unit tests: `./gradlew :app:testDebugUnitTest`
