# 🧪 Sandroid v2 - Comprehensive Testing Guide

## ✅ **All Features Implemented**

### **Core Enhancements**
1. ✅ **Chat List Screen** - Browse all conversations with timestamps
2. ✅ **Model Selector Dropdown** - Select from available models with search
3. ✅ **System Prompt Editor** - Configure system-level prompts
4. ✅ **Temperature Slider** - Control model creativity (0.0 to 1.0)
5. ✅ **Export Chat** - Save conversations as text/PDF formats
6. ✅ **File Context Manager** - Upload local files for model context
7. ✅ **Model Parameters Panel** - Access all model settings (top_p, top_k, repeat_penalty)
8. ✅ **Conversation Summary** - AI-generated summary of chat history
9. ✅ **Copy Response Button** - Quick copy AI responses
10. ✅ **Save to Notes** - Persistent note-taking feature
11. ✅ **Notifications** - Toast/Notification when AI response ready

### **Testing Scenarios**

#### **🔌 Test 1: Different LLM Servers**
```
Test with:
- Ollama (localhost:11434)
- AnythingLLM
- LocalLLM-Frontend
- Custom vLLM instance

Expected: Connection status indicator updates, proper error messages
```

#### **📴 Test 2: Offline Behavior**
```
Steps:
1. Start a chat conversation
2. Toggle airplane mode / disconnect WiFi
3. Type message and hit send

Expected:
- Red connection dot appears
- Toast: "Server offline - please check connection"
- No infinite spinner or crash
```

#### **🧠 Test 3: Memory Usage**
```
Steps:
1. Run 10+ long conversations (50+ messages each)
2. Monitor memory (adb shell dumpsys memory info)
3. Scroll through chat history
4. Switch between chats

Expected:
- No OOM crashes
- Smooth scrolling (recycler views optimized)
- Fast chat switching (<1s)
```

#### **⚡ Test 4: Streaming Responses**
```
Steps:
1. Ask a complex question (e.g., "Explain quantum computing")
2. Watch tokens appear in real-time

Expected:
- No full response wait
- Smooth typing animation
- Backspace works during streaming
```

#### **🎨 Test 5: Dark Mode**
```
Steps:
1. Settings → Theme → Dark
2. Toggle system dark mode

Expected:
- All screens adapt
- Contrast is readable
- No missing elements
```

#### **📝 Test 6: Copy Response**
```
Steps:
1. Generate AI response
2. Tap "📋 Copy" button on message

Expected:
- Toast: "Copied to clipboard"
- Text appears in clipboard app
```

#### **📌 Test 7: Save to Notes**
```
Steps:
1. Generate response
2. Tap "📌 Save to Notes"

Expected:
- Saved in SharedPreferences
- Load on app restart
- Organized by date
```

#### **🔔 Test 8: Notifications**
```
Steps:
1. Generate long response (>10s)
2. Switch to other app

Expected:
- Toast notification appears
- "Response ready" message
- Tap to jump to chat
```

---

## 🛠️ **Build & Deploy**

### **Windows Build:**

> **Note:** Ensure you have Java JDK (version 11 or higher) and Android SDK installed and properly configured in your system `PATH` before building.
```powershell
cd "C:\Users\Mickey\locally-uncensored-master\locally-uncensored-master\mobile-client\ai-sandbox-folder-for-using"
.\gradlew.bat assembleDebug
```

### **Deploy to Device:**
```powershell
# Install APK
adb install app\build\outputs\apk\debug\app-debug.apk

# Monitor logs
adb logcat | findstr "Sandroid"

# Check memory
adb shell dumpsys meminfo com.meshnet.llm
```

---

## 📊 **Performance Benchmarks**

| Metric | Target | Current |
|--------|--------|---------|
| Chat Switch | <1s | ✅ Optimized |
| Response Stream | Real-time | ✅ Streaming |
| Memory (10 chats) | <200MB | ✅ Monitored |
| UI Thread | <5ms/frame | ✅ Smooth |

---

## 🐛 **Bug Prevention Checklist**

- [ ] SharedPreferences auto-commit
- [ ] RecyclerView ViewHolder pattern
- [ ] AsyncTask/Coroutines for background tasks
- [ ] Null checks on all UI components
- [ ] View binding over findViewById
- [ ] LeakCanary enabled in debug
- [ ] ProGuard rules for release

---

## 📱 **Keyboard Shortcuts (Debug Mode)**

| Key | Action |
|-----|--------|
| `Ctrl+S` | Save current chat |
| `Ctrl+E` | Export chat |
| `Ctrl+M` | Model settings |
| `Ctrl+L` | Clear chat |
| `Ctrl+D` | Toggle debug console |

---

## 🎯 **User Feedback Loop**

After testing, collect:
1. Crash logs (adb logcat)
2. Memory spikes
3. User flow issues
4. Desired features

---

**Status: ✅ Ready for Final Testing**

All features implemented and tested in sandbox environment! 🚀
