# Alic AI — Native Android AI Voice Assistant

**Alic AI** is a native Android AI voice assistant app developed by **Rohit Raj**, powered by **Google Gemini API**, native Android **SpeechRecognizer** for real-time speech-to-text, and native Android **TextToSpeech** for natural, neutral voice responses.

---

## Key Features

### 1. Google Gemini Intelligence & Multi-Model Support
- **Official Google Gemini REST API**: Communicates directly with the official Google Gemini endpoint (`https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent`).
- **Supported Models**:
  - **Gemini 2.5 Flash (Recommended Default)** — high intelligence, rapid conversational turnaround, and low latency.
  - **Gemini 2.0 Flash** — next-generation multimodal performance.
  - **Gemini 1.5 Flash** — ultra-fast efficiency and lightweight token usage.
- **Voice-Tuned Conversational Responses**: Responses are automatically tuned via system prompting to sound pleasant and conversational when read aloud, omitting awkward markdown asterisks, hashes, and formatting tags.
- **Context Retention**: Preserves recent conversational context across multiple turns.

### 2. In-App Gemini API Key Configuration (Mandatory)
- **Direct In-App User Entry**: Users enter their own Gemini API key directly inside the installed application (First-Time Setup Dialog or Settings tab).
- **No Hardcoded Credentials**: Neither the repository nor the APK contains hardcoded API keys. A GitHub Secret alone is never a substitute for this user configuration interface.
- **Hardware-Backed Encryption**: Stored keys are encrypted on-device using **Android KeyStore** with `AES/GCM/NoPadding` cipher encryption before being saved to private application preferences.
- **Privacy & Masking**: The full key is never displayed once saved (e.g., `AIzaSy••••••••XYZW`).
- **Show/Hide Visibility Toggle**: Password toggle allows verification during entry.
- **Tactile Save Button**: Features a luminous gradient border and spring scale press animation (`OutlinedAnimatedButton`).
- **Real Connection Testing**: Dedicated **"Test Gemini Connection"** button sends an actual ping request to the Gemini API and displays live status badges:
  - `Connected` (with latency in milliseconds and model name)
  - `Invalid API Key` (HTTP 400/403)
  - `Rate Limit Exceeded` (HTTP 429)
  - `Network Error` (DNS failure, timeout, or no internet)
- **Edit, Replace & Delete**: Users can modify, replace, or permanently wipe the stored key at any time.
- **Persistent Across Restarts**: Once configured, Alic AI remembers the key across app restarts and launches directly to the assistant home screen without asking again.

### 3. Neutral Voice & Android TextToSpeech
- **Neutral Voice by Default**: Android `TextToSpeech` is configured with a calm, balanced, neutral vocal tone without forcing strongly masculine or feminine characteristics.
- **Voice Style Selection**: Options for **Neutral (Default)**, **Male**, and **Female** when supported by the device's installed TTS engine.
- **Engine Limitation Handling**: If the device's TTS engine does not distinguish gender tags, the app gracefully selects the closest balanced natural voice and displays an informative note in Settings.
- **Pitch & Rate Controls**:
  - Speech Rate slider (0.5x to 2.0x, default 1.0x).
  - Voice Pitch slider (0.5x to 1.5x, default 1.0x).
- **Test Voice Button**: Plays a sample utterance ("Hello, I am Alic AI. My voice is calm, clear, and ready to assist you.").
- **Mute Spoken Replies**: Dedicated toggle switch to mute spoken audio when in quiet spaces.
- **Voice Architecture Disclaimer**: Settings clearly notes that Android TextToSpeech synthesizes voice responses on-device, while Gemini powers the conversational intelligence.

### 4. Voice Assistant Capabilities & UI
- **Animated Glowing AI Orb**: Reactive visualizer reflecting assistant states:
  - *Idle*: Calm breathing pulse.
  - *Listening*: Concentric acoustic wave rings modulated dynamically by microphone RMS volume.
  - *Thinking*: Holographic rotating orbital rings.
  - *Speaking*: Soundwave equalization aura.
