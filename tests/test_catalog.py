"""Regression checks for the silent-no-change defects in the 0.1.0 extractor."""
import importlib.util,json,pathlib,sys
root=pathlib.Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location('catalog',root/'scripts/extract_catalog.py')
module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
actual=module.extract(sys.argv[1])
assert actual==json.loads((root/'app/src/main/assets/catalog.json').read_text()),'Catalog is not reproducible'
for version,groups in actual.items():
 assert len(groups)==3
 for group in groups:
  assert group['target'] in ['com.android.systemui','com.samsung.android.honeyboard']
  for row in group['rows']:
   assert 0<=row['opacity']<=100
   assert row['type'] in ['color','bool','string']
 quick=groups[1]['rows'];names={name for row in quick for name in row['names']}
 assert 'qs_tile_round_background_on' in names,(version,'main-screen mapping lost')
 assert 'open_theme_notification_title_text_color' in names,(version,'text colors lost')
 keyboard=groups[0]['rows']
 assert any(row['type']=='string' and 'theme_designer_overlay_package_name' in row['names'] for row in keyboard),(version,'keyboard marker lost')
 if int(version)>=80000:
  assert 'subscreen_qs_tile_round_background_on' in names,(version,'repeated UID lost')
  assert 'qs_flashlight_seekbar_tickmark_color' in names,(version,'shared theme reference lost')
  assert any(row['uid']=='4-2-3' and 'qs_tile_round_background_on' in row['names'] and row['theme']=='secondary' for row in quick)
print('PASS: reproducible catalogs; main + cover controls, shared references, text colors and keyboard marker preserved')
