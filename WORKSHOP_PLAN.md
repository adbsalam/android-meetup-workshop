# Compose Multiplatform Memory Match Workshop Plan

## Aim

The app currently exists only in the Android module. This workshop migrates the app to Kotlin Multiplatform (KMP).

The migration adds a shared module, a web app, shared UI, a shared view model, platform-specific implementations, and
shared tests.

## Learning outcomes

By the end of the workshop, attendees can:

- Enable the `shared` and `webApp` modules and run a Hello World Wasm app.
- Move Compose UI from the Android module into shared code.
- Move image assets into `composeResources` and use the generated resource accessors.
- Move the view model into shared code and add the dependencies that it needs.
- Use `expect` and `actual` for platform-specific BackHandler and time-zone implementations.
- Move shared behaviour tests into the shared module.

## Agreed game behaviour

- Two players enter names. Blank names become `Player 1` and `Player 2`.
- The game shuffles 10 pairs into a 20-card deck.
- A match adds one point, keeps the current turn, and shows green feedback for one second.
- A mismatch shows red feedback for one second. The cards then turn down, and the next player takes a turn.
- The game blocks card input during feedback. A fixed-height status area prevents layout movement.
- The board adapts to the window and uses no more than six columns. Cards use square shapes and a 3D flip.
- The end screen shows the result, scores, local start time, time-zone ID, and elapsed time.
- Android Back and browser Escape open the same exit dialog during play.

## Migration starting point

The app starts in the Android module. The workshop adds the `shared` and `webApp` modules and moves code into shared
source sets one step at a time.

## Workshop sequence

### 1. Enable the shared module and web app

Set up the project so that it can build a Kotlin/Wasm web app.

1. Add `shared` and `webApp` to `settings.gradle.kts`.
2. Update `shared/build.gradle.kts` for the shared module.
3. Update `webApp/build.gradle.kts` for the web app.
4. Create a Hello World Wasm app.
5. Run the Wasm app in a browser.

Expected result: the project contains `shared` and `webApp`, and the Hello World Wasm app runs in a browser.

### 2. Migrate the UI to the shared module

Move the Compose UI from the Android module into shared code.

1. Move the UI into the shared module.
2. Comment out unresolved references.
3. Compile the app and keep it in a working state.
4. Move all image assets to `composeResources`.
5. Update image references to use the generated Compose Multiplatform resources.

Expected result: the UI runs from shared code, and the image assets load on Android and in the web app.

### 3. Migrate the view model

Move the view model into the shared module and resolve the errors that appear.

1. Move the view model into shared code.
2. Compile the project.
3. Read each unresolved reference.
4. Add the required view model dependencies.
5. Resolve the remaining errors.

Expected result: the shared UI uses the shared view model on Android and in the web app.

### 4. Add platform-specific implementations

Use `expect` and `actual` declarations for APIs that differ between platforms.

1. Add a shared `expect` declaration for `BackHandler`.
2. Add the Android `actual` implementation.
3. Add the web app `actual` implementation.
4. Add a shared `expect` declaration for time-zone access.
5. Add the Android and web app `actual` implementations.
6. Connect the implementations to the shared UI.

Expected result: BackHandler and time-zone access work on each platform.

### 5. Migrate the tests

Move tests for shared behaviour into the shared module.

1. Move common tests into `shared/src/commonTest`.
2. Keep platform-specific tests in their platform source sets.
3. Run the shared tests.
4. Run the Android tests.
5. Run the web app tests.

Expected result: shared behaviour is tested from the shared module, and platform-specific behaviour remains covered by
platform tests.

Raise questions before moving to the next step when a reference, dependency, or platform API is unclear.

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
