# Sandroid v2 - Local LLM Frontend for Android

A modern, user-friendly Android app for interacting with local LLMs (Ollama, LM Studio, etc.) via NordVPN Meshnet.

## ✨ Features

### 🚀 Core Features
- **Streaming Responses** - Real-time token display (no more waiting!)
- **Connection Status Indicator** - Green/red dot shows server connectivity
- **Persistent Settings** - IP, model, temperature saved automatically
- **Chat Bubbles** - Beautiful visual distinction between user/AI messages
- **Auto-Save History** - Chats persist across app restarts

### 🎨 UI/UX Features
- **Timestamps** - Every message shows when it was sent
- **Auto-Scroll** - Smooth scroll to bottom on new messages
- **Long-Press Menu** - Copy, Copy+Paste, Delete options
- **Markdown Rendering** - Beautiful formatted responses
- **Copy Buttons** - One-tap copy on any message

### 🌙 Advanced Features
- **Dark/Light Themes** - Toggle between themes (or use system default)
- **Voice Input** - Speech-to-text for hands-free chatting
- **File Upload** - Send images/PDFs to vision models
- **Keyboard Shortcuts** - Ctrl+Enter to send, Ctrl+L to clear
- **Share Conversations** - Share chat history with others

## 🏗️ Architecture

### Project Structure
```
app/
├── src/main/
│   ├── java/com/meshnet/llm/
│   │   ├── MainActivity.java          # Main activity with chat UI
│   │   ├── ChatManager.java           # Manages multiple chats
│   │   ├── ChatSession.java           # Chat history model
│   │   ├── LLMClient.java             # Streaming LLM client
│   │   ├── Message.java               # Message data model
│   │   ├── MarkdownRenderer.java      # Markdown parser
│   │   ├── VoiceInputActivity.java    # Voice input screen
│   │   ├── FileUploadActivity.java    # File upload screen
│   │   └── ThemeManager.java          # Theme toggle
│   └── res/layout/
│       ├── activity_main.xml          # Main chat UI
│       ├── activity_voice_input.xml   # Voice input UI
│       └── activity_file_upload.xml   # File upload UI
└── build.gradle.kts                   # Gradle build config
```

### Dependencies
- **kotlinx-coroutines-android** - Async operations & streaming
- **org.commonmark:commonmark** - Markdown rendering
- **androidx.camera:camera-camera2** - Camera/file picker
- **androidx.speech:speech** - Voice input

## 🚀 Quick Start

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 34 or later
- Gradle 8.0+

### Build from Command Line
```bash
cd app
./gradlew assembleDebug
```

### Install on Device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 🛠️ Configuration

### build.gradle.kts Settings
```kotlin
android {
    compileSdk = 34
    defaultConfig {
        applicationId = "com.meshnet.llm"
        minSdk = 24
        targetSdk = 34
    }
}
```

### Default Settings (SharedPreferences)
- **IP Address**: 192.168.1.1 (change as needed)
- **Model**: local-model
- **Temperature**: 0.7
- **System Prompt**: "You are a helpful assistant."

## 📱 Connection Setup

1. **Start Ollama/LM Studio** on your backend server
2. **Configure NordVPN Meshnet** to connect your phone to the backend
3. **Enter Backend IP** in the app (or use saved default)
4. **Tap the green connection indicator** to test connectivity
5. **Start chatting!**

## 🎯 Use Cases

- **Local Privacy** - Keep conversations on-device
- **NordVPN Meshnet** - Connect to remote LLMs securely
- **Offline Mode** - Work without internet
- **Custom Models** - Use any Ollama-compatible model

## 📝 License

MIT License - Feel free to modify and distribute!

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## 📄 License

MIT License. See [LICENSE](LICENSE) for details.
