# SafeLab Mobile v2.5.0 — RC4 de sincronización y consistencia Web ↔ Android (2026-11-12)

> **Entrega para pruebas locales antes de publicar.** Esta RC4 continúa la RC3 y no
> cambia el contrato del backend ni las migraciones Flyway. No se ha verificado
> una compilación Android o una ejecución real en el emulador desde este entorno;
> esas pruebas deben realizarse en Android Studio con el backend v2.5.0 encendido.

## ¿Qué se corrigió en esta revisión?

1. **Sin botón «Actualizar datos».** Se eliminó el botón de las vistas operativas. Las
   listas se actualizan al entrar/cambiar de módulo o submódulo, al recuperar
   primer plano y **cada 15 segundos mientras la app está activa**, sin cerrar sesión.
   La recarga se ejecuta con `GET /api/v1/integration/bootstrap` y las reglas de
   permisos del backend: no se implementa WebSocket ni push instantáneo.
2. **Administrador de plataforma:** cambiar Resumen, Empresas, Actividad, Más,
   abrir una empresa, pasar entre General/Usuarios/Dispositivos/Pagos/Operaciones
   o abrir «Ver todos» activa una nueva consulta. Este flujo antes no estaba
   enlazado con la recarga general de la navegación de Android.
3. **Usuarios por organización:** el KPI y la lista de personal de cada empresa
   excluyen al `organizationAdmin` (gerente) y al administrador global. Ejemplo:
   6 empleados + 1 gerente = **6 empleados** en el detalle de la empresa, pero
   **7 cuentas** si se consulta el total de cuentas incluyendo al gerente.
   El total global de cuentas del administrador de plataforma no cambia.
4. **Perfil del usuario:** después del bootstrap se reconcilia la información
   local con `users` del backend. Si el perfil está siendo editado, no se borran
   los cambios todavía no guardados.
5. **Permisos y asignaciones:** la recarga automática no reinicializa los switches
   y casillas con modificaciones pendientes; si el formulario no tiene cambios,
   incorpora las modificaciones nuevas del servidor.
6. **Aislamiento:** el cliente descarta respuestas antiguas si la sesión cambió
   durante la petición HTTP. El servidor sigue siendo la autoridad para tenant,
   permisos y recursos; no se agregaron permisos nuevos a los usuarios.
7. **Detalle abierto:** la ficha de un registro vuelve a consultar su dato actual
   dentro del snapshot más reciente (si desapareció o perdió permiso, no
   conserva la versión antigua en pantalla).

## Ejecución local (Android Studio)

1. Inicia PostgreSQL 18 (`safelab_db`) y Spring Boot **backend-v2.5.0** con perfil
   `postgres` en `localhost:8080` (no ejecutar JSON Server del frontend).
2. Abre este directorio como proyecto Gradle en Android Studio, sincroniza y
   ejecuta la variante `debug` en Android Emulator. `10.0.2.2:8080` corresponde
   al host de tu PC **únicamente para el emulador**.
3. Inicia sesión con los roles de prueba; cambia de módulos y verifica cambios
   desde Web, sin pulsar botones de actualización ni cerrar sesión.
4. Para probar un **Android físico**, cambia la URL del build debug por la IP LAN
   de la PC y configura firewall y red; el backend desplegado utilizará HTTPS
   (se prepara en v3.0.0). El valor `https://example.invalid/api/v1` del build
   `release` es un marcador y **NO está listo para publicar**.

**Pruebas completas:** [`docs/SYNC_PARITY_QA_RC4.md`](docs/SYNC_PARITY_QA_RC4.md).

---

# Histórico RC3 — Sincronización móvil y paridad API (2026-11-07)

> **Candidata de validación local.** No crear todavía un tag estable: primero aprobar `npm run build`, compilación Android, tests Maven y pruebas reales PostgreSQL.

## Incidencias corregidas respecto de RC2

