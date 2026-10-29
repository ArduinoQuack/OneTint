#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/tests
javac --release 8 -d build/tests \
  app/src/main/java/dev/dov/onetint/Palette.java \
  app/src/main/java/dev/dov/onetint/AmoledTheme.java \
  app/src/main/java/dev/dov/onetint/ThemeOptions.java \
  app/src/main/java/dev/dov/onetint/PreviewScene.java \
  tests/PaletteTest.java tests/ThemeOptionsTest.java tests/VolumePreviewTest.java
java -cp build/tests PaletteTest
java -cp build/tests ThemeOptionsTest
java -cp build/tests VolumePreviewTest
if [[ -n "${JSON_JAR:-}" ]]; then
  javac --release 8 -cp "$JSON_JAR" -d build/tests \
    app/src/main/java/dev/dov/onetint/RestoreState.java tests/RestoreStateTest.java
  java -cp "build/tests:$JSON_JAR" RestoreStateTest
fi
if [[ -n "${THEME_PARK_APK:-}" ]]; then
  python3 tests/test_catalog.py "$THEME_PARK_APK"
fi
