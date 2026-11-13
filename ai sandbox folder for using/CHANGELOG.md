# Changelog

All notable changes to LocalLLM-Frontend (Sandroid) will be documented in this file.

## [2.0.0] - 2026-11-13

### 🎉 Major Features Added

#### Priority 1: Core Improvements
- **Streaming Responses**: Real-time token display for faster perceived performance
- **Connection Status Indicator**: Green/red dot showing server connectivity
- **Persistent Settings**: IP, model, and temperature saved between launches
- **Chat Bubbles**: Visual distinction between user and AI messages
- **Auto-save Chat History**: Chats persist across app restarts

#### Priority 2: UI/UX Enhancements
- **Timestamps**: All messages show time of sending
- **Auto-scroll**: Automatically scrolls to bottom on new messages
- **Long-press Menu**: Copy, Copy + Paste, Delete options
- **Markdown Rendering**: AI responses rendered with proper formatting
- **Copy Buttons**: Easy copy on any message

#### Priority 3: Advanced Features
- **Dark/Light Themes**: Toggle between themes or use system default
- **Voice Input**: Speech-to-text input with start/stop controls
- **File Upload**: Support for images and PDFs (vision models)
- **Keyboard Shortcuts**: Ctrl+Enter to send, Ctrl+L to clear
- **Share Conversation**: Share chats with other users

### 🔧 Technical Improvements
- Upgraded to Coroutines-based async architecture
- Implemented modern Material Design components
- Added comprehensive error handling with actionable messages
- Improved network reliability with retry logic

### 📦 New Dependencies Added
- kotlinx-coroutines-android
- markdown parser library
- speech-to-text support
- file picker integration

---

## [1.0.0] - Original Version

### Initial Release
- Basic chat interface
- Non-streaming responses
- Simple IP and model configuration
- Minimal error handling
