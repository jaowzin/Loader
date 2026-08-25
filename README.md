# Carrom Loader v2

Clean-room Android dual-app/runtime research project for the authorized Carrom Pool CTF.

## What v2.0.0 does

- Detects the installed `com.miniclip.carrom` package.
- Copies the base APK and every installed split APK into Loader-private storage.
- Treats that private copy as a virtual package snapshot.
- Selects a compatible ABI from the copied APK set.
- Extracts native `.so` files into the virtual package directory.
- Creates a `DexClassLoader` whose dex and native-library paths point at the imported private copy.
- Resolves `CarromApplication` and `CarromActivity` without executing target lifecycle code.
- Includes a standalone trajectory/reflection solver for the future line module.

## What it does not claim yet

v2.0.0 is the foundation, not a finished VirtualApp replacement. It does **not** yet launch the full Carrom Activity from the copied package. Android component lifecycle, Resources/AssetManager construction, Binder/service/provider virtualization and activity/task routing are the next runtime layers.

The project intentionally does not copy BlackBox, VirtualApp, CarromKing/AimCarrom code, or bypass Play Integrity / third-party licensing.

## Runtime roadmap

1. Private base/split import + native extraction + isolated classloader — **v2.0**.
2. Resources/AssetManager built from the imported APK set.
3. Virtual `Application` context and lifecycle.
4. Host Activity/task routing for the imported `CarromActivity`.
5. Carrom module bridge and calibrated trajectory renderer.
6. CTF gameplay solver/automation layer after the virtual runtime is stable.

## Build

The repository uses GitHub Actions and Android Gradle Plugin 8.7.3 with compile/target SDK 35.
