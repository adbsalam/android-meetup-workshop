# Compose Multiplatform Memory Match Workshop

This repository contains a two-player memory game for Android and the browser. Both hosts call the shared `App()` composable from `shared/src/commonMain`.

The deck has 20 shuffled cards with 10 matching pairs. A match earns one point and keeps the turn. A mismatch shows red feedback for one second, then passes the turn.

## Workshop branches

- `workshop-starter-v2` contains the playable game and two Android-only sample images.
- `workshop-solution-v2` starts from the starter and adds shared card artwork, browser Escape support, browser time-zone data, and this guide.

Use the starter branch for the workshop. Compare its code and history with the solution branch.

## Architecture

| Module or path | Purpose |
| --- | --- |
| `androidApp` | Android host. `MainActivity` calls the shared `App()`. |
| `webApp` | JS and Wasm browser hosts. `ComposeViewport` calls the shared `App()`. |
| `shared/src/commonMain` | Shared Compose UI, game rules, MVI state, and session time. |
| `shared/src/commonTest` | Platform-independent game and ViewModel tests. |
| `shared/src/commonMain/composeResources/drawable` | Shared card fronts and the card back. |
| `webApp/src/jsMain` | JS time-zone module import. |
| `webApp/src/wasmJsMain` | Wasm time-zone module import. |
| `kotlin-js-store` | Yarn lockfiles for JS and Wasm packages. |

`MainActivity` in `androidApp` calls `App()` from `setContent`. The browser entry point calls the same composable inside `ComposeViewport`.

The shared `MemoryMatchViewModel` owns immutable UI state in a `StateFlow`. The UI sends sealed intents to the ViewModel. Android retains this state across configuration changes.

The root UI uses `Modifier.safeContentPadding()` and `fillMaxSize()`. The browser host uses `ComposeViewport` and full-size page styles.

## Game features

- Two players enter names. Blank names become `Player 1` and `Player 2`.
- The deck has 10 technology pairs and 20 shuffled cards.
- A match adds one point, keeps the same player, and shows green feedback for one second.
- A mismatch shows red feedback for one second. The cards then turn down, and the next player takes a turn.
- The game blocks card input while feedback is visible. A fixed-height status area prevents the board from moving.
- The board adapts to the available width and uses no more than six columns. Each card keeps a square shape and flips in 3D.
- The end screen shows the winner or draw, both scores, the local start time, the time-zone ID, and elapsed time.
- Android Back and browser Escape open the same `Leave game?` dialog.
- The welcome screen has a `DropdownMenu` with plain-language rules.
- The solution uses ten supplied card-front images and `card_back.png`. Each card label remains behind its image as text.

## Requirements

- JDK 17 or later
- Android Studio or IntelliJ IDEA with the Kotlin Multiplatform plugin
- Android SDK platform API 37
- A browser with Wasm Garbage Collection support:
  - Chrome or Chromium 119 or later
  - Firefox 120 or later
  - Safari or WebKit 18.2 or later

Kotlin/Wasm requires browser support for Wasm Garbage Collection.

## Run the app

Build the Android app:

```bash
./gradlew :androidApp:assembleDebug
```

Run the Wasm development app:

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

Run the Wasm production app:

```bash
./gradlew :webApp:wasmJsBrowserProductionRun
```

## Run tests and builds

Run the shared game tests on JS and Wasm:

```bash
./gradlew :shared:jsTest :shared:wasmJsTest
```

Run the browser time-zone tests:

```bash
./gradlew :webApp:jsBrowserTest :webApp:wasmJsBrowserTest
```

Build the Wasm browser distribution:

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentExecutableDistribution
```

## Workshop exercises

1. Trace how `androidApp` and `webApp` call `App()` in `shared/src/commonMain`.
2. Read `MemoryMatchViewModel.kt` and `MemoryMatchGame.kt`. Trace state changes from an intent to the rendered UI.
3. Review the Compose imports and Gradle source sets. The `androidx.compose.*` prefix alone does not identify platform support.
4. On the starter branch, find `kotlin_logo.png` and `gdg_logo.png` in Android resources. Move them to the shared Compose resources.
5. Add the other card fronts and `card_back.png` to `shared/src/commonMain/composeResources/drawable`. Use the generated `Res.drawable.*` accessors.
6. Compare the browser time-zone ID on both branches. Read the JS and Wasm module imports and compare both Yarn lockfiles.
7. Use Android Back and browser Escape. Both actions must open the same exit dialog during play.
8. Run the JS and Wasm tests, then build the Android and Wasm apps.

## Key gotchas

### Compose imports

The `androidx.compose.*` prefix does not show whether an API supports `commonMain`. Make sure that the Gradle artifact and source set support the API.

Keep Android framework APIs, such as `android.os` and `android.widget`, out of `commonMain`.

### Shared resources

Compose Multiplatform does not use Android `R.drawable.*` accessors for shared images. It generates `Res.drawable.*` accessors from files in `composeResources/drawable`.

Use lowercase filenames. After you add or rename an image, run a Gradle sync or build before you use its generated accessor.

### Browser time zones

The version catalog adds `kotlinx-datetime` to `commonMain`. The browser targets also need the npm package `@js-joda/timezone`.

The JS and Wasm targets use separate `@JsModule` declarations. Gradle stores their package locks in `kotlin-js-store/yarn.lock` and `kotlin-js-store/wasm/yarn.lock`.

After you change an npm dependency, update both lockfiles:

```bash
./gradlew kotlinUpgradeYarnLock kotlinWasmUpgradeYarnLock
```

Commit both lockfiles so that other builds use the same package versions.

### Browser layout and input

The web host uses `ComposeViewport`. The shared root uses `safeContentPadding()` and fills its host.

The same `OutlinedTextField` code runs on Android and in the browser. Android can show a software keyboard. Desktop browser users usually type with a hardware keyboard.

### Web stability

Compose Multiplatform for Web and Kotlin/Wasm remain in beta. Read current browser support and stability notes before a production decision.

Use Kotlin Slack channels `#compose-web` and `#multiplatform` for community support.

## References

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Kotlin/Wasm documentation](https://kotlinlang.org/docs/wasm-overview.html)
- [Compose Multiplatform documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform.html)
- [Compose Multiplatform resources](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html)
- [`TimeZone.currentSystemDefault`](https://kotlinlang.org/api/kotlinx-datetime/kotlinx-datetime/kotlinx.datetime/-time-zone/-companion/current-system-default.html)
- [Kotlin Slack](https://kotlinlang.slack.com/)
