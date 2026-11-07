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
