# OTP Bridge

Personal Android utility that extracts OTP/verification codes from SMS and optionally Gmail, then exposes the latest code through Android Autofill.

## SMS mode

This personal/sideload build uses Android's `READ_SMS` + `RECEIVE_SMS` permissions. The app:

- asks for the user's mobile number as an app setting;
- requests SMS permissions at runtime;
- scans the SMS inbox for an OTP-like message;
- listens for incoming SMS and extracts an OTP-like code;
- stores only the latest extracted code locally;
- exposes the latest code through the Android Autofill Service.

The app does not upload SMS contents or OTPs to a server.

## Gmail mode

Gmail access is optional and uses the read-only Gmail scope. A Google Cloud OAuth configuration is required for a working Gmail integration.

## GitHub Actions

Every push/PR to `main` runs the Android debug build and publishes the APK as a workflow artifact. The workflow can also be started manually from the Actions tab.

For GitHub Actions Gradle guidance, see the official Gradle Actions documentation.