- **Mobile:** `GET /api/v1/integration/bootstrap` se vuelve a consultar al cambiar módulo/pestaña o submódulo, y al recuperar el primer plano. RC3 tenía un botón **Actualizar datos** (eliminado en RC4) y un mensaje de error con opción de reintento; **ya no hace falta cerrar sesión** para obtener cambios de Web. No se implementaron WebSockets ni sincronización push instantánea.
- **Mobile:** las colecciones de **activos** y **sensores** ofrecen editar y eliminar, y **mantenimiento** ofrece editar, cuando el token tiene el permiso correspondiente. El formulario de activos incluye nombre, categoría, sede, ubicación, estado, fecha y responsable existente por ID (solo el gerente). Sensores permiten configuración de umbrales y envío de lectura; eliminaciones piden confirmación y respetan las FK.
- **Mobile:** acciones del backend para reconocer/resolver/escalar alertas, investigar/resolver/cerrar incidentes y activar/pausar reglas de cumplimiento, de acuerdo con los permisos. Se conserva el alta de recursos, invitaciones y asignaciones de RC2.
- **Web + API + Mobile:** los permisos efectivos se publican en el bootstrap autenticado, en vez de que cada interfaz los adivine desde valores simulados. El backend sigue validando permisos en cada endpoint. El backend limita el permiso implícito del gerente a su catálogo oficial y evita recursos sin asignación para roles técnicos.
- **Backend:** HTTP 409 claro al intentar borrar equipos o sensores que conservan referencias clínicas; **V1–V4 de Flyway no se modificaron**. No borrar ni reinicializar `safelab_db`.
- **Relaciones:** se han comprobado las referencias del seed JSON en 11 colecciones sin encontrar IDs inexistentes ni cruces entre organizaciones; no equivale a verificar la base local ya modificada. Ejecutar `database/diagnostics/verify_rc3_relations.sql` en pgAdmin.

## Compatibilidad y límites

**Leer:** los 12 BC siguen consumiendo colecciones autorizadas de Spring Boot.
**Escribir:** sólo existen operaciones reales para endpoints específicos. Las pantallas sin API equivalente continúan siendo de consulta o demostración; no se simula un guardado exitoso. Para detalle consulta `docs/COMPATIBILITY_MATRIX_RC3.md`.
**Sincronización:** al navegar, volver a primer plano o pulsar Actualizar; no es tiempo real continuo mientras ambas pantallas permanecen abiertas.
**Roles:** 403 del backend es la autoridad; los botones visibles no garantizan permisos si la sesión cambió.

### Orden para pruebas

1. Haz copia de seguridad de `safelab_db` en pgAdmin. Detén Web/Mobile/Backend.
2. Inicia **Backend RC3** en IntelliJ (`postgres`, las cinco variables privadas ya configuradas). Verifica Swagger y Flyway V4.
3. Inicia **Web RC3** (`npm install`, `npm run dev`, opcional `npm run build`).
4. Ejecuta **Mobile RC3** en Android Emulator (servidor local `http://10.0.2.2:8080/api/v1`).
5. Ingresa como gerente en ambas aplicaciones. Crea un activo en Web; cambia de módulo en Mobile y vuelve a Activos: debe aparecer **sin cerrar sesión**.
6. Edita ubicación/estado desde Mobile; cambia de módulo en Web y vuelve: verifica el mismo registro.
7. Comprueba actualización desde pgAdmin, intenta eliminar un activo sin dependencias y uno con sensores vinculados (este último debe devolver 409), y valida una cuenta de técnico sin permisos de edición.
8. Corre las consultas de diagnóstico y la matriz QA antes de cualquier commit o tag.

---

# SafeLab Mobile — v2.5.0 (API local)

## Corrección de asignaciones de activos — v2.5.0 RC2 (2026-11-07)

**Motivo:** en la entrega RC1 `responsible` era texto libre y `users.resourceIds` era el
control de acceso. Por eso poner "Andrea Torres" en un activo no lo hacía visible
para esa cuenta. Los KPI estaban bien calculados según los activos autorizados,
pero la asignación era inconsistente.

**Cambios:** `responsibleUserId` es el vínculo por ID al empleado existente. El gerente
elige de una lista de empleados activos de **su empresa** y **sede autorizada**;
Spring Boot valida esas restricciones y guarda tanto el activo como su asignación
(`users.resourceIds`). El responsable anterior pierde el acceso derivado de ese
vínculo cuando se transfiere; los recursos adicionales concedidos explícitamente
a través de *Users & Permissions* siguen siendo una autorización independiente.
Los KPI se recalculan sobre la lista actualizada tras volver a sincronizar.

- **PostgreSQL:** Flyway `V4__asset_responsible_user_assignment.sql` añade una
  referencia SQL verificada por empresa, enlaza registros legados cuyo nombre
  coincida con un único empleado activo autorizado para la sede y añade esos
  activos a `resource_ids` sin borrar otras asignaciones. No borra datos, no
  repite el seed y no altera V1–V3.
- **Web:** el campo *Responsible* deja de ser un texto libre y pasa a ser un
  selector de empleados existentes; el formulario envía el ID. Al crear también
  persiste `status` y `lastInspection` (antes se perdían en POST).
- **Android:** en el formulario de alta desde *Facilities* el gerente puede
  elegir un empleado existente; el perfil del empleado y su colección de activos
  se refrescan desde la API cuando la aplicación retoma actividad.
- **JSON Server:** la semilla de demostración conserva el mismo criterio de IDs;
  la actualización de una base JSON de ejecución requiere reset explícito.

