# Build and test

Requires JDK 17, Python 3, zip, Android SDK platform 35 and build-tools 35.0.0. There is no Gradle project or third-party Android library dependency.

```sh
export ANDROID_JAR=/path/to/sdk/platforms/android-35/android.jar
export BUILD_TOOLS=/path/to/sdk/build-tools/35.0.0
```

## Signed build

Keep the signing keystore outside the repository. To update an installed OneTint APK, use the same private key that signed it. The public source does not include that key or its password.

```sh
export ONETINT_KEYSTORE=/private/path/onetint.jks
read -rs -p 'Keystore password: ' ONETINT_KEY_PASSWORD
export ONETINT_KEY_PASSWORD
bash build.sh
unset ONETINT_KEY_PASSWORD
```

Output: `build/OneTint.apk`. The build verifies its signature, ZIP contents and alignment. Package `dev.dov.onetint`, version 0.1.11 (code 12), minimum API 34, target API 35. The build uses the keystore's sole signing key; the key password is expected to match the store password.

For a separate personal build, create your own keystore with `keytool`. A differently signed APK cannot be installed over an official or previous personal build. Restore the existing theme before uninstalling an old app to switch signers.

## Compile without a key

```sh
ONETINT_UNSIGNED=1 bash build.sh
```

Output: `build/OneTint-unsigned.apk`. This checks compilation, resource linking and D8 without accessing signing material. It must be signed before installation.

## Host tests

```sh
bash scripts/test.sh
```

Checks palette contrast, user-option parsing, notification mappings, exact popup ARGB, captured apply options, and volume preview number/fill consistency.

Optional restore-state tests use a host JSON implementation. Supply its local JAR; it is not packaged in the APK:

```sh
JSON_JAR=/path/to/json.jar bash scripts/test.sh
```

Optional catalog extraction regression checks require a compatible Theme Park APK you already have:

```sh
THEME_PARK_APK=/path/to/ThemePark.apk bash scripts/test.sh
```

The extractor is `scripts/extract_catalog.py`; the checked-in catalog is sufficient for building. Do not add Samsung APKs to the repository.

## Render the previews

```sh
mkdir -p build/preview-host
javac --release 8 -d build/preview-host \
  app/src/main/java/dev/dov/onetint/Palette.java \
  app/src/main/java/dev/dov/onetint/ThemeOptions.java \
  app/src/main/java/dev/dov/onetint/PreviewScene.java \
  app/src/main/java/dev/dov/onetint/MaterialWifi.java \
  app/src/main/java/dev/dov/onetint/MaterialVolume.java \
  scripts/RenderPreview.java
java -cp build/preview-host RenderPreview 1 2B5666 false > build/quick-panel.svg
```

Arguments: tab (`0` keyboard, `1` Quick Panel, `2` volume), accent RGB, Quick Panel white-icons boolean. Optional arguments: inactive RGB, volume white-icons boolean, notification background/text/icon RGB, volume level and maximum. These are renders of shared preview geometry, not phone screenshots.
