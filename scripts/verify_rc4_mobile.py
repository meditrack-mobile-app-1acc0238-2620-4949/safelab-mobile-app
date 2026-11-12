#!/usr/bin/env python3
"""Pruebas ESTÁTICAS sin Android SDK; no sustituyen ejecutar la app contra PostgreSQL."""
import json
from pathlib import Path
src=Path(__file__).resolve().parents[1]
base=src/"app/src/main/java/pe/edu/upc/safelab"
kotlin=list(base.rglob("*.kt"))
assert kotlin, "No se encontró código Kotlin"
assert not any("Actualizar datos" in p.read_text() or "Refresh data" in p.read_text() for p in kotlin), "Botón manual presente"
app=(base/"navigation/SafeLabApp.kt").read_text()
assert "delay(15_000L)" in app and "Lifecycle.Event.ON_RESUME" in app
assert "UserProfilesRepository.syncFromBackend" in app
platform=(base/"shared/presentation/screens/PlatformAdminScreens.kt").read_text()
assert "LaunchedEffect(tab, orgId, orgTab, fullCollection)" in platform
assert platform.count("visibleOrganizationEmployees")>=2
store=(base/"shared/infrastructure/data/MultiTenantStore.kt").read_text()
assert "visibleOrganizationEmployees" in store and "sessionRevision" in store
lists=(base/"shared/presentation/screens/TenantUiComponents.kt").read_text()
assert "visibleOrganizationEmployees" in lists
admin=(base/"boundedcontext/identityaccess/presentation/screens/OrganizationAdminScreens.kt").read_text()
assert "remember(userId) { mutableStateMapOf" in admin
assert "lastServerSelection" in admin
assert "LaunchedEffect(orgId, section, selectedId)" in admin
for p in (src/"app/src/main/assets").glob("*.json"):
    json.loads(p.read_text())
seed=json.loads((src/"app/src/main/assets/safelab_data.json").read_text())
for org in seed["organizations"]:
    accounts=[x for x in seed["users"] if x.get("organizationId")==org["id"]]
    employees=[x for x in accounts if x.get("role") not in ("organizationAdmin", "platformAdmin")]
    assert len(employees)<=len(accounts)
    print(f"{org['id']}: empleados={len(employees)}, cuentas={len(accounts)}")
print("OK: validación estática RC4; no equivale a compilación o prueba HTTP")
