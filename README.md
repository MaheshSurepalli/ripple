# 🌊 Ripple — Viral Social Challenge App (Android)

> **“How far can something I started travel?”**

Ripple is a social challenge app where one person starts a simple challenge and sends it to a small number of friends. Each participant must complete the challenge before unlocking and seeing other participants' responses. After completing it, that participant passes the challenge to 3 more people, creating an exponential viral propagation chain:

$$\mathbf{1 \to 3 \to 9 \to 27 \to 81 \to 243 \dots}$$

---

## ✨ Features

- **🔑 Token-Centric Branching**: Unique single-use invite tokens (`https://ripple.app/i/{inviteToken}`) tied to exact parent-child nodes for complete tree reconstruction.
- **🔒 Backend-Enforced Reveal Privacy**: Recipient devices only see blurred teasers and metadata. Inviter responses are strictly protected by Firestore & Storage Security Rules until an atomic submission is verified.
- **⚡ Atomic Server Mutations**: Enforces single-use tokens, anti-cheating duplicate checks, generation incrementation ($G_{child} = G_{parent} + 1$), and aggregate stats updates in Firestore transactions.
- **📸 Camera & Text Challenges**: Fast photo snap and text responses with optional approximate location tagging (coarse city/country only; zero exact GPS coordinates stored).
- **💥 Dopamine Unlock Animation**: Biomorphic particle bursts upon submission that reveal the inviter's secret photo/text.
- **🌳 Propagation Tree Visualizer**: Visual generation breakdown ($0 \to 1 \to 2 \dots$) and node inspection bottom sheet.
- **📊 Real-Time Viral Analytics**: Live viral coefficient ($K$), total kilometers traveled, generation distribution, and city/country counts.
- **🏆 Shareable Milestone Story Cards**: Exportable high-contrast 9:16 vertical cards (*"MY RIPPLE JUST HIT 100 PEOPLE"*, *"Reached 12 Countries"*, *"Survived 20 Generations"*) formatted for Instagram Stories, WhatsApp Status, and Snapchat.
- **🐞 Built-In Multi-User Simulator**: In-app debug harness to switch between `@siva`, `@alex`, `@john`, `@sarah`, and `@elena` with 1-tap, and simulate live participants joining from around the world.
- **🛡️ Safety & Content Moderation**: Automated prompt safety filters, user reporting, and blocking.

---

## 🏗️ Architecture & Tech Stack

- **UI Layer**: 100% Jetpack Compose with Material 3, custom Oceanic bioluminescent theme (`OceanNight`, `RippleCyan`, `RippleTeal`, `RippleAqua`, `RippleCoral`), and CameraX viewfinder.
- **Domain Layer**: Clean architecture with Kotlin Coroutines, StateFlow, Use Cases, and domain models.
- **Backend & Cloud**:
  - **Firebase Project**: `ripple-viral-prod`
  - **Cloud Firestore**: Atomic transactions, batch writes, and server-authoritative security rules (`firestore.rules`).
  - **Cloud Storage**: Secure media uploads (`storage.rules`).
  - **Firebase Authentication**: Google Sign-In & Phone Auth.
- **Deep Linking**: Android App Links (`https://ripple.app/i/*`) & Custom Scheme (`ripple://invite/*`) with deferred install-referrer fallback.

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Android SDK API 34+

### Building & Running
```bash
# 1. Clone repository
git clone https://github.com/MaheshSurepalli/ripple.git
cd ripple

# 2. Run unit tests
./gradlew testDebugUnitTest

# 3. Build debug APK
./gradlew assembleDebug

# 4. Install onto connected device/emulator
./gradlew installDebug
```

---

## 🧪 Testing External Deep Links
```bash
# Test opening an invite link via ADB:
adb shell am start -a android.intent.action.VIEW -d "https://ripple.app/i/tok_alex_pending" com.example.ripple
```

---

## 📜 License
Copyright © 2026 Ripple App. All rights reserved.
