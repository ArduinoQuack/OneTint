#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
: "${ANDROID_JAR:?Set ANDROID_JAR to Android 35 android.jar}"
: "${BUILD_TOOLS:?Set BUILD_TOOLS to Android SDK build-tools directory}"
if [[ "${ONETINT_UNSIGNED:-0}" != 1 ]]; then
    : "${ONETINT_KEYSTORE:?Set ONETINT_KEYSTORE to a private signing keystore}"
    : "${ONETINT_KEY_PASSWORD:?Set ONETINT_KEY_PASSWORD}"
fi
rm -rf build/classes build/dex
mkdir -p build/classes build/dex
"$BUILD_TOOLS/aapt2" compile --dir app/src/main/res -o build/resources.zip
"$BUILD_TOOLS/aapt2" link -o build/base.apk -I "$ANDROID_JAR" --manifest app/src/main/AndroidManifest.xml -A app/src/main/assets build/resources.zip
javac -encoding UTF-8 --release 8 -cp "$ANDROID_JAR" -d build/classes app/src/main/java/dev/dov/onetint/*.java
jar cf build/classes.jar -C build/classes .
"$BUILD_TOOLS/d8" --lib "$ANDROID_JAR" --min-api 34 --output build/dex build/classes.jar
cp build/base.apk build/unsigned.apk
(cd build/dex && zip -q -u ../unsigned.apk classes.dex)
"$BUILD_TOOLS/zipalign" -f 4 build/unsigned.apk build/aligned.apk
if [[ "${ONETINT_UNSIGNED:-0}" == 1 ]]; then
    cp build/aligned.apk build/OneTint-unsigned.apk
    "$BUILD_TOOLS/zipalign" -c 4 build/OneTint-unsigned.apk
    echo "Built build/OneTint-unsigned.apk (not installable until signed)"
    exit 0
fi
rm -f build/OneTint.apk
"$BUILD_TOOLS/apksigner" sign --ks "$ONETINT_KEYSTORE" --ks-pass env:ONETINT_KEY_PASSWORD --out build/OneTint.apk build/aligned.apk
"$BUILD_TOOLS/apksigner" verify --verbose build/OneTint.apk
"$BUILD_TOOLS/zipalign" -c 4 build/OneTint.apk
python3 -c 'import zipfile; z=zipfile.ZipFile("build/OneTint.apk"); assert z.testzip() is None'
