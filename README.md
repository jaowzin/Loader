# Carrom Loader

Carrom Loader is a CTF-focused Android host with an integrated virtualization runtime for running the Carrom target inside a controlled virtual process.

## Runtime

The runtime is vendored directly into this repository and maintained alongside the Carrom-specific integration code.

Integrated modules:

- `runtime-core`
- `runtime-bridge`
- `runtime-codegen`

The Loader application uses the runtime API to clone the installed `com.miniclip.carrom` package into virtual user 0 and launch it inside the controlled environment.

## Current milestone

1. Initialize the Carrom runtime from the Loader Application.
2. Clone the already-installed `com.miniclip.carrom` package into virtual user 0.
3. Launch the virtual Carrom instance.
4. Observe the virtual Application lifecycle through the Loader-owned bridge.
5. Extend the Carrom-specific module layer for CTF trajectory and line experiments.

## Project layout

- `app` — Carrom Loader application and Carrom-specific integration.
- `runtime-core` — virtualization runtime core.
- `runtime-bridge` — reflection/runtime compatibility bridge.
- `runtime-codegen` — generated runtime bindings.
- `licenses` — licenses required by incorporated dependencies.

## Build

GitHub Actions installs Android SDK 35 + NDK 29.0.13846066 and builds `:app:assembleDebug` with JDK 21 / Gradle 8.14.5.
