# Somers Launcher (PR-1 merge-ready skeleton)

## What this PR-1 implements

This repository provides the **PR-1 foundation** for a preinstalled Android POS launcher onboarding flow.

Implemented stack:
- Kotlin + Jetpack Compose, single-activity app.
- Layered structure:
  - `ui` (screens)
  - `presentation` (state/actions/viewmodel orchestration)
  - `domain` (models/interfaces)
  - `data` (DataStore + mocks)
  - `core/logging` (audit logger)

## Flow implemented (mock end-to-end)

Startup gate checks persisted activation state:
- `activated=false` -> onboarding flow
- `activated=true` -> pass-through already-activated placeholder

Onboarding stages:
1. Welcome
2. Language selection
3. Network setup (mock Wi-Fi UX)
4. Activation in progress (fake rotating statuses)
5. Completed (then optional pass-through transition)
6. Error screen (for activation or connection failures)

Back behavior stays in-app and routes to previous logical stage.

## Persistence

DataStore file: `launcher_state.preferences_pb`

Stored keys:
- `activated: Boolean`
- `language: String` (app locale code)
- `network_mode: WIFI | MOBILE`

Language fallback defaults to Russian when stored value is absent/invalid.

## Locale handling

PR-1 now uses Android string resources for app UI text, with per-locale `strings.xml` files.
Russian is intentionally provided via default `values/strings.xml` (Android standard fallback), not a separate `values-ru/`.

Supported launcher locales:
- Russian (`ru`) — default/fallback
- English (`en`)
- Tajik (`tg`)
- Kyrgyz (`ky`)
- Armenian (`hy`)
- Uzbek (`uz`)

Runtime app-locale switching is applied through `AppCompatDelegate.setApplicationLocales(...)`.
System locale changes remain out of scope.

## Mocked services in PR-1

- `MockWifiManager` (network list/refresh/connect states)
- `MockConnectivityChecker` (wifi/mobile internet availability)
- `MockActivationClient` (deterministic success/failure)
- `MockHandoffManager` (handoff attempt stub)

No vendor SDK integration in this PR.

## Audit logging

JSONL audit log target:
- Primary: app-specific external files directory: `<external-files>/audit/launcher_audit.jsonl`
- Fallback: internal app files directory: `<internal-files>/audit/launcher_audit.jsonl`

Important operational note:
- App-specific external storage is usually retrievable via USB/ADB/device service tooling.
- Visibility in consumer file-manager apps may vary by device policy and Android storage restrictions.

## Deferred intentionally (PR-2 / PR-3)

- Vendor SDK integration (ANFU/NewPOS/Newland)
- Real Wi-Fi scan/connect and SIM/mobile detection
- Real activation-agent API integration
- Boot receiver/default launcher/kiosk HOME enforcement
- Real post-activation launcher disabling
- Real target app launch/autostart policy integration
- Final branding/animations assets