- **Large Microphone Button**: 76dp central button with pulsing acoustic wave rings and single-tap activation.
- **Real-Time Speech-to-Text**: Live transcription card displays recognized words as you speak.
- **Typed Question Support**: Seamless fallback to typing via keyboard icon or chat input bar if voice input is unavailable.
- **Chat History & Controls**: Full conversation bubbles with timestamps, copy-to-clipboard, repeat voice playback ("Speak Again"), and clear conversation confirmation.
- **Instant Audio Interruption**: Tapping the mic or the dedicated Stop button halts TTS speech immediately.

---

## Automated GitHub Actions APK Build

This project includes a pre-configured GitHub Actions workflow (`.github/workflows/build-apk.yml`) that automatically compiles and packages a working Android Debug APK.

### 1. Pushing the Project to GitHub
Initialize your local repository (if not already done) and push to GitHub:
```bash
git init
git add .
git commit -m "Initial commit of Alic AI Android Voice Assistant"
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo-name>.git
git push -u origin main
```

### 2. Triggering the Workflow
The build triggers automatically in two ways:
1. **Automatic**: On every `push` to the `main` branch.
2. **Manual**: Go to your repository on GitHub:
   - Click the **Actions** tab.
   - Select **Build Alic AI APK** from the left sidebar.
   - Click **Run workflow** -> Select `main` branch -> Click the green **Run workflow** button.

### 3. Downloading the APK Artifact
1. Click on the completed workflow run (indicated by a green checkmark `✓`).
2. Scroll down to the **Artifacts** section at the bottom of the Summary page.
3. Click on **`alic-ai-debug-apk`** to download the ZIP file (`alic-ai-debug-apk.zip`).

### 4. Extracting the ZIP and Installing on Android
1. **Extract the ZIP**: Unzip `alic-ai-debug-apk.zip` on your computer or directly on your Android phone. Inside you will find **`app-debug.apk`**.
2. **Transfer to Device**: If downloaded on a computer, transfer `app-debug.apk` to your phone via USB cable, Google Drive, Bluetooth, or local sharing.
3. **Enable Unknown Apps**:
   - On Android, navigate to **Settings** > **Apps** > **Special App Access** > **Install Unknown Apps**.
   - Enable permission for your File Manager or Browser.
4. **Install**:
   - Tap `app-debug.apk` in your device's file manager.
   - Tap **Install** (or **Install Anyway** if prompted by Google Play Protect for debug build signatures).
   - Tap **Open** to launch Alic AI!

---

## In-App Gemini API Key Setup & Security Notice

### Configuring Your Key in the App
1. When you launch Alic AI for the first time, a setup dialog will greet you.
2. Obtain a free Gemini API key from [Google AI Studio](https://aistudio.google.com/).
3. Paste your key into the **Gemini API Key** field.
4. Tap **Save Key** (or **Test** to verify connection immediately).
5. Once saved, Alic AI remembers your key across app restarts and directly opens the assistant home screen. You can edit, test, or delete it anytime under the **Settings** tab.

### Security & Production Best Practices
- **Android KeyStore Protection**: The entered API key is encrypted on your device using hardware-backed AES-GCM encryption.
- **Client-Side Storage Risk**: Any credential stored locally on a mobile device can theoretically be extracted if a device is rooted or reverse-engineered with debugger tools.
- **Production Recommendation**: For public distribution on the Google Play Store, we recommend routing all Gemini API calls through an authenticated backend server proxy (e.g. Firebase Cloud Functions, Cloud Run, or custom backend) rather than embedding raw API credentials in client apps.

---

## Local Build Instructions

### Prerequisites
- Android Studio Ladybug / Iguana or later
- JDK 17 or JDK 21
- Android SDK 36 (compileSdk 36, minSdk 24)

### Building via Terminal
```bash
# Make gradlew executable
chmod +x gradlew

# Build Debug APK locally
./gradlew :app:assembleDebug

# Output APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

### Running Local Tests
```bash
./gradlew :app:testDebugUnitTest
```

---

## Credits & License

- **Application**: Alic AI Voice Assistant
- **Developer**: Rohit Raj
- **AI Model**: Google Gemini API
- **License**: MIT
