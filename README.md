# Open Jarvis

An Android-native personal assistant project built with Kotlin and Jetpack Compose. Target: Android 11+ (API 30), suitable for development and use from an Android phone.

## Implemented in the current source

- Compose dashboard, AI chat screen, provider configuration and voice-chat entry.
- OpenAI-compatible Chat Completions client; endpoint, model ID and API key are configurable.
- API credentials, recent conversation, and user-managed memory notes encrypted with an Android Keystore AES-GCM key.
- Offline command router: date/time, battery, device/API information, Settings, Wi-Fi and Bluetooth settings, camera, Calendar, Calendar event form, browser, Maps search, and supported app launch shortcuts (YouTube, WhatsApp, Gmail, Chrome).
- Personal task list with local persistence, completion/deletion actions, date/time reminder picker, scheduled Android alarm notifications, and reminder cancellation on task completion/deletion.
- User-controlled clear-chat action removes the encrypted local conversation history and resets the visible chat session.
- Notification listener requiring the user to grant Android Notification Access. Current implementation records notification count, package name and timestamp only; it intentionally does not persist notification text.
- GitHub Actions Android CI runs unit tests and assembles a debug APK artifact.

## Feature status — not all requested Jarvis capabilities are finished

The following are **not yet implemented as production-ready modules**: always-on offline “Hey Jarvis” wake-word detection; persistent background voice assistant; true multi-provider profile management/model switching; screen capture and OCR; floating overlay; notification summarization/intelligence beyond metadata; scheduled task alarms; permission-aware deep app control; smart automation routines; offline conversational model; study/PDF/document reader; web research and daily briefing; calendar/reminder sync; Termux/Python/Node integration; GitHub coding agent/APK builder; multi-agent/MCP/skills; biometric app lock and a complete security settings screen.

These require separate implementation, permission UX, device testing, and feature-specific verification. A dashboard button or roadmap item is not evidence that a feature works.

## Build

GitHub Actions workflow: `.github/workflows/android.yml`

CI command: `gradle --no-daemon testDebugUnitTest assembleDebug`

Download the debug APK from the successful Android CI run's `open-jarvis-debug` artifact. API keys must never be committed to the repository.
