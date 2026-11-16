# Notes

1. [Injectable Logcat logger (`AppLogger`)](#1-injectable-logcat-logger-applogger)
2. [Playback control and app-scoped `SleepTimer`](#2-playback-control-and-app-scoped-sleeptimer)

# 1. Injectable Logcat logger (`AppLogger`)

## What was added
| Layer | File | Role |
|---|---|---|
| domain | `domain/logging/AppLogger.kt` | Interface `d / i / w / e`, no Android imports |
| util | `util/LogcatAppLogger.kt` | Implementation backed by `android.util.Log` |
| di | `di/LoggingModule.kt` (+ `AppComponent`) | `@Binds @Singleton AppLogger ← LogcatAppLogger` |
| ui | `PlayerViewModel` | Logs user actions and timer end (tag `PlayerViewModel`) |
| ui | `MainActivity` → `MainScreen(logger = …)` | Logs screen changes (tag `MainScreen`) |
| test | `fake/FakeAppLogger.kt`, `PlayerViewModelTest` | Records entries, 6 new logging tests |

Logcat filter (all tags, including section 2):
`tag:PlayerViewModel | tag:MainScreen | tag:SleepTimer | tag:PlaybackController`.

## Design choices
- **Interface in `domain/`, implementation in `util/`.** `android.util.Log` throws
  "Method not mocked" in JVM unit tests. With an interface, the ViewModel tests use a hand-written
  fake, and the ViewModel itself has no Android logging code.
- **`@Singleton` in its own `LoggingModule`.** The logger keeps no state, so one shared instance is
  enough, and being app-scoped makes it safe to inject into ViewModels (CLAUDE.md rule). It isn't a
  repository, so it didn't belong in `RepositoryModule`.
- **What gets logged:**
  - In the ViewModel, at info level: duration selected, cancel, OK, play, pause.
  - "Timer finished" is now logged by `SleepTimer` (tag `SleepTimer`), see section 2.
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
- **No test for the Compose logging.** The project has unit tests only, and testing `MainScreen`
  would need Compose UI tests.
- **Tests compare exact message text,** so rewording a log message means updating its test.
- **A failure in `saveLastDuration` isn't logged.** Error handling for saving wasn't part of this
  feature.

## Ideas beyond the spec
- **Turn logging off in release builds.**
  - Option 1: a `NoOpAppLogger` bound from a release-only module.
  - Option 2: check `BuildConfig.DEBUG`, which needs `buildFeatures.buildConfig = true`.
- **Move the log messages into constants** shared by the ViewModel and its tests.
- **Log Room errors** (save or read) with `logger.e(tag, msg, throwable)`.

# 2. Playback control and app-scoped `SleepTimer`

When the sleep timer reaches zero it pauses playback. The countdown lives outside the ViewModel, so
leaving the screen no longer cancels it.

## What was added
| Layer | File | Role |
|---|---|---|
| domain | `domain/playback/PlaybackController.kt` | Interface: `observeIsPlaying(): Flow<Boolean>`, `play()`, `pause()` |
| domain | `domain/timer/SleepTimer.kt` | `@Singleton` countdown: `state`, `start(totalSeconds)`, `stop()`; pauses playback at zero |
| domain | `domain/timer/SleepTimerState.kt` | `Idle` / `Running(remainingSeconds)` / `Finished`, no UI fields |
| data | `data/playback/InMemoryPlaybackController.kt` | `@Singleton`, keeps `isPlaying` in a `MutableStateFlow` and logs each change |
| di | `di/PlaybackModule.kt` (+ `AppComponent`) | `@Binds PlaybackController ← InMemoryPlaybackController` |
| di | `di/CoroutineModule.kt` (+ `AppComponent`) | `@Singleton CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)` |
| ui | `PlayerViewModel` | Combines `sleepTimer.state` and `isPlaying` into `uiState`; `onPlayClick` / `onPauseClick` |
| ui | `SleepTimerUiState` | `Idle` and `Running` get `isPlaying: Boolean = false` |
| ui | `ui/screen/PlayPauseButton.kt`, `IdleContent` | Stateless Play/Pause button (2 previews), shown on Idle |
| res | `strings.xml` | `playback_play`, `playback_pause` |
| test | `fake/FakePlaybackController.kt` | Records `playCount` / `pauseCount`, exposes `isPlaying` as a flow |
| test | `data/playback/InMemoryPlaybackControllerTest`, `domain/timer/SleepTimerTest` | 3 + 6 tests |
| test | `PlayerViewModelTest` | `createViewModel(...)` helper; playback and "timer outlives the ViewModel" tests |

## Design choices
- **`PlaybackController` is in `domain/playback`, not `domain/repository`.** It sends commands and
  doesn't store data, so it isn't a repository. For the same reason its binding lives in its own
  `PlaybackModule`, like `AppLogger` has `LoggingModule`, instead of in `RepositoryModule`.
- **The playback implementation is in memory.** Real audio would need Media3/ExoPlayer (a new
  dependency) or `MediaPlayer` plus an audio file, which the project doesn't have. The log lines are
  how you see play and pause on a device.
- **The countdown moved from `viewModelScope` into `SleepTimer`.** Before, pressing Back cleared the
  ViewModel, cancelled the countdown, and playback was never paused, which defeats a sleep timer.
  `SleepTimer` is `@Singleton` and runs in an app-wide scope, so a new ViewModel sees the timer
  that is already running.
- **The scope is injected, not created in `SleepTimer`.** Tests pass their own `TestScope`; the app
  gets one from `CoroutineModule` that is never cancelled.
- **The scope uses `Dispatchers.Main.immediate`.** Every timer update runs on the main thread, so
  `stop()` can't race a tick that would set `Running` again or call `pause()` after the stop.
  `delay` doesn't block the thread, so this costs nothing.
- **`CoroutineScope` has no qualifier.** A qualifier declared in `di/` would make `domain` depend
  on `di`. With only one scope in the graph, the plain type is enough.
- **Only a countdown that reaches zero pauses playback.** Cancel and OK call `stop()`, which
  cancels the job before it gets to `pause()`.
- **`InMemoryPlaybackController.play()` / `pause()` do nothing when already in that state.** The
  timer always calls `pause()` at zero, even if Play was never pressed; the early `return` avoids a
  misleading "Playback paused" log, and `play()` has the same guard for symmetry. The guard is in the implementation, so callers (`SleepTimer`, the
  ViewModel) stay simple and `pauseCount` in the fake still counts every call.
- **`isPlaying` is a field on `Idle` and `Running`, not a wrapper state.** The sealed interface and
  `MainScreen`'s `when` stay as they were. The `= false` default kept the existing
  `Idle(null)` / `Running(900)` assertions valid, since the fake starts paused. `Finished` has no
  `isPlaying`, because the timer has just paused playback.
- **Two events, `onPlayClick` / `onPauseClick`, instead of one toggle.** A toggle would make the
  ViewModel read its own state, which can be stale when nothing collects `uiState`. The button only
  picks which callback to call.
- **Tests use the real `SleepTimer`.** It runs in the `TestScope` itself, not in `backgroundScope`,
  because `advanceUntilIdle()` ignores background work and the "finished" tests would never finish.
  A side effect: at the end of a test, `runTest` lets a timer that is still running finish in
  virtual time.
- **`createViewModel(...)` in `PlayerViewModelTest`.** Default arguments and named overrides, so
  each test passes only the fakes it inspects, and a new ViewModel dependency means editing one
  function.

## Limitations
- **No sound.** `InMemoryPlaybackController` only changes a flag and writes a log line.
- **The Play/Pause button is only on the Idle screen.** `Running` still carries `isPlaying`, but
  `RunningContent` doesn't show it.
- **The timer doesn't survive the process being killed.** It outlives the screen, not the app.
- **The first frame always shows `isPlaying = false`.** That's the `stateIn` initial value. When
  something is already playing, the label shows "Play" for one frame, then switches to "Pause".
- **No test for the Compose UI** (`PlayPauseButton`, `IdleContent`); the project has unit tests only.

## Ideas beyond the spec
- **Real audio:** a `MediaPlayer`-backed `PlaybackController` with a file in `res/raw`, or Media3
  if a dependency is approved. Only the implementation and its binding change.
- **A foreground `Service`** that owns `SleepTimer`, so the timer survives the app going to the
  background for a long time.
- **Show the Play/Pause button on the Running screen** again, if wanted.