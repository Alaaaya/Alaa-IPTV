from pathlib import Path
import re
import xml.etree.ElementTree as ET

root = Path('/home/ubuntu/Alaa-IPTV/app/src/main')
errors = []
xml_files = list(root.rglob('*.xml'))
for path in xml_files:
    try:
        ET.parse(path)
    except Exception as exc:
        errors.append(f'XML parse error: {path}: {exc}')

resource_names = {kind: set() for kind in ('drawable', 'string', 'color', 'layout')}
for path in root.joinpath('res').rglob('*'):
    if path.is_file():
        parent = path.parent.name
        if parent.startswith(('drawable', 'layout')):
            resource_names[parent.split('-')[0]].add(path.stem)
        if parent.startswith('values') and path.suffix == '.xml':
            try:
                tree = ET.parse(path)
                for node in tree.getroot():
                    name = node.attrib.get('name')
                    tag = node.tag.rsplit('}', 1)[-1]
                    if name and tag in resource_names:
                        resource_names[tag].add(name)
            except Exception:
                pass

for path in list(root.joinpath('res').rglob('*.xml')) + list(root.joinpath('java').rglob('*.kt')):
    text = path.read_text(errors='replace')
    for kind, name in re.findall(r'@(drawable|string|color|layout|id)/([A-Za-z0-9_]+)', text):
        if kind != 'id' and name not in resource_names.get(kind, set()):
            errors.append(f'Missing @{kind}/{name} referenced by {path}')

for path in root.joinpath('res/layout').glob('*.xml'):
    text = path.read_text(errors='replace')
    ids = re.findall(r'android:id="@[+]?id/([A-Za-z0-9_]+)"', text)
    seen = set()
    for item in ids:
        if item in seen:
            errors.append(f'Duplicate id @{item} in {path}')
        seen.add(item)

if errors:
    print('\n'.join(errors))
    raise SystemExit(1)
print(f'OK: parsed {len(xml_files)} XML files; checked resource references and layout IDs.')
