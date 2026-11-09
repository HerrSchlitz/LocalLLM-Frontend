# 👾 LocalLLM-Frontend 👾

An ultra-basic, Android app that functions as a frontend designed to connect seamlessly to a local PC hosting uncensored Language Models (LLMs) and AI agents. This project aims to bridge the gap between desktop computing power and mobile convenience, enabling secure-ish, low-latency, AI interactions on the go or within your home network.

<img width="903" height="885" alt="shared_image" src="https://github.com/user-attachments/assets/6fe963f5-20eb-4589-8302-6168c4ec22c2" />

## 🌟 Project Background 🌟

This repository marks my first successful milestone after months of self-teaching, deep-diving into the world of LLMs, and figuring out how to build a functional Android app to use as an AI frontend. This repository contains the app designed to access abliterated/uncensored language models that are locally hosted on a separate device/PC and accessible from either the same network or remotely from an Android device.

*   **Self-Taught Journey:** Initially I was just trying to find a new hobby to cut back on drinking and avoid the "doomscrolling" of social media sites trap we all seem to fall into *(fun fact: This was not a hobby that reduced alcohol consumption or time spent on my phone, as I used Termux on a Galaxy S23 Ultra to create the entire thing)*. After spending countless months and hours trying to learn about mobile development workflows, terminal mechanics, and network configurations from YouTube tutorials, other GitHub repositories' code, and Reddit forums, alongside a few failed/partially working projects, this one came to life. Please note, this is my first "successful deployment" and I'm a little proud of the achievement; however, I am aware how basic it is and there's a good chance it may work for others. I am more than open to feedback and eager to learn, but please be kind 😇
*   **AI Collaboration:** In line with modern development practices, AI assistance was used to co-author, debug, refine, and optimize this codebase. *(Honestly, I think just learning C++, Python, JavaScript etc. would've been faster with all the setbacks from hallucinations and guardrails).*
*   **Future Vision:** The application currently has very few features or adjustments. I plan to continue working on it systematically to achieve the ad-free, open-source application that I couldn't find when I first started building it.*
*   **A Note on Inexperience:** Please be aware that this project is heavily AI-assisted. Because I am still learning and not yet experienced enough, I don't always know if I have missed an essential feature or left something in that should have been removed. I felt it was better to be completely transparent about my current gaps in knowledge rather than saying nothing, just in case a configuration or build issue affects someone else's setup.^

## 🚀 Key Features 🚀

*   **Meshnet Ready:** Engineered to securely connect your Android mobile device to a local host machine via a mesh network (e.g., NordMeshnet or similar meshnet layers).
*   **Remote & Local Flexibility:** Operates smoothly (in most cases) whether you are sitting on the same Wi-Fi network as your host PC or connecting remotely from miles away.
*   **Lightweight Frontend:** Avoids bloated mobile framework overhead, keeping the interface snappy and strictly focused on raw interface communication with your backend model server (like Ollama or local inference engines).

---

## 📱 Application Interface & Workflow

### 1. Configuration Panel
Set up your destination host addresses, swap active inference models, modify generative temperature curves, and pass system prompt rules seamlessly.

<img width="513" height="1000" alt="shared_image" src="https://github.com/user-attachments/assets/5b536bd8-883b-444e-b57e-534f49fe50e1" /><img width="513" height="1000" alt="shared_image" src="https://github.com/user-attachments/assets/b88c690d-a4a9-4686-bc96-a5f6ffadd991" /><img width="513" height="1000" alt="shared_image" src="https://github.com/user-attachments/assets/4df6bdde-39c5-4d77-824d-11b1d772fec9" />




### 2. Live Chat Stream
The clean messaging component layout handles contextual scrolling, message delivery monitoring (including error tracking outputs), and rich-text parsing.

<img width="513" height="1000" alt="shared_image" src="https://github.com/user-attachments/assets/0dafbaf4-9f34-419a-b612-af06a321dee1" /><img width="513" height="1000" alt="shared_image" src="https://github.com/user-attachments/assets/41a8fb6b-ec95-4ca9-9f99-dae3f3e303ec" /><img width="513" height="1000" alt="shared_image" src="https://github.com/user-attachments/assets/77f10544-3290-4243-8f2e-25c2c3187292" />




---

## 🛠️ Project Structure 🛠️

The repository contains the clean, decoupled Android source tree required to compile the application:
*   `/app` - Core Android application source code, UI layouts, and activities.
*   `/gradle` - Build wrappers and environment configurations.
*   `build.gradle.kts` - Kotlin-based dependency management.

---

*Maintained by [HerrSchlitz](https://github.com).*

---

## 🔒 Network Security Note (HTTP vs. HTTPS)

> [!IMPORTANT]
> This application communicates strictly over **unencrypted HTTP, not HTTPS**. Because the app is designed to run exclusively inside an encrypted **NordVPN Meshnet** private tunnel, data traveling between your Android device and your host PC is fully protected by NordVPN’s wire-level transport security. However, because the application layer itself utilizes raw `http://` endpoints, **do not expose your backend server port directly to the public internet** without a secure VPN or meshnet active.

---

## 🛠️ Installation & Backend Configuration

Follow these steps to link this Android frontend to your local PC inference engine:

### 1. Configure the Backend Machine (PC)
Ensure your local inference engine (i.e., Ollama, LM Studio, or a custom API server) is listening on your network interface rather than just `localhost`.

*   For **Ollama** on Windows/Linux, set the environment variable:
    ```bash
    OLLAMA_HOST=0.0.0.0:11434
    ```
*   Start your backend service and verify it is active.

### 2. Get Your NordVPN Meshnet IP
1.  Open the NordVPN app on your host PC and ensure **Meshnet** is toggled ON.
2.  Note down the unique **Meshnet IP address** assigned to your PC (e.g., `100.x.x.x`).
3.  Ensure your Android device has NordVPN active, Meshnet toggled ON, and is linked under the same account profile.

### 3. Connect the Android App
1.  When launching the app on your mobile device, enter your host PC's NordVPN Meshnet IP (replacing `<YOUR_NORDVPN_MESHNET_IP>`) and port (the numbers at the end after the `:` in the line below, which is `11434`) into the configuration field using the explicit http scheme:
    ```text
    http://<YOUR_NORDVPN_MESHNET_IP>:11434
    ```

You should now be able to start chatting with your LLM hosted locally on your PC straight from your mobile Android device! 😁
