# Parla Relay / پارلا کنترل

A new native Android controller inspired by the feature set of Smart Simote and redesigned around the visual identity of [Parla Sazan](https://parlasazan.com/). It uses the official published Parla logo and the site's primary mint/green color system.

## Product structure

- Persian-first, right-to-left UI with three clear destinations: Home, Access, and Settings.
- Two independent relay controls with user-defined labels and activation durations.
- Controller status, SIM balance, direct call, and prepaid recharge flows.
- Authorized-phone and remote-ID management (IDs 1–500).
- Independent device-reporting controls for remote, caller, administrator SMS, and user SMS events.
- Controller date/time sync using the Solar Hijri date expected by the original protocol.
- Controller language, calendar, password, balance formula, and firmware-version commands.
- Optional SMS preview with masked phone number and hidden controller PIN.
- Four-digit local app PIN plus Android biometric login on Android 9+.

## Security improvements over the reference APK

- The controller phone number, controller PIN, balance formula, relay labels, and timings are encrypted with an Android Keystore AES-GCM key.
- The app PIN is stored as a salted PBKDF2-HMAC-SHA256 verifier, never as plaintext.
- App backup and device-transfer extraction of preferences are disabled.
- Destructive bulk operations require a second confirmation.
- Repeated local PIN failures cause a temporary lock instead of silently resetting credentials.
- The launcher activity is the only exported component.

The controller protocol itself still travels over ordinary SMS because compatibility with the existing hardware requires it. SMS is not end-to-end encrypted by this app.

## Build

Requirements:

- Android Studio with JDK 17
- Android SDK Platform 35 and Build Tools 35.0.0

Build from Android Studio, or run:

```bash
./gradlew assembleDebug
```

The project uses Android Gradle Plugin 8.8.2 with Gradle 8.10.2. It has no runtime third-party dependencies.

## Key source files

- `DashboardActivity.java` — main RTL dashboard, access management, and settings UI
- `DeviceGateway.java` — permission-aware SMS and call dispatch with confirmations
- `CommandProtocol.java` — validated controller-command generation
- `SecureStore.java` — encrypted settings and hashed app PIN
- `PersianDate.java` — dependency-free Gregorian-to-Solar-Hijri conversion

## Device command compatibility

Outgoing messages are composed as `<controller PIN><command>`, matching the reference app. The implementation covers relay control, status/balance checks, phone and remote management, reporting options, relay names/times, controller date/time, language/calendar, version request, password change, and recharge settings.
