# SafeLab Mobile

**Android application | Kotlin · Jetpack Compose · Domain-Driven Design**

SafeLab Mobile is the Android client of **SafeLab**, a digital ecosystem designed to support the monitoring and operational oversight of clinical laboratories, pharmaceutical storage facilities, and sensitive cold-chain environments. The application provides a mobile-first interface for reviewing operational conditions, equipment health, environmental measurements, alerts, incidents, reporting, and traceability.

This repository hosts the **mobile presentation layer** of the SafeLab ecosystem. It is intended to evolve alongside the SafeLab web application and a shared backend API. Web and mobile will use the same business domain boundaries while providing experiences tailored to their respective platforms.

## Project at a glance

| Area | Current approach |
| --- | --- |
| Platform | Android (native) |
| Language | Kotlin |
| Interface | Jetpack Compose and Material 3 |
| Architecture | Modular structure organized by Bounded Context (DDD) |
| Navigation | Native bottom navigation with secondary destinations |
| Localization | English / Spanish interface (being expanded) |
| Appearance | Light and dark themes |
| Data integration | Local and in-memory sample data; shared REST API planned |

> **Current development status:** SafeLab Mobile is in active development. Authentication, account registration, monitoring, and other existing screens currently run with local data. Account email verification, payment processing, live remote device control, and production security controls require backend and external-service integrations. Do not use the current client to protect real clinical data or operate physical equipment.

## Getting started

### Requirements

- **Android Studio** with Android SDK Platform **35** installed.
- **JDK 17** configured as the Gradle JDK in Android Studio.
- Android emulator or physical Android device running **Android 9 (API 28)** or later.
- Internet access for the first Gradle synchronization, if dependencies are not cached.

The project uses **Android Gradle Plugin 8.9.2**, **Kotlin 2.0.21**, and the **Gradle 8.11.1 wrapper**. Use the included wrapper instead of installing a separate Gradle version.

### Run locally

1. Clone the repository or download and extract its ZIP archive.
2. Open **Android Studio → File → Open** and select the project root (the folder containing `settings.gradle.kts`).
3. Allow **Gradle Sync** to complete and install any missing SDK components as prompted.
4. Select the **app** run configuration and a compatible Android emulator or physical device.
5. Click **Run**.

To build from the command line:

```bash
# macOS / Linux / Git Bash (with a suitable Java and Android SDK environment)
./gradlew assembleDebug

# Windows Command Prompt / PowerShell
# gradlew.bat assembleDebug
```

A successful build produces an Android debug APK under `app/build/outputs/apk/debug/`.

### Local access

At this stage, the preconfigured administrator account in the local repository is used to explore the existing app. Registered accounts remain **pending activation** and cannot sign in until the future activation service is connected. For a **local demonstration only**, sign in with `admin@hotmail.com` and `SafeLab2026%`. The administrator also has a fixed 18-digit username; see `IdentityAccessMockData.kt`. **Do not reuse sample credentials or local in-memory password handling in production.**

## Repository structure

```text
app/
└── src/main/java/pe/edu/upc/safelab/
    ├── boundedcontext/   # Business modules with isolated responsibilities
    ├── common/           # Shared utilities (e.g., localization)
    ├── navigation/       # Mobile navigation and app composition
    ├── shared/           # Reusable presentation and domain contracts
    └── ui/               # Theme and visual components
```

### Bounded Contexts

The repository contains twelve BC directories: eight initial mobile foundations and four additional AV2 feature implementations. All modules currently use demonstration data; they are not integrated with a production backend.

| Bounded Context (target domain name) | Directory | Development status |
| --- | --- | --- |
| Identity & Access Management | `identityaccess` | Existing mobile foundation |
| Facility & Asset Management | `monitoringorganization` | Existing foundation; domain rename planned |
| Sensor & Telemetry Monitoring | `sensormonitoring` | Existing foundation; domain refinement planned |
| Equipment Health & Maintenance | `equipmentmaintenance` | Existing foundation; domain refinement planned |
| Alert & Incident Management | `alertsincidents` | Existing mobile foundation |
| Operational Dashboard & Insights | `dashboardoverview` | Existing foundation; domain rename planned |
| Reporting & Analytics | `reportingcompliance` | Existing foundation; domain rename planned |
| Audit & Traceability | `audittraceability` | Existing mobile foundation |
| User Profile Management | `userprofiles` | Editable personal information (offline demo) |
| Subscription & Billing | `subscriptionbilling` | Read-only subscription and billing (offline demo) |
| Environmental Compliance Management | `environmentalcompliance` | Environmental rules and evaluations (offline demo) |
| Remote Control & Actuation | `remoteactuation` | Simulated commands, actuator status and history (offline demo) |