**Importante:** no crear aún el tag v2.5.0 ni publicar RC2 como versión final.
Esta es una candidata a pruebas; Maven, Vite, Gradle, PostgreSQL y emulador
requieren comprobación completa en el equipo local.

### Orden obligatorio de prueba

1. Detener los tres procesos. Hacer una **copia de seguridad de `safelab_db`**.
2. Sustituir primero el backend por RC2 (mantener variables privadas actuales).
3. Iniciarlo con perfil `postgres`: Flyway aplica automáticamente **V4 una sola vez**.
   Comprobar en `flyway_schema_history` que la V4 finalizó correctamente.
4. Ejecutar Web RC2 con `npm install` y `npm run dev` (no se necesita su `npm run server`).
5. Entrar como gerente, editar `Freezer QA Web v2.5`, seleccionar **Andrea Torres**
   en el nuevo desplegable y guardar. Cerrar e iniciar la sesión de Andrea,
   o refrescarla, para que vuelva a consultar el alcance.
6. Comprobar el inventario y los KPI de Andrea, los IDs de usuario de los activos
   y el cambio en PostgreSQL. El total **debería ser 7 solo si** permanecen los
   6 activos del seed asignados a Andrea y el Freezer QA de prueba se vincula a ella;
   cualquier filtro de sede o permisos adicional cambia el resultado.
7. Abrir Mobile RC2 en emulador, iniciar sesión como Andrea y revisar su listado.

Consultas SQL de verificación (pgAdmin → `safelab_db`):

```sql
SELECT installed_rank, version, description, success
FROM public.flyway_schema_history ORDER BY installed_rank;

SELECT a.id, a.name, a.responsible, a.responsible_user_id,
       u.full_name AS employee_name, a.facility_id
FROM public.assets a
LEFT JOIN public.users u ON a.responsible_user_id = u.id
WHERE a.organization_id = 'org-andes'
ORDER BY a.name;

SELECT u.full_name, u.resource_ids
FROM public.users u WHERE u.full_name = 'Andrea Torres';

SELECT count(*) AS linked_assets
FROM public.assets a JOIN public.users u
 ON a.responsible_user_id = u.id
WHERE u.full_name = 'Andrea Torres' AND a.organization_id = 'org-andes';
```

**Diferencia entre roles:** la lista del gerente muestra todos los activos de su
organización, mientras la de Andrea solo muestra aquellos autorizados por su
alcance y sedes. `responsible` es una etiqueta legada; el identificador seguro
es `responsibleUserId`. Los ajustes manuales en Users & Permissions conceden
acceso, pero no cambian por sí mismos quién figura como responsable principal.

---


## Integración local v2.5.0 (desarrollo)

Esta versión **no despliega servicios**. Las tres aplicaciones siguen separadas, pero Web y Mobile consumen HTTP/JSON desde **SafeLab Platform API**. La API es la única que conoce las credenciales PostgreSQL, realiza las validaciones y aplica IAM/RBAC/RLS. Ninguna contraseña SQL se incorpora a Vue o Android.

### Arquitectura

```text
Vue (localhost:5173)  ──────┐
                            ├── Spring Boot API (localhost:8080) ── PostgreSQL (5432)
Android emulator (10.0.2.2) ┘
```

**JSON Server se conserva en el repositorio backend** como perfil `jsonserver` opcional. Los frontends siempre consumen la API Spring Boot, nunca `data/db.json` ni PostgreSQL directamente.

### Qué está conectado

- Login real y Bearer token, con datos de sesión obtenidos de Spring Boot.
- `GET /api/v1/integration/bootstrap`: snapshot multiempresa filtrado en servidor, sin contraseñas. Las pantallas de los 12 BC pueden leer las colecciones autorizadas.
- Altas y modificaciones principales de activos y sensores, incidentes, órdenes de mantenimiento y reportes, usando endpoints específicos, no un CRUD global abierto.
- Acciones de alertas, comandos simulados, invitaciones, permisos y asignaciones de empleados donde el frontend dispone de la operación correspondiente.
- Refresco de datos Web al recuperar el foco y Mobile al reanudar la actividad. **No hay WebSocket ni actualización instantánea en segundo plano**.

### Limitaciones explícitas

No todos los flujos heredados del frontend tienen equivalentes seguros en el backend v2. Los controles de configuración global de plataforma, simulaciones puramente visuales, carga de evidencias/archivos y algunas operaciones de gestión especial aún requieren endpoints y pruebas adicionales. No deben considerarse persistidos solo porque una pantalla cambie de apariencia. El modo Android de depuración permite HTTP a la máquina del desarrollador; en producción se requerirá HTTPS y una URL real.

La integración se considera **candidata a validación**, no aprobada ni lista para producción, hasta ejecutar `mvn test`, `npm run build`, el build Android y la matriz de pruebas cruzadas indicada en `docs/INTEGRATION_TEST_PLAN.md`.

