# Finorix Signals 🐂

Advanced AI-powered trading signals terminal for Android.

## Features
- **Flash Signal Engine**: Real-time analysis with >80% confidence scoring.
- **Automated Outcomes**: Background verification of signal success (WIN/LOSS) using live market data.
- **Cloud Sync**: Firestore-backed signal history and user preferences.
- **FCM Push Notifications**: Instant alerts for high-confidence signals.
- **Premium UI**: Neon-green glow aesthetics with smooth animations and edge-to-edge display.

## Development Setup
1. Clone the repository.
2. Add your `google-services.json` to `app/`.
3. Add your `OPENROUTER_API_KEY` to `local.properties`.
4. Run `./gradlew assembleDebug` to build.

## Beta Distribution
To share the app with beta testers:

1. **Setup Testers**:
   - Go to [Firebase Console](https://console.firebase.google.com/) -> App Distribution.
   - Create a group named `beta-testers`.
   - Add tester emails.

2. **Deploy**:
   ```bash
   ./gradlew assembleRelease appDistributionUploadRelease
   ```

## Landing Page
The project includes a premium landing page deployed at:
**[https://fironix-app.web.app](https://fironix-app.web.app)**

To redeploy the web terminal:
```bash
firebase deploy --only hosting
```

## Tech Stack
- **UI**: Jetpack Compose + Material 3
- **DI**: Hilt
- **Database**: Room (Local) + Firestore (Cloud)
- **Networking**: Retrofit + OKHttp
- **Background**: WorkManager + Foreground Services
- **Analytics**: Firebase Analytics + Crashlytics
