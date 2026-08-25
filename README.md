# Carrom Loader

Carrom Loader is a CTF-focused Android host that uses the ready-made **NewBlackbox** virtualization engine instead of reimplementing a dual-app runtime.

## Engine

- Upstream: `ALEX5402/NewBlackbox`
- Pinned commit: `89b59836c66f173756a4ae258cf379a957649820`
- License: Apache-2.0 (see `engine/NewBlackbox/LICENSE` after submodule checkout)
- Integrated modules: `Bcore`, `black-reflection`, `compiler`

The upstream engine is included as a Git submodule so its code and history remain clearly separate from the Carrom-specific host code.

## Current milestone

1. Initialize NewBlackbox/Bcore from the Loader Application.
2. Clone the already-installed `com.miniclip.carrom` package into virtual user 0 using `installPackageAsUser`.
3. Launch it using `launchApk`.
4. Observe the Carrom virtual Application lifecycle through a small Loader-owned bridge.

The Carrom line/trajectory module will be added only after the virtual game launch is stable. The Loader configuration intentionally does not enable root hiding, VPN redirection, FLAG_SECURE bypass, or third-party log forwarding.

## Build

GitHub Actions checks out the NewBlackbox submodule, installs Android SDK 35 + NDK 29.0.13846066, and builds `:app:assembleDebug` with JDK 21 / Gradle 8.14.5.
