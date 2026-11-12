# SafeLab v2.5.0 RC4 — Matriz de QA de sincronización Web ↔ Mobile

Fecha: 2026-11-12 · Objetivo: aprobar la app Android antes del despliegue.

## Contrato de refresco

- Lectura: `GET /api/v1/integration/bootstrap` con Bearer Token, siempre filtrado
  por permisos, organización, sede y recurso desde Spring Boot.
- Refresco: al navegar entre módulos, en las pestañas internas del administrador
  de plataforma y del gerente, al retomar el primer plano, tras un CRUD remoto
  exitoso y cada **15 s** mientras Android está en primer plano.
- No es *tiempo real push*: no se usan WebSockets, SSE ni Firebase. Las pantallas
  pueden reflejar cambios externos con un retraso de hasta aproximadamente
  15 s si se mantienen abiertas, más la latencia de red. Si no hay conectividad,
  se conserva el último snapshot visible, y no se afirma que esté actualizado.
- Los permisos se validan **en el backend** con cada operación, no se obtienen
  por modificar la UI. Un `403` sigue siendo definitivo.

## Casos de aceptación (ejecutar con Backend + PostgreSQL + Web abiertos)

| ID | Rol / vista Android | Acción desde Web o Mobile | Resultado esperado |
|---|---|---|---|
| A01 | Administrador SafeLab / Empresas | Crear o modificar empleado en Web | Abrir empresa → Usuarios; lista y KPI actualizados sin logout |
| A02 | Administrador / Empresa Farma | Una organización tiene N empleados y 1 gerente | KPI **N**, no N+1; vista Usuarios **N**, gerente excluido |
| A03 | Administrador / Usuarios globales | Abrir Usuarios globales | Incluye cuentas de gerente; no usar el KPI de empleados |
| A04 | Administrador / Empresa | Cambiar General → Usuarios → Dispositivos → Pagos → Operaciones | Cada transición vuelve a consultar la API |
| A05 | Administrador / Actividad | Crear alerta o incidente vía API autorizada | Se actualiza la actividad autorizada al navegar/esperar |
| A06 | Gerente / Empleados | Crear empleado o invitación desde Web | Lista, permisos y contadores coherentes sin logout |
| A07 | Gerente / Permisos | Modificar permisos desde Web | Abrir usuario y verificar permisos del servidor |
| A08 | Gerente / Permisos | Mantener switches sin guardar 20 s | Ningún cambio sin guardar se pierde durante refrescos |
| A09 | Gerente / Asignaciones | Modificar asignación Web con editor cerrado | Abrir editor; nuevos recursos visibles |
| A10 | Gerente / Asignaciones | Mantener casillas locales sin guardar 20 s | La app no descarta las selecciones en edición |
| A11 | Gerente / Activos | Crear activo desde Web | Entrar en Activos y verificar nombre, sede y responsable |
| A12 | Gerente / Activos | Modificar ubicación desde Android | Reflejado en Web y `public.assets` PostgreSQL |
| A13 | Técnico / Activos | Asignación hecha por gerente | El recurso aparece cuando el backend lo autoriza; sin permisos no se muestra editar |
| A14 | Gerente / Sensores | Crear o modificar sensor desde Web | Lectura y KPI actualizados en Android |
| A15 | Encargado / Mantenimiento | Editar registro autorizado | Aparece cambio desde la otra aplicación |
| A16 | Usuarios / Alertas e incidentes | Reconocer/resolver según permiso | Estado consistente desde Web y Android |
| A17 | Calidad / Cumplimiento | Cambiar regla desde Web | Estado visible en Android si la API lo devuelve |
| A18 | Usuarios / Reportes y auditoría | Generar registro compatible vía API | Listados vuelven a cargar sin logout |
| A19 | Gerente / Suscripción | Alterar plan, cupos o facturación mediante API existente | Información nueva visible si bootstrap la expone |
| A20 | Todos / Perfil | Cambiar nombre o teléfono desde Web y abrir perfil Android | Datos nuevos, sin perder edición abierta |
| A21 | Todos / Aplicación en segundo plano | Modificar registro desde Web y regresar | Se consulta de nuevo al primer plano |
| A22 | Todos / Pantalla sin cambiar | Modificar registro desde Web, esperar >=15 s | Se actualiza sin intervención (si hay conectividad) |
| A23 | Seguridad / Tenant | Autenticar dos empresas separadas | Sin mezcla de organizaciones ni datos de sesión anterior |
| A24 | Todos / Sin backend | Detener Spring Boot | No aparece éxito de guardado falso; se muestra error o conserva el último snapshot |

**Limitación de alcance:** estas comprobaciones cubren **lectura y consistencia**
para todos los módulos que la API ya expone. No crean endpoints CRUD inexistentes
ni convierten vistas de consulta en acciones editables; consultar la matriz
`COMPATIBILITY_MATRIX_RC3.md` para operaciones que sí están implementadas.

## Validación técnica en Android Studio

- Gradle Sync: sin errores; `Build > Make Project` y `./gradlew.bat :app:assembleDebug`.
- Kotlin/Compose: navegar por todas las tabs y perfiles (administrador, gerente,
  técnico, calidad, mantenimiento); no cierres sesión entre actualizaciones.
- Logcat: verificar las peticiones a `/integration/bootstrap`; revisar 401,
  403 y fallos de red, si aparecen.
- Comprobar antes del release: HTTPS, `API_BASE_URL` de producción, red física,
  exclusión de datos demo y secretos del APK, firma y permisos de Android.

## Conteos SQL (solo lectura)

```sql
SELECT organization_id,
       COUNT(*) FILTER (WHERE role NOT IN ('organizationAdmin', 'platformAdmin')) AS empleados,
       COUNT(*) FILTER (WHERE role = 'organizationAdmin') AS gerentes,
       COUNT(*) AS cuentas_totales
FROM public.users
GROUP BY organization_id
ORDER BY organization_id;
```

Si el esquema usa valores de rol distintos, primero consultar `SELECT DISTINCT role FROM public.users;`.
