# Notes: injectable Logcat logger (`AppLogger`)

## What was added
| Layer | File | Role |
|---|---|---|
| domain | `domain/logging/AppLogger.kt` | Interface `d / i / w / e`, no Android imports |
| util | `util/LogcatAppLogger.kt` | Implementation backed by `android.util.Log` |
| di | `di/LoggingModule.kt` (+ `AppComponent`) | `@Binds @Singleton AppLogger ← LogcatAppLogger` |
| ui | `PlayerViewModel` | Logs user actions and timer end (tag `PlayerViewModel`) |
| ui | `MainActivity` → `MainScreen(logger = …)` | Logs screen changes (tag `MainScreen`) |
| test | `fake/FakeAppLogger.kt`, `PlayerViewModelTest` | Records entries, 6 new logging tests |

Logcat filter: `tag:PlayerViewModel | tag:MainScreen`.

## Design choices
- **Interface in `domain/`, implementation in `util/`.** `android.util.Log` throws
  "Method not mocked" in JVM unit tests. With an interface, the ViewModel tests use a hand-written
  fake, and the ViewModel itself has no Android logging code.
- **`@Singleton` in its own `LoggingModule`.** The logger keeps no state, so one shared instance is
  enough, and being app-scoped makes it safe to inject into ViewModels (CLAUDE.md rule). It isn't a
  repository, so it didn't belong in `RepositoryModule`.
- **What gets logged:**
  - In the ViewModel, at info level: duration selected, cancel, OK, timer finished.
  - In the ViewModel, at warning level: a rejected duration.
  - Never on each countdown tick, which would write a line every second.
- **Compose logs screen changes, not values.** `LaunchedEffect(uiState::class.simpleName)` writes one
  line per Idle / Running / Finished change, not one per recomposition or per tick.
- **`require` became `if` + `throw IllegalArgumentException`,** so the warning is logged before the
  exception. Callers see the same exception type and message as before.

## Rejected alternative
**`CompositionLocal` (`LocalAppLogger`).** It avoids passing the logger down through composables, but
it hides the dependency, and a composable that reads it without a provider crashes at runtime. With
only one screen, a plain `MainScreen(logger = …)` parameter is simpler and checked at compile time.
The stateless `*Content` composables and their previews stay free of the logger.

## Limitations
- **Logs also show in release builds.** Nothing filters by build type yet.
- **`PlaybackTracker` still calls `Log.d` directly.** It's outside this feature, so it was left as is.
- **No test for the Compose logging.** The project has unit tests only, and testing `MainScreen`
  would need Compose UI tests.
- **Tests compare exact message text,** so rewording a log message means updating its test.
- **A failure in `saveLastDuration` isn't logged.** Error handling for saving wasn't part of this
  feature.

## Ideas beyond the spec
- **Turn logging off in release builds.**
  - Option 1: a `NoOpAppLogger` bound from a release-only module.
  - Option 2: check `BuildConfig.DEBUG`, which needs `buildFeatures.buildConfig = true`.
- **Switch `PlaybackTracker` to `AppLogger`.**
- **Move the log messages into constants** shared by the ViewModel and its tests.
- **Log Room errors** (save or read) with `logger.e(tag, msg, throwable)`.