# Changelog

## 0.2.1

- Replace the resource-name list with labeled color swatches around each preview.
- Group the keyboard controls into keys, letters, pressed key, pop-up and background.
- Put notification colors directly below the Quick Panel controls.
- Show one vertical volume popup, with fill and track controls beside it.
- Add an opacity slider to the color editor and keep the exact ARGB field.
- Redraw the Quick Panel connection tiles and stop keyboard labels showing through the pop-up.
- Prefer Samsung media and headphone icons when available.

## 0.2.0

- Add searchable, individual color controls for Keyboard, Quick Panel and Volume.
- Save custom ARGB values and reset individual colors or a section.
- Keep explicit overrides independent of the main accent, preserving the entered opacity.
- Update the main preview colors to follow custom values. Some secondary resources are not drawn in the preview.

## 0.1.12

- Accept RGB and ARGB hex values, including `8086ABF2`.
- Preserve transparency when moving the color wheel, saving colors and applying overlays.
- Show all eight digits so the saved alpha is visible.

## 0.1.11

- Set the key pop-up to `#80171717`, with white text.
- Keep only the pizza notification in the Quick Panel preview.
- Draw the translucent pop-up as one shape so its neck does not become darker where shapes overlap.
- Prepare public source, build instructions and a fixed-values reference. Signing files are excluded.

## 0.1.10

- Add vector-based app-icon recoloring.
- Keep Restore's loading label on its own button.
- Read the system media-volume level for the volume preview and replace the speaker drawing.
- Redraw notification previews as wide pills.
- Remove the automatic-contrast caption.

## 0.1.9

- Add separate notification background, text and icon pickers.
- Add a black/white volume icon selector and group related controls.

## Earlier versions

Established the root overlay, resource readback and restore mechanisms, added AMOLED surfaces, and introduced the preview tabs and Quick Panel icon controls.
