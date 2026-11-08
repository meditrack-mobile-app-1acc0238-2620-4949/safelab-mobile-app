# Matriz de compatibilidad SafeLab v2.5.0 — RC3

Esta matriz compara **operaciones soportadas por el backend actual**, no promete CRUD universal de todas las colecciones. La autorización por token, recurso y organización se decide en el backend.

| Colección / flujo | Lectura Web/Mobile | Operaciones de escritura probables | Android RC3 | Limitación relevante |
|---|---|---|---|---|
| IAM y sesión | Sí | Login/logout, invitaciones, permisos, asignaciones | Login/invitaciones/permisos/asignaciones existentes | Password recovery y flujos externos incompletos |
| Sedes / facilities | Sí | GET | Consulta | Sin creación/edición de sedes en esta API |
| Equipos / assets | Sí | POST, PATCH, DELETE | Crear, editar, eliminar con confirmación | DELETE 409 si hay sensores, mantenimiento u otros vínculos |
| Sensores | Sí | POST, PATCH, PATCH reading, DELETE | Crear, editar, registrar lectura, eliminar | DELETE 409 con relaciones existentes; gerente sin `sensors.configure` por política |
| Mantenimiento | Sí | POST, PATCH | Crear orden, editar registro | No DELETE en API |
| Alertas | Sí | Acknowledge, resolve, escalate | Acciones autorizadas en menú y pantalla original | No POST arbitrario de alertas: se generan por lecturas |
| Incidentes | Sí | POST, investigate, resolve, close | Crear y cambiar etapa | No DELETE genérico |
| Cumplimiento | Sí | PATCH reglas, POST reglas | Alternar reglas existentes | Alta de reglas disponible en API, pero no integrada en formulario Android |
| Control remoto | Sí | POST comandos | Acción de mando simulada | No control de hardware real |
| Reportes | Sí | POST reporte simulado | Alta de registro | No genera PDF real |
| Auditoría | Sí | GET | Consulta | Inmutable desde el cliente |
| Suscripciones | Sí | endpoints sujetos a plataforma | Consulta | No pagos reales desde móvil |
| Perfil usuario | Sí | PATCH me | Edición en perfil | Cambio de email restringido |

## Casos mínimos de aceptación

| Caso | Rol | Acción | Resultado esperado |
|---|---|---|---|
| QA-01 | Gerente | Crear activo en Web; ir a otro módulo en Mobile y volver | Visible sin cerrar sesión |
| QA-02 | Gerente | Editar ubicación en Mobile; volver a activos en Web | Misma ubicación en Web y PostgreSQL |
| QA-03 | Gerente | Cambiar responsable desde dropdown Web/Mobile | `responsible_user_id` y `users.resource_ids` coherentes |
| QA-04 | Gerente | Eliminar activo sin dependencias en Mobile | Registro deja de existir en Web/SQL después de refrescar |
| QA-05 | Gerente | Eliminar activo con sensor asociado | HTTP 409; activo permanece |
| QA-06 | Cuenta con `sensors.configure` | Editar umbral/lectura en Mobile | Cambios persistidos y alertas si corresponde |
| QA-07 | Técnico sin `assets.manage` | Consultar equipo y buscar Edit/Delete | Solo lectura y endpoint denegado con 403 si se fuerza |
| QA-08 | Gerente empresa A | Intentar editar recurso empresa B | 403; sin fuga de datos ni cambios |
| QA-09 | Usuarios con diferentes resourceIds | Comparar contadores en Web/Mobile y consultas SQL | Coinciden tras sincronización |
| QA-10 | Todos | Desconectar el backend y refrescar Mobile | Error visible y reintento, sin éxito falso |
| QA-11 | Todos | Pasar Android a segundo plano y regresar | Se actualiza el snapshot y las métricas |

Para los detalles de integridad de la base de datos: `database/diagnostics/verify_rc3_relations.sql` del backend.
