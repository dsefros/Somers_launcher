# Somers Launcher (PR-3 activation-ready flow)

## What PR-3 now makes real

PR-3 completes the real activation flow from startup to target-app handoff:

- `startup gate -> onboarding -> network setup -> real activation API call -> success policy -> handoff`
- Activation success now persists `activated=true`, attempts handoff to configured target app, and routes launcher into pass-through mode.
- On next app launch, `activated=true` skips onboarding and goes directly to pass-through behavior.
- Activation failures do **not** set `activated=true`; they show localized, user-safe error text while keeping response diagnostics in logs.
- Activation network behavior now maps API errors, transport failures, request timeout, and malformed JSON responses explicitly.
- Target app selection is **config-only** (build config); activation API does not override handoff target.
- Activation transport uses an intentional **empty-body POST** handshake with explicit JSON headers (`Accept` + `Content-Type: application/json`).

## Runtime configuration required

Configuration is build-config driven (see `app/build.gradle.kts` defaults):

- `ACTIVATION_ENDPOINT` (full URL for activation API POST)
- `ACTIVATION_TIMEOUT_MS` (request timeout in milliseconds)
- `TARGET_APP_PACKAGE` (handoff package)
- `TARGET_APP_ACTIVITY` (optional explicit activity, empty string means package launch)

Override mechanism (real and supported):

- Put values in `~/.gradle/gradle.properties` or project `gradle.properties`, or pass via CI/CLI `-P...`.
- Supported Gradle properties:
  - `somers.activationEndpoint`
  - `somers.activationTimeoutMs`
  - `somers.targetAppPackage`
  - `somers.targetAppActivity`

Example CLI override:

```bash
./gradlew assembleDebug \
  -Psomers.activationEndpoint=https://activation.example.com/api/v1/activate \
  -Psomers.activationTimeoutMs=20000 \
  -Psomers.targetAppPackage=com.example.target \
  -Psomers.targetAppActivity=com.example.target.MainActivity
```

(CI can pass the same `-P` keys; local defaults remain in `app/build.gradle.kts`.)

## Manual verification (real flow)

1. Install launcher with a reachable activation endpoint.
2. Launch app with clean app data.
3. Complete onboarding and network step (Wi-Fi or mobile skip).
4. Confirm activation request reaches backend and returns valid JSON contract.
   - Transport semantics: request is `POST` with explicit JSON headers and an intentionally empty body.
5. On success:
   - launcher persists activation state,
   - handoff is attempted to configured target app (config-only target policy),
   - launcher enters pass-through mode and remains installed as fallback launcher entrypoint.
6. Force close launcher and open again: onboarding must **not** reappear.
7. Failure checks:
   - API business failure (`success=false`) shows localized activation failure.
   - Transport failure / timeout / malformed response each show localized failure text.
   - `activated=true` must remain unset after failures.

## Explicitly deferred

- OEM-specific launcher disable/hard kiosk behavior remains deferred because safe/portable behavior is vendor/policy dependent.
- No fake vendor SDK hooks were added; deferred paths continue to log explicit deferred status.
