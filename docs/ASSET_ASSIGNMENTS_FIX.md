# Prueba de asignación de activos entre Web, Mobile y PostgreSQL

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
