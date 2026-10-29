# Fixed values and editable defaults

Colors below use Android's `#AARRGGBB` format: alpha, red, green, blue. `80` is 128/255 opacity (about 50%); `FF` is fully opaque. A translucent color's visible result depends on the surface behind it.

## Fixed applied values

| Use | Value or rule |
| --- | --- |
| Keyboard and Quick Panel backgrounds | `#FF000000` |
| Neutral surface base, including key/volume-track resources | `#FF181818`, with per-resource opacity |
| Normal keyboard key backgrounds | Surface at 50%: `#80181818`; preview composites it over black as `#FF0C0C0C` |
| Keyboard key pop-up | `#80171717` |
| Pop-up text | `#FFFFFFFF` |
| Normal keyboard text/icons and other neutral foreground roles | `#FFF5F4FA`, with per-resource opacity |
| Pressed keyboard key | Selected accent at 50% opacity |
| Focused alternative-key bubble | Selected accent, as defined by its catalog entry |
| Error role | Day `#FFB3261E`; night `#FFFFB4AB` |
| Keyboard alert text | `#FFDB332A` |
| Keyboard search field | Day `#80FFFFFF`; night `#80000000` |
| Quick Panel edit/helper icons | White where specified in the catalog; checkbox tint retains its catalog opacity |
| Quick Panel Wi-Fi helper resources | Blue/on helper `#FFFFFFFF`; gray helper `#40808080` |
| One UI 8.5+ framework refresh marker | `#FFFAFAFF` (`sem_index_scroll_preview_text_color`) |

These are mappings, not a promise that each firmware renders every listed resource. Root apply checks the effective resources it finds on the device. Missing resource names are reported.

Source: `Palette.java`, `AmoledTheme.java`, `RootBridge.java` and `app/src/main/assets/catalog.json`.

## Editable values

| Control | Initial value | Behavior |
| --- | --- | --- |
| Main accent | `#FFB49AFF` | Keyboard accents, active tiles, volume progress, app-icon accent |
| Inactive bubbles | `#FF757575` | Independent color picker |
| Notification background | `#FF171717` | Independent color picker |
| Notification text | `#FFFFFFFF` | Independent color picker, fully opaque |
| Notification icons | `#FF757575` | Independent color picker, fully opaque |
| Quick Panel icon fill | White | Manual Black/White slider |
| Volume icons | Old automatic result on first upgrade; otherwise the saved choice | Manual Black/White slider; does not change automatically with the accent |

All color pickers except the fixed pop-up accept six-digit RGB and produce opaque colors. Reset returns each secondary color to the initial value above.

The main action button's text uses automatic contrast: relative sRGB luminance above `.179` selects black, otherwise white. The same helper selects the small symbol inside a notification preview's colored app-icon circle. It does not override the manual Quick Panel/volume choices or the white pop-up text.

## Preview-only constants

Previews illustrate the settings; they are not screenshots of SystemUI or Samsung Keyboard.

| Preview | Fixed values |
| --- | --- |
| Keyboard | QWERTY layout, `EN(US)` spacebar, simulated pressed `g`, key dimensions and labels |
| Quick Panel | Six sample tiles and their on/off states; one sample pizza message from Messages at 7:20 AM |
| Volume | Panel `#FF151515`, unfilled track `#FF333333`, labels `#FFF5F4FA`; number/fill come from the current Android media stream |

The volume number is Android's current stream index / maximum, not a fixed percentage. Vendor fine-volume steps or output-device displays can differ from this public API. A Samsung media drawable is used if a supported resource name is present; otherwise the bundled Material volume icon is used.

## App interface and launcher icon

The app's own layout and dark chrome remain fixed. Main colors include background `#FF0D0F14`, text `#FFF4F0FF`, muted text `#FFAAA7B6`, tab background `#FF191B22`, selected tab `#FF30333E`, settings cards `#FF17191F`, color chips `#FF272A33` and Restore button `#FF20222C`. Borders, selection animations, sizes and typography are defined in the view code. The app's theme fallback accent is `#FFBEA6FF`.

The launcher icon has a fixed `#FF171820` background and `#FFF8F1FF` highlight. Its droplet accent uses the selected color when the optional icon overlay succeeds. The bundled fallback accent is `#FFB49AFF`.

## Other fixed behavior

Catalog resource names, per-component opacity, supported version ranges, the main-user restriction, overlay identifiers, restore-state whitelist and operation timeouts are constants. They are implementation limits rather than appearance controls. Layout, sounds, key animations and real notification content are not changed by OneTint.
