# OneTint

Change the colors of Samsung Keyboard, Quick Panel and the volume panel. Root required.

## What you can change

- Main accent color, using a color wheel or hex code
- Keyboard keys, letters, pressed key, pop-up and background
- Active Quick Panel bubbles and panel background
- Quick Panel icons: black or white
- Inactive Quick Panel bubbles
- Notification background, text and icon colors
- Volume fill, track and black/white icons

By default, the keyboard background is AMOLED black, normal keys are dark, pressed keys use a dimmed accent, and the key pop-up is translucent gray (`#80171717`) with white text. Named color swatches sit below each preview, or beside the volume popup. Tap a swatch to open its color wheel, hex field and opacity slider. Reset a swatch to its default, or use Reset beside a section heading to reset that section. Custom values take priority over the main accent. OneTint's app icon also follows the applied accent.

## Install

Download the APK from [Releases](https://github.com/ArduinoQuack/OneTint/releases), install it and grant root access.

Pick a color and tap **Apply color**. Live updates stay on until you tap **Pause live color**. The Keyboard, Quick Panel and Volume tabs show previews of your settings.

**Restore previous colors before uninstalling or clearing app data.** The overlays can stay active after uninstalling, and the app stores information needed to restore your earlier Theme Park settings.

## Compatibility

Requires Android 14+, Samsung Keyboard and the main Android user. Resource mappings are included for One UI 6–9, but most testing has been on a Galaxy S24+ running Android 16 / One UI 8.5.

Samsung updates can break the mappings. Picture themes and high-contrast keyboards may also affect the result. The 0.2.1 interface has passed local build and color tests; phone testing is still pending.

## How it works

OneTint uses root to apply fabricated resource overlays. It checks the effective resource values after applying them and rolls back if verification fails. If a Theme Park component conflicts, OneTint records its state before temporarily disabling it. Restore re-enables the saved components.

The app-icon overlay is checked separately. Launcher caching can affect when the new icon appears.

OneTint has no Internet permission or analytics. It does not change keyboard sounds, layouts or system files. It is not affiliated with Samsung.

## Source

- [Build and test](docs/BUILDING.md)
- [Fixed colors and defaults](docs/APPEARANCE.md)
- [Changelog](CHANGELOG.md)
- [Third-party notices](THIRD_PARTY_NOTICES.md)

MIT license. The Material icons use the Apache 2.0 license. Signing keys are not included.
