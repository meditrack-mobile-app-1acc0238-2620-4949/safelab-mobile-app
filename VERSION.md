# SafeLab Mobile v2.5.3 — recuperación de conexión (AV2)

Parche sobre la entrega recibida como `safelab-mobile-app-av2-v2.5.2`.
Validación de compilación y de APK en Android Studio pendiente.

- Se distingue el error de DNS, el tiempo de espera y la indisponibilidad del servidor.
- OkHttp tiene `connectTimeout=15 s`, `readTimeout=75 s` y `callTimeout=90 s` para contemplar el arranque lento del backend.
- GET dispone de un único reintento acotado frente a errores transitorios o HTTP 502/503/504. Las solicitudes POST/PUT/PATCH/DELETE no se reintentan automáticamente.
- La pantalla de ingreso no confirma la sesión si falla el `GET /integration/bootstrap`; descarta el token parcial, permite volver a intentar con las credenciales conservadas y no requiere borrar los datos de la app.
- La sincronización automática omite solicitudes redundantes cuando ya hay una lectura en curso; se conserva el refresco al navegar, al volver a primer plano y el polling existente.
- Al cerrar sesión, el token local se invalida inmediatamente sin esperar a la red.
- Se mantiene el mismo endpoint de Render, permisos, temas, módulos y UI. No se implementa cierre por inactividad (pendiente para el Trabajo Final).
- `versionCode=4`, `versionName=2.5.3`. Para actualizar la APK instalada, compilarla con la misma clave de firma o desinstalar la anterior solo si cambia la firma.

**Comprobaciones necesarias en dispositivo físico y emulador:** iniciar sesión con API activa; probar con Wi-Fi desactivado; reintentar sin borrar datos; restaurar Internet; validar servidor lento en Render; verificar sincronización y cierre de sesión; comprobar que las peticiones de escritura no se repiten automáticamente.

---

# SafeLab Mobile v2.5.0 RC4 — sincronización y paridad de conteos (2026-11-12)

- No se modifica el número visible `versionName = "2.5.0"` para esta RC local.
- Se elimina el botón manual de refresco; navegación, primer plano y polling
  de 15 s recuperan datos para todos los roles y BC.
- Detalle de organización: empleados sin gerente; global conserva cuentas.
- Perfiles, permisos y asignaciones se reconcilian sin descartar borradores.
- La API continúa en Spring Boot v2.5.0 y PostgreSQL con las mismas migraciones.
- Validar en Android Studio antes de considerar esta versión estable.

# SafeLab v2.5.0 RC3 — API local (sincronización y paridad)

Validación pendiente. Conservar tags anteriores. V1–V4 Flyway inmutables.

# SafeLab Mobile — v2.5.0 RC2 (asignación de activos)

Corrección previa al tag; mantiene la base y el historial de v2.5.0.

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
# SafeLab Mobile — v2.5.0 (integración local)

Base: tag `av2-v1.1.0`. Se conserva estructura y nomenclatura de los 12 BC y las vistas originales.

Integración: inicio de sesión por API REST; carga multiempresa con `GET /integration/bootstrap`; operaciones autorizadas comunicadas a PostgreSQL a través de Spring Boot. Contratos técnicos JSON conservados en inglés, interfaz ES/EN.

No contiene datos SQL, contraseñas ni una URL de producción operativa. Es una candidata a pruebas: consultar documentación para limitaciones y pruebas antes del tag.
