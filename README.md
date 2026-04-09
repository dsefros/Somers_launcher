# Somers Launcher (PR-2 device integration layer)

## What PR-2 makes real

This PR extends the merged PR-1 skeleton with real Android device-side integration points while keeping activation-agent business contract integration deferred to PR-3.

Implemented in PR-2:
- Vendor-aware strategy layer for ANFU / NewPOS / Newland with explicit fallback and TODO-safe adapters.
- Real Android Wi-Fi scan/connect integration (`WifiManager`, `ConnectivityManager`) behind domain ports.
- Real internet reachability checks for Wi-Fi and mobile transports using multiple public endpoints.
- Controlled launcher behavior primitives:
  - portrait-only activity
  - in-app back routing for onboarding
  - keep-screen-awake during activation stage
  - temporary launcher-role extension points (prepared, partially deferred)
- Real app handoff primitive by package / explicit activity with structured failure mapping.
- Hardened JSONL audit logging with file rotation.
- Config object for target app, reachability endpoints, vendor override, and feature flags.

## Vendor abstraction structure

- `domain/Ports.kt`: core interfaces (`VendorSystemControl`, `VendorStrategySelector`, `WifiManager`, `ConnectivityChecker`, `HandoffManager`).
- `data/vendor/BuildVendorStrategySelector.kt`: runtime vendor resolution from build fingerprint (or config override).
- `data/vendor/VendorSystemControls.kt`: default + ANFU + NewPOS + Newland adapters.

> Important: vendor SDK-specific method calls are intentionally not faked. Unknown SDK paths are marked as TODO and return explicit non-success `SystemActionResult` values.

## Real network behavior notes

- Wi-Fi scanning/connection uses platform APIs and respects platform permission/policy constraints.
- Android 13+ requires runtime `NEARBY_WIFI_DEVICES`; Android 12 and below use runtime `ACCESS_FINE_LOCATION` for scan/connect discovery behavior.
- Before requesting runtime permission, the network step remains in an explicit not-yet-requested state; after denial it shows a permission-required state (not a misleading empty network list).
- Android 10+ connection path uses `WifiNetworkSpecifier` request flow (ephemeral request semantics may vary by OEM policy).
- Internet reachability checks test multiple endpoints and are transport-specific where possible.
- UI state distinctions are preserved:
  - selected/not connected
  - connecting
  - connected with internet
  - connected without internet
  - connection error

## Logging strategy

Audit logs remain JSONL and are written to:
- Primary: `<app external files>/audit/launcher_audit.jsonl`
- Fallback: `<app internal files>/audit/launcher_audit.jsonl`

Rotation:
- current log rotates at ~1 MB to `launcher_audit.prev.jsonl`.

Storage visibility still depends on device policy and Android scoped-storage behavior.

## Manual validation checklist (real devices)

### ANFU
- Verify vendor detection resolves to `ANFU`.
- Verify Wi-Fi scan/list/connect flow with secure and open AP.
- Verify activation screen keeps display awake.
- Verify attempted controlled-mode logs and TODO result details.
- Verify target app launch by package and explicit activity failure handling.

### NewPOS
- Verify vendor detection resolves to `NEWPOS`.
- Validate network scan/connect and internet reachability transitions.
- Validate back button remains in launcher flow during onboarding.
- Verify vendor action logs indicate deferred SDK hooks.

### Newland
- Verify vendor detection resolves to `NEWLAND`.
- Validate onboarding gate behavior (`activated=false` enters onboarding, `activated=true` pass-through).
- Validate mobile-skip enablement only when mobile internet check is true.
- Verify handoff success/failure events are logged.

## Deferred to PR-3

- Final activation-agent API integration and business response parsing.
- Final post-activation launcher disable flow that depends on activation-agent result contract.
- Vendor SDK deep integrations for full kiosk/hard lock behavior.
- Full production rollout/release hardening and branding assets.
