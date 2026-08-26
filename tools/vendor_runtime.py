#!/usr/bin/env python3
from pathlib import Path
import shutil
import sys

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "engine" / "NewBlackbox"

MODULES = {
    "Bcore": "runtime-core",
    "black-reflection": "runtime-bridge",
    "compiler": "runtime-codegen",
}

REPLACEMENTS = [
    ("top.niunaijun.blackreflection", "dev.jaowzin.carromloader.bridge"),
    ("top/niunaijun/blackreflection", "dev/jaowzin/carromloader/bridge"),
    ("top_niunaijun_blackreflection", "dev_jaowzin_carromloader_bridge"),
    ("BlackReflection", "RuntimeBridge"),
    ("blackReflection", "runtimeBridge"),
    ("blackreflection", "runtimebridge"),
    ("black-reflection", "runtime-bridge"),
    ("top.niunaijun.blackbox", "dev.jaowzin.carromloader.runtime"),
    ("top/niunaijun/blackbox", "dev/jaowzin/carromloader/runtime"),
    ("top_niunaijun_blackbox", "dev_jaowzin_carromloader_runtime"),
    ("BLACKBOX", "CARROMRUNTIME"),
    ("BlackBox", "CarromRuntime"),
    ("blackBox", "carromRuntime"),
    ("blackbox", "carromruntime"),
    ("black_box", "carrom_runtime"),
    ("black-box", "carrom-runtime"),
]

# Generated reflection wrappers use the generic package `black.*`. Move that
# implementation detail under our runtime bridge namespace as well.
GENERIC_REPLACEMENTS = [
    ("package black.", "package dev.jaowzin.carromloader.mirror."),
    ("import black.", "import dev.jaowzin.carromloader.mirror."),
    ("black.android.", "dev.jaowzin.carromloader.mirror.android."),
    ("black.com.", "dev.jaowzin.carromloader.mirror.com."),
    ("black.dalvik.", "dev.jaowzin.carromloader.mirror.dalvik."),
    ("black.java.", "dev.jaowzin.carromloader.mirror.java."),
    ("black.libcore.", "dev.jaowzin.carromloader.mirror.libcore."),
]

TEXT_EXTS = {
    ".java", ".kt", ".kts", ".gradle", ".xml", ".aidl", ".cpp", ".cc", ".c", ".h", ".hpp",
    ".mk", ".cmake", ".properties", ".pro", ".txt", ".md", ".json", ".toml", ".sh", ".py"
}
TEXT_NAMES = {"Android.mk", "Application.mk", "CMakeLists.txt", "gradlew", ".gitignore"}


def rewrite_text(path: Path):
    if path.suffix.lower() not in TEXT_EXTS and path.name not in TEXT_NAMES:
        return
    try:
        text = path.read_text(encoding="utf-8")
    except (UnicodeDecodeError, OSError):
        return
    old = text
    for a, b in REPLACEMENTS:
        text = text.replace(a, b)
    for a, b in GENERIC_REPLACEMENTS:
        text = text.replace(a, b)
    if text != old:
        path.write_text(text, encoding="utf-8")


def rename_tree(root: Path):
    # Rename deepest paths first so package directories and Java file names move consistently.
    for p in sorted(root.rglob("*"), key=lambda x: len(x.parts), reverse=True):
        name = p.name
        new = name
        for a, b in REPLACEMENTS:
            new = new.replace(a, b)
        if new != name:
            p.rename(p.with_name(new))

    # Move the old package directory layout after textual renames.
    for base in [root / "src" / "main" / "java", root / "src" / "main" / "aidl"]:
        old = base / "top" / "niunaijun" / "carromruntime"
        if old.exists():
            dest = base / "dev" / "jaowzin" / "carromloader" / "runtime"
            dest.parent.mkdir(parents=True, exist_ok=True)
            if dest.exists():
                shutil.copytree(old, dest, dirs_exist_ok=True)
                shutil.rmtree(old)
            else:
                shutil.move(str(old), str(dest))

        old_bridge = base / "top" / "niunaijun" / "runtimebridge"
        if old_bridge.exists():
            dest = base / "dev" / "jaowzin" / "carromloader" / "bridge"
            dest.parent.mkdir(parents=True, exist_ok=True)
            if dest.exists():
                shutil.copytree(old_bridge, dest, dirs_exist_ok=True)
                shutil.rmtree(old_bridge)
            else:
                shutil.move(str(old_bridge), str(dest))

    # Move generic `black.*` reflection wrapper sources under our mirror namespace.
    java_base = root / "src" / "main" / "java"
    old_black = java_base / "black"
    if old_black.exists():
        dest = java_base / "dev" / "jaowzin" / "carromloader" / "mirror"
        dest.parent.mkdir(parents=True, exist_ok=True)
        if dest.exists():
            shutil.copytree(old_black, dest, dirs_exist_ok=True)
            shutil.rmtree(old_black)
        else:
            shutil.move(str(old_black), str(dest))


def main():
    if not SRC.exists():
        print("engine/NewBlackbox is not present; runtime may already be vendored")
        return 0

    for src_name, dst_name in MODULES.items():
        src = SRC / src_name
        dst = ROOT / dst_name
        if dst.exists():
            shutil.rmtree(dst)
        shutil.copytree(src, dst, symlinks=False)

    # Required upstream Apache-2.0 license is retained outside product UI/source branding.
    license_dir = ROOT / "licenses" / "runtime-engine"
    license_dir.mkdir(parents=True, exist_ok=True)
    shutil.copy2(SRC / "LICENSE", license_dir / "LICENSE")

    for module in MODULES.values():
        root = ROOT / module
        for path in list(root.rglob("*")):
            if path.is_file():
                rewrite_text(path)
        rename_tree(root)
        # One more pass after path moves.
        for path in list(root.rglob("*")):
            if path.is_file():
                rewrite_text(path)

    # Rewrite our integration layer to use the renamed runtime API.
    app_root = ROOT / "app"
    for path in app_root.rglob("*"):
        if path.is_file():
            rewrite_text(path)

    settings = ROOT / "settings.gradle"
    settings.write_text('''pluginManagement {\n    repositories {\n        maven { url "https://www.jitpack.io" }\n        google()\n        mavenCentral()\n        gradlePluginPortal()\n    }\n}\n\ndependencyResolutionManagement {\n    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)\n    repositories {\n        maven { url "https://www.jitpack.io" }\n        google()\n        mavenCentral()\n    }\n}\n\nrootProject.name = "CarromLoader"\ninclude ':app'\ninclude ':runtime-core'\ninclude ':runtime-bridge'\ninclude ':runtime-codegen'\n''', encoding='utf-8')

    build = ROOT / "app" / "build.gradle"
    text = build.read_text(encoding="utf-8").replace("project(':Bcore')", "project(':runtime-core')")
    build.write_text(text, encoding="utf-8")

    print("Vendored engine into runtime-core/runtime-bridge/runtime-codegen")
    return 0


if __name__ == "__main__":
    sys.exit(main())
