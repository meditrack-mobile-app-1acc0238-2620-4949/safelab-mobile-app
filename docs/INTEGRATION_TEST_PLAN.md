# SafeLab v2.5.0 — Pruebas cruzadas locales

## Requisitos

1. `safelab_db` existente, 3 migraciones Flyway aplicadas (V1-V3), datos demo importados mediante `scripts/seed-demo.ps1` si deseas probar las cuentas ficticias.
2. Spring Boot con perfil `postgres` iniciado en `http://localhost:8080`. Debe aparecer `Started SafeLabPlatformApiApplication`.
3. Web con `npm install && npm run dev` en `http://localhost:5173` y archivo `.env` construido desde `.env.example` (URL = `http://localhost:8080/api/v1`).
4. Android Studio y emulador Android oficial con SafeLab Mobile debug. URL de emulador `http://10.0.2.2:8080/api/v1`; en la máquina Windows se usa `localhost` pero en el emulador no.
5. No iniciar JSON Server en modo PostgreSQL.

## Matriz (registrar HTTP y salida SQL real)

| # | Prueba | Acción | Resultado esperado |
|---|---|---|---|
| 1 | Login Web | Gerente Andinos desde Vue | Acceso autorizado, token e información de `org-andes` |
| 2 | Login Mobile | Misma cuenta desde Android | Se muestran datos de la misma empresa |
| 3 | Protección global | Gerente → `GET /api/v1/platform/users` | HTTP 403 |
| 4 | Protección tenant | Gerente Andinos → `/identityaccess/organizations/org-gabriel/users` | HTTP 403 |
| 5 | Creación Web | Nuevo activo desde formulario de Web | HTTP 201, aparece en `public.assets` |
| 6 | Lectura Mobile | Regresar a Android o reabrirlo | El activo recién creado aparece después del refresco |
| 7 | Creación Mobile | Nuevo activo desde “Instalaciones y activos” | HTTP 201, aparece en `public.assets` |
| 8 | Lectura Web | Cambiar de pestaña y volver a SafeLab Web | Aparece el nuevo activo tras actualizar los datos |
| 9 | PATCH Web | Editar ubicación del activo | Misma fila e ID, campos nuevos en PostgreSQL |
| 10 | Usuario sin autorización | Intentar crear como rol sin `assets.manage` | HTTP 403, cero filas agregadas |
| 11 | Persistencia | Cerrar y reiniciar Spring Boot | Registros y ediciones se conservan |
| 12 | Restricciones SQL | Intentar ID/correo duplicado con endpoint autorizado | Error de integridad, no se duplican filas |
| 13 | Sesión expirada | Logout e intentar endpoint privado con token antiguo | HTTP 401 |
| 14 | JSON mode | Iniciar API con perfil `jsonserver` y JSON Server en 3000 | Sigue funcionando el backend sin PostgreSQL; datos independientes |

## Consultas en pgAdmin

```sql
SELECT id, name, organization_id, facility_id, location, status
FROM public.assets ORDER BY id DESC LIMIT 15;

SELECT id, name, organization_id, value, status
FROM public.sensors ORDER BY id DESC LIMIT 15;

SELECT version, description, success FROM public.flyway_schema_history ORDER BY installed_rank;
```

Consulta las tablas como administrador en pgAdmin. **No compartas credenciales SQL con los clientes**. Nunca ejecutes pruebas destructivas sobre una base con datos reales. Revisa también los logs HTTP y el código de error devuelto; cambios solo visuales no cuentan como persistencia.

## Notas

- Los frontends obtienen un snapshot al iniciar sesión y al regresar al primer plano: cambios cruzados requieren refresco, no se emiten eventos en tiempo real.
- Los endpoints del backend siguen aplicando autorización: la ocultación de botones del frontend no sustituye RBAC/RLS.
- El `DELETE /sensormonitoring/sensors/{id}` puede fallar con FK si el sensor tiene registros relacionados; es preferible desactivación/soft-delete en una etapa posterior.
- No consideres aprobados todos los BC por superar las pruebas de activos; validar cada flujo que se publique.
