# Open Jarvis

A modular, Android-native personal AI assistant built with Kotlin and Jetpack Compose.

## Device target
- Android 11+ (minSdk 30)
- Kotlin + Jetpack Compose
- Cloud AI providers with a lightweight offline-capable core
- User-controlled permissions for microphone, notifications, accessibility, overlay and screen capture

## Build
The GitHub Actions workflow builds the debug APK using Gradle and Android SDK.

## Development principles
- No API keys committed to source control.
- Sensitive actions require explicit user confirmation.
- Android permissions are requested transparently and can be revoked by the user.
- Features are implemented incrementally and tested; a visible control is not considered functional until its action works.

## Planned modules
Voice, conversation, AI providers, memory, device actions, notifications, screen vision/OCR, automation, study tools, documents, research, developer tools, MCP and security.
