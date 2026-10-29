"""Create presentation-neutral copies of the existing technical UML sources."""
from pathlib import Path
import json
import re

base = Path(__file__).resolve().parent
out = base / 'uml_unified'
out.mkdir(exist_ok=True)
groups = json.loads((base / 'uml_spec.json').read_text())
source = json.loads((base / 'uml_manifest.json').read_text())
areas = {g['prefix'].lower(): g['title'] for g in groups}
result = []
for index, diagram in enumerate(source, 1):
    identity = diagram['id']
    title = diagram['title'].split(' • ', 1)[1]
    if diagram['type'] == 'Use Case':
        title = 'Chức năng ' + areas[identity.split('_')[0]].lower()
    text = (base / 'uml' / (identity + '.puml')).read_text()
    text = re.sub(r'^title .+$', 'title ' + title, text, flags=re.M)
    text = re.sub(r'TV[1-4] — ', '', text)
    text = text.replace(r'Server\n(TV1 / TV2)', r'Server\nXử lý nghiệp vụ')
    assert not re.search(r'TV[1-4]', text)
    (out / (identity + '.puml')).write_text(text)
    result.append(dict(id=identity, type=diagram['type'], title=title,
                       number=f'B.{index}', use_cases=diagram['use_cases'],
                       path=f'uml_unified/{identity}.png'))
(out / 'manifest.json').write_text(json.dumps(result, ensure_ascii=False, indent=2))
print('Prepared 14 UML sources with functional titles and no member labels.')