---

## Historial y guía de la versión anterior

# SafeLab Mobile — Multi-Tenant Frontend

Android application built with **Kotlin, Jetpack Compose and Material 3** for SafeLab, a clinical cold-chain monitoring system for laboratories and pharmaceutical organizations.

This repository is the native mobile client. The domain is organized in **12 Bounded Contexts**, while the signed-in person's role determines the mobile navigation and the records they can access.

## Run locally

1. Open the project folder in **Android Studio**.
2. Allow Gradle to sync with an installed Android SDK (compile SDK 35).
3. Run the `app` configuration on an emulator (Android 9 / API 28 or newer) or a physical device.
4. Alternatively, with a working local SDK and Gradle distribution: `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`).

## What the app includes

- **SafeLab Platform:** a dedicated global administration experience for companies, users, subscriptions and audit events. Organization detail screens are grouped into users, devices, billing and operations.
- **Organization management:** the general manager sees only their company, can invite employees and edit their permission switches with explicit **Save, Cancel and Restore defaults** controls, and can assign equipment and sensors.
- **Organization operations:** employees can view only allowed navigation modules and resources. Management commands and changes are restricted according to the shared permission catalog.
- **Account registration:** either a new company/manager (pending company verification and payment) or an employee joining an existing organization (pending email activation, no separate payment).
- **Bilingual interface:** English and Spanish, with Material 3 light and dark themes. The authentication background uses a navy/blue gradient and a subtle flask watermark.
- **Data details and exports:** interactive record lists with search, expandable details and CSV sharing through Android's share sheet.

## Matching the web frontend

The files in `app/src/main/assets/` were copied directly from the approved Vue frontend:

| Android asset | Web source | Purpose |
|---|---|---|
| `safelab_data.json` | `server/db.json` | Same stable companies, accounts, assets, sensors, alerts, audit records and invoices |
| `safelab_permissions.json` | `src/boundedcontext/identityaccess/domain/models/permission-catalog.js` | 21 permissions, six employee roles, templates and delegation limits |
| `strings_en.json`, `strings_es.json` | `src/locales/` | Same organization, permission and role labels |

Data access is implemented in `shared/infrastructure/data/MultiTenantStore.kt`, initially from Android assets and `SharedPreferences`. That provides a small, replaceable data boundary for a future REST backend.

The dataset contains **3 companies, 19 accounts, 12 sensors, 10 assets and 3 subscriptions**. The system test account uses `admin@safelab.test`, password `SafeLab2026%`; all fictional demo accounts in the seed currently use that same local test password. **Never use these passwords or the local role checks as production authentication or authorization.** The mock records are packaged with the app and are not a secure database.

## Bounded Contexts

1. Identity & Access Management
2. User Profile Management
3. Subscription & Billing
4. Facility & Asset Management
5. Sensor & Telemetry Monitoring
6. Equipment Health & Maintenance
7. Alert & Incident Management
8. Environmental Compliance Management
9. Remote Control & Actuation
10. Operational Dashboard & Insights
11. Reporting & Analytics
12. Audit & Traceability

The existing native BC implementations are preserved. The multitenant presentation routes now read through the **shared web-aligned data store** to avoid showing the older single-company sample records to users of another organization.

## Development workflow

Branch from `develop` for features, fix or refactor work. After local tests, merge into `develop` and only promote validated releases to `main`. Suggested feature branch: `feature/av2-mobile-multitenant-rbac`.

## Next integration steps

Implement secure server-managed accounts, verified corporate email and payments, API-enforced tenant separation and permissions, persistent audit, and live IoT communication. Then replace the local mock store with authenticated APIs in both the web and mobile frontends.


## AV2 Mobile: final visual refinements

- The Android launcher uses the existing **SafeLab shield** artwork (adaptive and legacy icons).
- The theme selector is a single reusable, outlined sun/moon vector button.
- Sign out is intentionally red on both themes for all account types.
- For the organization manager, **More → Users & permissions → Employees → select an employee** opens a separate permission editor; 21 switches, role defaults, assignments, **Save** and **Cancel** are preserved. Leaving with unsaved changes via the editor's back button asks for confirmation.
- Login and registration use the existing themed gradient; registration headings remain legible in dark mode.
- The platform test account and all other mock accounts live **only** in `app/src/main/assets/safelab_data.json`. Kotlin IAM fixtures no longer duplicate passwords or login credentials. These are fictional local accounts, not secure authentication for production.
- The bundled `safelab_data.json` remains unchanged so IDs and demo records match SafeLab Web; `strings_en.json` and `strings_es.json` carry UI descriptions.

After opening in Android Studio, use **Build → Rebuild Project** and verify light/dark themes, both languages, register and sign-in, all account roles, manager permissions and launcher icon on the emulator.
