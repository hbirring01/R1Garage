# R1 Garage

[![Latest Release](https://img.shields.io/github/v/release/hbirring01/R1Garage?include_prereleases&sort=semver&label=release&color=0B57D0)](https://github.com/hbirring01/R1Garage/releases/latest)
[![Release Date](https://img.shields.io/github/release-date/hbirring01/R1Garage?color=0B57D0)](https://github.com/hbirring01/R1Garage/releases/latest)
[![APK Downloads](https://img.shields.io/github/downloads/hbirring01/R1Garage/total?color=0B57D0&label=APK%20downloads)](https://github.com/hbirring01/R1Garage/releases)
[![Android CI](https://github.com/hbirring01/R1Garage/actions/workflows/android.yml/badge.svg?branch=main)](https://github.com/hbirring01/R1Garage/actions/workflows/android.yml)
[![CodeQL](https://github.com/hbirring01/R1Garage/actions/workflows/codeql.yml/badge.svg?branch=main)](https://github.com/hbirring01/R1Garage/actions/workflows/codeql.yml)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/hbirring01/R1Garage/badge)](https://scorecard.dev/viewer/?uri=github.com/hbirring01/R1Garage)

Personal ownership dashboard for the Rivian R1S (Android).

<!-- LATEST_RELEASE:START -->
### 📦 Latest release: [`v0.1.3`](https://github.com/hbirring01/R1Garage/releases/tag/v0.1.3) — 2026-05-26

[⬇️ Download `R1Garage-v0.1.3.apk`](https://github.com/hbirring01/R1Garage/releases/download/v0.1.3/R1Garage-v0.1.3.apk) · [Release notes](https://github.com/hbirring01/R1Garage/releases/tag/v0.1.3)

```
adb install R1Garage-v0.1.3.apk
```
<!-- LATEST_RELEASE:END -->

Four pillars:

| Pillar | What it does |
| --- | --- |
| **Drive** | Auto-detects trips, logs distance / kWh / efficiency, builds lifetime stats |
| **Charge** | Auto-detects charge sessions; TOU-aware optimizer recommends cheapest start time |
| **Alerts** | Smart push notifications for window-down + rain, unexpected unlock, vampire drain, low 12V, etc. |
| **Garage** | Service intervals from live odometer, modification log, NHTSA recall feed |

## Status

This is the initial scaffold. Everything compiles and runs but most features
are stubs. The shape of the project is set; the work that remains is
business logic.

### Done
- Gradle / Kotlin / Compose / Hilt / Room / WorkManager / Retrofit wiring
- Material 3 theme (Google blue seed, dynamic color on Android 12+) — same
  palette as the sibling `CreditCardApp/` project
- Bottom-nav scaffold with 5 screens (Now, Drive, Charge, Alerts, Garage)
- Room schema for trips, charge sessions, mods, alerts, vehicle snapshots
- Encrypted token store for the Rivian session
- Retrofit + OkHttp client pointed at the unofficial Rivian GraphQL gateway
- Periodic WorkManager poller (15 min cadence)

### TODO (in suggested build order)
1. **Rivian auth flow** — login + OTP/CAPTCHA handling, then store tokens via
   `RivianTokenStore`. Until this lands the poller just no-ops and the UI
   shows "Not signed in".
2. **Trip detector** — diff successive snapshots, emit `Trip` rows when gear
   transitions to PARK.
3. **Charge detector** — same diff approach against plug state + SoC delta.
4. **Alert rules engine** — pluggable rules evaluated on each poll, writing
   to `AlertEventDao` and firing a `NotificationManager` notification.
5. **Service intervals** — replace the placeholder `nextService()` in
   `GarageViewModel` with the real Rivian-recommended schedule.
6. **NHTSA recall feed** — periodic call to `api.nhtsa.gov` by VIN.
7. **TOU optimizer** — pull rate plan from OpenEI URDB, compute optimal start.
8. **Settings screen** — theme mode, vehicle selection, sign-out, TOU plan.
9. **Widgets + pull-to-refresh + charts.**

## Building

Requires the same toolchain as the CreditCardApp:
- Android Studio Ladybug+ / AGP 9.2.x
- JDK 17
- Gradle 9.5.1 (downloaded by the wrapper)

The Gradle wrapper binary (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`)
is **not** included in this scaffold. Copy them from the sibling
`CreditCardApp/` project, or run `gradle wrapper --gradle-version 9.5.1`
from this directory.

```powershell
# from the workspace root
Copy-Item ..\CreditCardApp\gradlew .
Copy-Item ..\CreditCardApp\gradlew.bat .
Copy-Item ..\CreditCardApp\gradle\wrapper\gradle-wrapper.jar gradle\wrapper\
```

Then:

```powershell
.\gradlew :app:assembleDebug
```

On Windows, builds are redirected to `C:/CCBuild/r1garage-app` when that
folder exists, same trick as `CreditCardApp` to avoid OneDrive locking
the build outputs.

## Package layout

```
com.r1garage.android
├── R1GarageApplication.kt   Hilt entry, schedules the poller
├── MainActivity.kt          Compose host
├── ui/
│   ├── theme/               Material 3 colors, type, shapes
│   ├── navigation/          Bottom-nav routes
│   ├── components/          Reusable cards
│   ├── home/                "Now" dashboard
│   ├── drive/               Trip list + stats
│   ├── charge/              Charge sessions + optimizer hints
│   ├── alerts/              Alert event feed
│   └── garage/              Service / mods / recalls
├── data/
│   ├── local/               Room entities + DAOs + AppDatabase
│   ├── preferences/         DataStore-backed prefs
│   ├── rivian/              Unofficial Rivian GraphQL client
│   └── repository/          VehicleRepository (the seam)
├── domain/model/            UI-facing data shapes
├── di/                      Hilt modules (Database, Network)
└── work/                    WorkManager poller
```

## Free / one-time services used

| Service | Cost |
| --- | --- |
| Rivian unofficial GraphQL | Free (community-maintained) |
| Open-Meteo (weather, precip) | Free, no key |
| USGS Elevation | Free |
| OpenEI URDB (utility TOU rates) | Free |
| NHTSA recalls | Free |
| Google Play Console (only if publishing) | $25 one-time |
