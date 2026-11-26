# Compose Multiplatform Memory Match Workshop Plan

## Aim

This workshop uses a two-player memory game to teach shared Compose Multiplatform development for Android and Kotlin/Wasm.

## Learning outcomes

By the end of the workshop, attendees can:

- Map the `androidApp`, `webApp`, and `shared/src/commonMain` modules.
- Trace both platform hosts to the shared `App()` composable.
- Explain the shared ViewModel, immutable UI state, sealed intents, and `StateFlow`.
- Build platform-agnostic Compose UI with common layouts, state, and safe-content padding.
- Move Android-only images into Compose Multiplatform resources and use generated accessors.
- Add a pure Kotlin dependency to `commonMain` and browser support packages to JS and Wasm targets.
- Run Android and Wasm builds and name the browser requirements for Wasm Garbage Collection.
- Explain the current beta status of Compose Multiplatform for Web and Kotlin/Wasm.

## Agreed game behaviour

- Two players enter names. Blank names become `Player 1` and `Player 2`.
- The game shuffles 10 pairs into a 20-card deck.
- A match adds one point, keeps the current turn, and shows green feedback for one second.
- A mismatch shows red feedback for one second. The cards then turn down, and the next player takes a turn.
- The game blocks card input during feedback. A fixed-height status area prevents layout movement.
- The board adapts to the window and uses no more than six columns. Cards use square shapes and a 3D flip.
- The end screen shows the result, scores, local start time, time-zone ID, and elapsed time.
- Android Back and browser Escape open the same exit dialog during play.

## Branch structure

The parentless workshop root contains the refined game, shared MVI state, and core tests. The starter branch adds Android-only sample images and starter exercises. The solution branch starts from the starter. It adds shared artwork, browser Escape support, time-zone data, and full documentation.

| Branch | Workshop role |
| --- | --- |
| `workshop-starter-v2` | Playable game with two Android-only sample images and implementation exercises. |
| `workshop-solution-v2` | Complete game with ten shared card fronts, one shared back, browser support, and this guide. |

## Workshop sequence

### 1. Read the project structure

1. Find `MainActivity.kt` and the browser `main.kt`.
2. Trace each entry point to `App()` in `shared/src/commonMain`.
3. Identify the Android and browser host code.

Expected result: attendees can explain how two hosts use one shared UI.

### 2. Trace MVI and game rules

1. Read `MemoryMatchViewModel.kt`.
2. Follow a card-selection intent into `MemoryMatchGame.selectCard`.
3. Follow the updated `StateFlow` into the Compose UI.
4. Read the match rules, score rules, and turn rules.

Expected result: attendees can locate UI state, intent handling, and game logic.

### 3. Review common Compose APIs

1. Read the imports in `App.kt`.
2. Compare the common source-set dependencies in `shared/build.gradle.kts`.
3. Identify Android-only APIs that must stay out of `commonMain`.

Expected result: attendees use artifact and source-set support, not package prefixes, to judge platform compatibility.

### 4. Move and load artwork

1. On the starter branch, find `kotlin_logo.png` and `gdg_logo.png` under Android resources.
2. Move the images to `shared/src/commonMain/composeResources/drawable`.
3. Add the supplied card fronts and `card_back.png`.
4. Run Gradle sync or a build, then use the generated `Res.drawable.*` accessors.

Expected result: the same shared artwork appears on Android and Wasm.

### 5. Add browser time-zone support

1. Read the `kotlinx-datetime` entry in `gradle/libs.versions.toml`.
2. Read its `commonMain` dependency in `shared/build.gradle.kts`.
3. Compare the JS and Wasm `@JsModule` declarations.
4. Read the npm dependencies in `webApp/build.gradle.kts`.
5. Run `./gradlew kotlinUpgradeYarnLock kotlinWasmUpgradeYarnLock`.
6. Run the browser time-zone tests.

Expected result: browser time-zone IDs use IANA names instead of `SYSTEM`.

### 6. Compare keyboard behaviour

1. Start a game in the browser.
2. Press Escape and select `Stay`.
3. Press Escape again and select `Leave`.
4. If an Android device or emulator is available, repeat with Android Back.

Expected result: both hosts use the same game-exit state and dialog.

### 7. Build and run each target

1. Run the shared JS and Wasm tests.
2. Build the Android debug app.
3. Run the Wasm browser app in a supported browser.

Expected result: shared game logic and both browser targets build without platform-specific Compose imports.

## Key gotchas

### Compose package names

Many common Compose Multiplatform APIs use the `androidx.compose.*` package. The package prefix does not show whether a dependency supports `commonMain`.

### Generated resources

Shared assets belong in `shared/src/commonMain/composeResources/drawable`. Compose Multiplatform generates the `Res.drawable.*` accessors after Gradle processes the resources.

Android `R.drawable.*` does not provide a shared resource API.

### JS and Wasm dependencies

`kotlinx-datetime` works from common code. Browser time-zone data needs the `@js-joda/timezone` npm package and a target-specific `@JsModule` import.

Keep both Yarn lockfiles under version control. When the npm dependency changes, update both lockfiles.

### Browser and layout support

Kotlin/Wasm needs Wasm Garbage Collection support. The workshop uses Chrome or Chromium 119+, Firefox 120+, and Safari or WebKit 18.2+ as minimum versions.

The shared root uses `Modifier.safeContentPadding()` and `fillMaxSize()`. The browser host uses `ComposeViewport` and page styles that fill the viewport.

Android can show a software keyboard. Desktop browser users usually type with a hardware keyboard.

## Validation commands

Run the Android build:

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

Run the shared game tests:

```bash
./gradlew :shared:jsTest :shared:wasmJsTest
```

Run the browser time-zone tests:

```bash
./gradlew :webApp:jsBrowserTest :webApp:wasmJsBrowserTest
```

Update the JS and Wasm package locks:

```bash
./gradlew kotlinUpgradeYarnLock kotlinWasmUpgradeYarnLock
```

## Production and community notes

Compose Multiplatform for Web and Kotlin/Wasm remain in beta. Read current browser support and stability notes before a production decision.

Use the [Kotlin/Wasm documentation](https://kotlinlang.org/docs/wasm-overview.html) and [Compose Multiplatform documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform.html) for current platform details.

Use Kotlin Slack channels `#compose-web` and `#multiplatform` for community support.
