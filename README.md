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

> **Current development status:** SafeLab Mobile is in active development. Authentication, account registration, monitoring, and other existing screens currently run with local data. Account email verification, payment processing, remote device control, and production security controls require backend and external-service integrations. Do not use the current client to protect real clinical data or operate physical equipment.

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

At this stage, the preconfigured administrator account in the local repository is used to explore the existing app. Registered accounts remain **pending activation** and cannot sign in until the future activation service is connected. See `identityaccess/data/local/IdentityAccessMockData.kt` for the current local credentials and test account configuration. **Do not reuse sample credentials or local in-memory password handling in production.**

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

The repository currently contains eight established BC implementations plus four empty module placeholders for upcoming feature branches.

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
| User Profile Management | `userprofiles` | Placeholder — feature pending |
| Subscription & Billing | `subscriptionbilling` | Placeholder — feature pending |
| Environmental Compliance Management | `environmentalcompliance` | Placeholder — feature pending |
| Remote Control & Actuation | `remoteactuation` | Placeholder — feature pending |

**Why are some folders still using their earlier names?** This baseline deliberately preserves all working Kotlin package paths, Compose screens, navigation contracts, and repositories. Business naming will be synchronized with the web frontend and backend in a dedicated refactor, not by renaming folders before their dependencies are updated.

Each new directory includes only a `.gitkeep` file, ensuring Git can retain the folder. Implementations, models, screens, routes, and service integrations will be introduced in their respective feature branches.

## Planned capabilities

- **User Profile Management:** view registered personal details; edit permitted fields through an explicit edit / save / cancel flow with input validation. Authentication credentials and access permissions remain owned by IAM.
- **Subscription & Billing:** read-only access to the active subscription, payment history, upcoming invoice, and individual payment details. Account activation will follow an external email-verification-and-payment flow. The mobile interface will not directly change plans or execute payments.
- **Environmental Compliance Management:** evaluate measured conditions against operational and preservation rules, record compliance status, and expose results to other modules.
- **Remote Control & Actuation:** display and eventually submit authorized device-control requests through a backend service, with safety checks and auditability. No physical actuation is implemented in this baseline.

Future integration will replace mock data with authenticated REST API calls. Business rules and authorization must be enforced by the backend, not only by the Android UI.

## Development workflow

- **`main`** — stable release-ready changes.
- **`develop`** — shared integration baseline, including working navigation and BC boundaries.
- **`feature/*`** — isolated development of new BCs, screens, and enhancements.
- **`archive/*`** — historical coursework or completed development branches kept for reference.

Create a feature branch from the latest `develop`, keep the scope limited to one feature or BC, and merge through a pull request after code review and successful testing.

## Roadmap

1. Stabilize the shared BC catalog and progressively complete the missing mobile interfaces.
2. Align SafeLab Web and SafeLab Mobile around common domain contracts and terminology.
3. Deliver backend APIs, persistence, IAM, subscription information, compliance evaluation, and authorized IoT integrations.
4. Connect both clients to the backend, test the end-to-end flows, and prepare production-oriented deployment and demonstration.

## License

See [LICENSE](LICENSE) for the repository's license terms.