**Why are some folders still using their earlier names?** This baseline deliberately preserves all working Kotlin package paths, Compose screens, navigation contracts, and repositories. Business naming will be synchronized with the web frontend and backend in a dedicated refactor, not by renaming folders before their dependencies are updated.

The four AV2 feature contexts are now implemented with local sample data and their own domain/presentation code. Their future API contracts and persistence will be added after the mobile and web architectures are aligned.

## Demonstration account IDs and data consistency

- Each newly registered account receives a **UUID v4 internal ID** and a **random, immutable, 18-digit numeric username** generated by `SecureRandom`. The email can also be used to sign in.
- Account status remains **pending activation** until a future backend implements verification and payment confirmation. No email is currently sent and no real payments occur.
- The seeded administrator has a stable UUID and username so User Profiles and Subscription & Billing refer to the same account. Do not change these seeds independently.
- Local username collisions are checked and regenerated; the production backend must enforce uniqueness transactionally in its database.
- All passwords, account records and billing entries in this version are **demonstration-only data**; the real backend must hash passwords and protect credentials and payments.
- The dashboard sensor snapshot, environmental violations, alerts, incident references and audit operators are aligned with shared sample identifiers and timestamps. Historic actors are preserved as history, not treated as the current user.

## Planned capabilities

- **User Profile Management:** view registered personal details; edit permitted fields through an explicit edit / save / cancel flow with input validation. Authentication credentials and access permissions remain owned by IAM.
- **Subscription & Billing:** read-only access to the active subscription, payment history, upcoming invoice, and individual payment details. Account activation will follow an external email-verification-and-payment flow. The mobile interface will not directly change plans or execute payments.
- **Environmental Compliance Management:** evaluate measured conditions against operational and preservation rules, record compliance status, and expose results to other modules.
- **Remote Control & Actuation:** view controllers and sample connectivity; simulate predefined commands with confirmation; inspect local command history and sample audit references. The UI rejects offline or unauthorized actions. **No physical command is transmitted.** Future API integration must enforce authorization and safety interlocks on the server and record tamper-resistant audits.

Future integration will replace mock data with authenticated REST API calls. Business rules and authorization must be enforced by the backend, not only by the Android UI.

## Remote actuation demonstration (AV2)

1. Sign in with the local administrator account, then open **More → Remote Control & Actuation**.
2. Visit **Devices**, choose a sample online controller and select an available action. The confirmation dialog explains that this is an **offline simulation**.
3. Confirm the simulation to update the temporary device mode and create a `SIM-###` record; inspect it under **History**. Offline controllers and unauthorized roles cannot submit simulations.

The seeded `CMD-079` to `CMD-081` references are fictitious records aligned with the existing Audit & Traceability sample. New `SIM-###` records are stored only in the local repository and are **not** appended to the Audit & Traceability BC yet. This cross-context audit integration belongs to the future backend. **Never use this version to operate clinical equipment.**

## Development workflow

- **`main`** — stable release-ready changes.
- **`develop`** — shared integration baseline, including working navigation and BC boundaries.
- **`feature/*`** — isolated development of new BCs, screens, and enhancements.
- **`archive/*`** — historical coursework or completed development branches kept for reference.

Create a feature branch from the latest `develop`, keep the scope limited to one feature or BC, and merge through a pull request after code review and successful testing.

## Roadmap

1. Validate and refine the twelve mobile BC foundations and their bilingual UI.
2. Align SafeLab Web and SafeLab Mobile around common domain contracts and terminology.
3. Deliver backend APIs, persistence, IAM, subscription information, compliance evaluation, and authorized IoT integrations.
4. Connect both clients to the backend, test the end-to-end flows, and prepare production-oriented deployment and demonstration.

## License

See [LICENSE](LICENSE) for the repository's license terms.
