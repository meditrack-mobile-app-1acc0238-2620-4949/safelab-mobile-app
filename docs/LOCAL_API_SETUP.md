# SafeLab Mobile — ejecución API local

1. Inicia PostgreSQL y Spring Boot con el perfil `postgres` en `http://localhost:8080`.
2. Abre el proyecto en Android Studio y selecciona variante **debug**.
3. Usa el emulador Android oficial: desde allí la máquina host es `10.0.2.2`. La URL está en `app/build.gradle.kts` → `buildConfigField("String", "API_BASE_URL", ...)`.
4. El build debug permite HTTP local únicamente para las pruebas. El build release usa `https://example.invalid/api/v1` como marcador deliberado y NO está listo para producción.
5. Pulsa ▶ en Android Studio. Inicia sesión con cuentas de prueba solo si se importó la semilla PostgreSQL.
6. Al recuperar el primer plano, la app descarga de nuevo el snapshot autorizado. No existe sincronización push instantánea.
7. Para teléfono físico usa una IP LAN de tu PC, configura firewall/red y una URL permitida por tu network security config de debug; nunca uses `10.0.2.2` en teléfono físico.
8. Si necesitas comparar la antigua demo sin API, cambia `USE_LOCAL_MOCKS` a `true` en build debug y reconstruye. No lo habilites en un APK de producción.

Los errores HTTP/validaciones proceden de Spring Boot. Consultar `INTEGRATION_TEST_PLAN.md` para pruebas cruzadas y limitaciones.

## Cliente HTTP y operaciones PATCH

La variante `debug` utiliza **OkHttp 4.12.0** para soportar los métodos HTTP, incluyendo `PATCH`, que no está soportado por `HttpURLConnection` de forma portable. La primera sincronización puede requerir que Android Studio descargue la dependencia.

Los formularios anteriores no pedían siempre una sede: el adaptador utiliza una instalación autorizada del usuario para los registros iniciales. Revisa la sede elegida en PostgreSQL y adapta el formulario cuando haya más de una instalación.
