#!/usr/bin/env python3
"""Extract only resource mappings from a user-provided Theme Park APK.
Keep repeated UIDs: a single UID can color both main and cover-screen controls.
Resolve common-theme references before omitting a resource.
"""
import json, sys, zipfile, xml.etree.ElementTree as ET
from pathlib import Path

def extract(apk):
    result = {}
    groups = [('75. HoneyBoard', 'Keyboard', 'com.samsung.android.honeyboard'),
              ('04. Notification panel', 'QuickPanel', 'com.android.systemui'),
              ('2001. VolumePanel', 'Volume', 'com.android.systemui')]
    with zipfile.ZipFile(apk) as z:
        for version in ['60000', '70000', '80000', '90000']:
            prefix='assets/'+version+'/'
            themes={}; references={}
            for name in sorted(z.namelist()):
                if name.startswith(prefix+'templates/') and name.endswith('.json'):
                    for item in json.loads(z.read(name)).get('template',[]):
                        themes[item['uid']]=item
                if name.startswith(prefix) and name.endswith('ThemeMetadata.xml'):
                    for prop in ET.fromstring(z.read(name)).iter('Property'):
                        if prop.get('ValueRef'):
                            references.setdefault(prop.get('UID'),prop.get('ValueRef'))
            def resolve(uid, seen=()):
                if uid in seen: return None
                item=themes.get(uid,{})
                if item.get('theme'): return item
                ref=references.get(uid)
                return resolve(ref,seen+(uid,)) if ref else None
            output=[]
            for folder,label,target in groups:
                properties=ET.fromstring(z.read(prefix+'overlay/'+folder+'/ThemeMetadata.xml'))
                rows=[]
                for prop in properties.iter('Property'):
                    p=prop.attrib; kind=p.get('ValueType','')
                    if kind not in ['Color','TintColor','BgColor','TextColor','Bool','Text']: continue
                    item=resolve(p.get('UID'))
                    if not item or not p.get('DestAttribName'): continue
                    # Arbitrary strings/images are outside this single-color app's scope.
                    if kind=='Text' and item['theme']!='overlay_package_name': continue
                    rows.append({'uid':p['UID'],'names':[n.strip() for n in p['DestAttribName'].split(',') if n.strip()],
                                 'type':'bool' if kind=='Bool' else 'string' if kind=='Text' else 'color',
                                 'theme':item['theme'],'opacity':int(p.get('Opacity',item.get('opacity',100)))})
                output.append({'name':label,'target':target,'rows':rows})
            result[version]=output
    return result

if __name__=='__main__':
    dest=Path(sys.argv[2]); dest.write_text(json.dumps(extract(sys.argv[1]),separators=(',',':')))
