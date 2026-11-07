"""Check the small JSON contract shared by SafeLab Web and Android.

Run from this repository: python scripts/verify_mock_data.py [path/to/web/server/db.json]
Uses only the Python standard library; not needed to run the Android app.
"""
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'app/src/main/assets'
data = json.loads((ASSETS / 'safelab_data.json').read_text(encoding='utf-8'))
roles = json.loads((ASSETS / 'safelab_permissions.json').read_text(encoding='utf-8'))
checks = 0

def check(condition, description):
    global checks
    assert condition, description
    checks += 1

orgs = {o['id'] for o in data['organizations']}
users = {u['id'] for u in data['users']}
assets = {a['id']: a for a in data['assets']}
sensors = {s['id']: s for s in data['sensors']}
check(len(orgs) == 3, 'Expected three organizations')
check(len(users) == 19, 'Expected the same 19 demo accounts as the web')
check(len(sensors) == 12, 'Expected 12 sensors')
check(len(assets) == 10, 'Expected 10 assets')
check(data['users'][0]['email'] == 'admin@safelab.test', 'Platform admin email mismatch')
check(all(u.get('organizationId') in orgs for u in data['users'] if u['role'] != 'platformAdmin'), 'Invalid membership')
for collection in ['facilities','assets','sensors','alerts','incidents','actuators',
                   'maintenanceRecords','complianceRules','complianceEvaluations',
                   'complianceViolations','remoteCommands','reports','auditLogs']:
    for row in data[collection]:
        check(row.get('organizationId') in orgs, f'Invalid tenant for {collection}: {row.get("id")}')
for item in data['sensors']:
    check(item.get('assetId') in assets, f'Missing asset for {item["id"]}')
    check(assets[item['assetId']]['organizationId'] == item['organizationId'], 'Cross-tenant sensor reference')
for alert in data['alerts']:
    if alert.get('sensorId'):
        check(alert['sensorId'] in sensors, 'Missing linked sensor')
        check(sensors[alert['sensorId']]['organizationId'] == alert['organizationId'], 'Cross-tenant alert')
for record in data['maintenanceRecords']:
    check(record['equipmentId'] in assets, 'Missing maintenance equipment')
    check(assets[record['equipmentId']]['organizationId'] == record['organizationId'], 'Cross-tenant maintenance')
permissions = [p for group in roles['groups'] for p in group['permissions']]
check(len(permissions) == 21 and len(set(permissions)) == 21, 'Expected 21 unique permissions')
for lang in ('en', 'es'):
    trans = json.loads((ASSETS / f'strings_{lang}.json').read_text(encoding='utf-8'))['multi']
    for p in permissions:
        key = p.replace('.', '_')
        check(bool(trans.get('permission_' + key)), f'Missing {lang} label {p}')
        check(bool(trans.get('description_' + key)), f'Missing {lang} description {p}')
if len(sys.argv) > 1:
    web = json.loads(Path(sys.argv[1]).read_text(encoding='utf-8'))
    check(data == web, 'Web and mobile JSON snapshots differ')
print(f'PASS: {checks} data, tenant, role and localization assertions')
