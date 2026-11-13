package com.meshnet.llm

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var llmClient: LLMClient
    private lateinit var chatManager: ChatManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var themeManager: ThemeManager

    // UI Components
    private lateinit var ipInput: EditText
    private lateinit var portInput: EditText
    private lateinit var modelInput: EditText
    private lateinit var promptInput: EditText
    private lateinit var sendButton: Button
    private lateinit var chatDisplay: TextView
    private lateinit var connectionIndicator: ImageView
    private lateinit var connectionLabel: TextView
    private lateinit var menuButton: ImageView
    private lateinit var chatListButton: ImageView
    private lateinit var modelSelectorButton: ImageView
    private lateinit var settingsButton: ImageView
    private lateinit var voiceButton: ImageView
    private lateinit var fileButton: ImageView
    private lateinit var exportButton: ImageView

    // State
    private var isConnected = false
    private var isStreaming = false
    private var currentChatId: String? = null
    private var currentChatName: String = "Chat 1"

    // Launchers
    private lateinit var chatListLauncher: ActivityResultLauncher<Unit>
    private lateinit var modelSelectorLauncher: ActivityResultLauncher<Unit>
    private lateinit var systemPromptLauncher: ActivityResultLauncher<Unit>
    private lateinit var temperatureLauncher: ActivityResultLauncher<Unit>
    private lateinit var exportChatLauncher: ActivityResultLauncher<Unit>
    private lateinit var fileContextLauncher: ActivityResultLauncher<Unit>
    private lateinit var modelParamsLauncher: ActivityResultLauncher<Unit>
    private lateinit var summaryLauncher: ActivityResultLauncher<Unit>

    // Permissions
    private val permissionsNeeded = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize managers
        chatManager = ChatManager(this)
        settingsManager = SettingsManager(this)
        themeManager = ThemeManager(this)

        // Initialize client
        llmClient = LLMClient(this)

        // Initialize UI
        initUI()

        // Restore settings
        restoreSettings()

        // Load chat list
        loadChatList()

        // Request permissions
        requestPermissions()

        // Setup launchers
        setupLaunchers()

        // Setup menu
        setupMenu()
    }

    private fun initUI() {
        // Get views
        ipInput = findViewById(R.id.ipInput)
        portInput = findViewById(R.id.portInput)
        modelInput = findViewById(R.id.modelInput)
        promptInput = findViewById(R.id.promptInput)
        sendButton = findViewById(R.id.sendButton)
        chatDisplay = findViewById(R.id.chatDisplay)
        connectionIndicator = findViewById(R.id.connectionIndicator)
        connectionLabel = findViewById(R.id.connectionLabel)
        menuButton = findViewById(R.id.menuButton)
        chatListButton = findViewById(R.id.chatListButton)
        modelSelectorButton = findViewById(R.id.modelSelectorButton)
        settingsButton = findViewById(R.id.settingsButton)
        voiceButton = findViewById(R.id.voiceButton)
        fileButton = findViewById(R.id.fileButton)
        exportButton = findViewById(R.id.exportButton)

        // Set initial model
        val savedModel = settingsManager.getModelName() ?: "mistral"
        modelInput.setText(savedModel)

        // Set connection indicator
        updateConnectionIndicator()

        // Set listeners
        sendButton.setOnClickListener {
            sendPrompt()
        }

        chatListButton.setOnClickListener {
            openChatList()
        }

        modelSelectorButton.setOnClickListener {
            openModelSelector()
        }

        settingsButton.setOnClickListener {
            openSettings()
        }

        voiceButton.setOnClickListener {
            openVoiceInput()
        }

        fileButton.setOnClickListener {
            openFileContext()
        }

        exportButton.setOnClickListener {
            openExportChat()
        }

        // Auto-scroll to bottom
        chatDisplay.scrollToBottom()
    }

    private fun restoreSettings() {
        val savedIp = settingsManager.getIpAddress() ?: "192.168.1.100"
        val savedPort = settingsManager.getPort() ?: "11434"
        val savedModel = settingsManager.getModelName() ?: "mistral"
        val savedPrompt = settingsManager.getSystemPrompt() ?: "You are a helpful assistant."

        ipInput.setText(savedIp)
        portInput.setText(savedPort)
        modelInput.setText(savedModel)
        promptInput.setText(savedPrompt)
    }

    private fun loadChatList() {
        val chats = chatManager.getChats()
        if (chats.isEmpty()) {
            chatDisplay.text = "No conversations yet. Start chatting!"
            return
        }

        val chatNames = chats.map { it.name }.toTypedArray()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, chatNames)
        // In a full app, you'd use a RecyclerView here
        // For now, we'll just show the first chat
        currentChatName = chats[0].name
    }

    private fun sendPrompt() {
        val ip = ipInput.text.toString().trim()
        val port = portInput.text.toString().trim()
        val model = modelInput.text.toString().trim()
        val systemPrompt = promptInput.text.toString().trim()

        // Validate input
        if (ip.isEmpty() || port.isEmpty() || model.isEmpty()) {
            showToast("Please fill in IP, Port, and Model")
            return
        }

        // Create chat session if first message
        if (currentChatId == null) {
            currentChatId = chatManager.createChat(currentChatName)
            chatManager.saveChat(currentChatId, currentChatName)
        }

        // Add user message
        addMessage("user", systemPrompt, currentChatId)

        // Connect and stream
        connectAndStream(ip, port, model, systemPrompt, currentChatId)
    }

    private fun connectAndStream(ip: String, port: String, model: String, systemPrompt: String, chatId: String) {
        isConnected = false
        updateConnectionIndicator()

        llmClient.connect(ip, port.toInt(), model, systemPrompt) { response ->
            addMessage("assistant", response, chatId)
            // Auto-save chat
            chatManager.saveChat(chatId, currentChatName)
        }

        llmClient.onError { error ->
            handleConnectionError(error)
        }

        llmClient.onDisconnect {
            isConnected = false
            updateConnectionIndicator()
        }
    }

    private fun addMessage(role: String, text: String, chatId: String) {
        val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val message = ChatMessage(chatId, timestamp, role, text)
        chatManager.addMessage(chatId, message)
        chatDisplay.appendText(getMessageHtml(message, chatId))
        chatDisplay.scrollToBottom()
    }

    private fun getMessageHtml(message: ChatMessage, chatId: String): String {
        return """
            <div style="margin: 8px 0; padding: 10px; border-radius: 8px; max-width: 80%;">
                <div style="font-size: 12px; color: #666;">${message.timestamp}</div>
                <div style="font-weight: ${if (message.role == "user") "bold" else "normal"};">
                    ${if (message.role == "user") "You:" else "AI:"}
                </div>
                <div style="margin-top: 4px;">${message.content}</div>
                <div style="margin-top: 4px; font-size: 10px; color: #999;">
                    <button onclick="copyMessage('${message.content}')" style="background: none; border: none; cursor: pointer;">📋 Copy</button>
                </div>
            </div>
        """.trimIndent()
    }

    private fun handleConnectionError(error: String) {
        showToast("Connection error: $error")
        isConnected = false
        updateConnectionIndicator()
    }

    private fun updateConnectionIndicator() {
        connectionIndicator.setImageResource(if (isConnected) android.R.drawable.ic_dialog_alert else android.R.drawable.stat_sys_download_done)
        connectionLabel.text = if (isConnected) "Connected" else "Connecting..."
        connectionIndicator.alpha = if (isConnected) 1.0f else 0.5f
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT).apply {
            show()
        }
    }

    private fun requestPermissions() {
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.forEach { (permission, granted) ->
                if (granted) {
                    // Permission granted
                }
            }
        }

        val permissionsToRequest = permissionsNeeded.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun setupLaunchers() {
        chatListLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult) {}
        modelSelectorLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult) {}
        systemPromptLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult) {}
        temperatureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult) {}
        exportChatLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult) {}
        fileContextLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult) {}
        modelParamsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult) {}
        summaryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult) {}
    }

    private fun openChatList() {
        val intent = Intent(this, ChatListActivity::class.java).apply {
            putExtra("chatManager", chatManager)
        }
        startActivity(intent)
    }

    private fun openModelSelector() {
        val intent = Intent(this, ModelSelectorActivity::class.java)
        startActivity(intent)
    }

    private fun openSettings() {
        // TODO: Implement settings activity
    }

    private fun openVoiceInput() {
        val intent = Intent(this, VoiceInputActivity::class.java)
        startActivity(intent)
    }

    private fun openFileContext() {
        val intent = Intent(this, FileContextActivity::class.java)
        startActivity(intent)
    }

    private fun openExportChat() {
        val intent = Intent(this, ExportChatActivity::class.java)
        startActivity(intent)
    }

    private fun openSystemPrompt() {
        val intent = Intent(this, SystemPromptEditorActivity::class.java)
        startActivity(intent)
    }

    private fun openTemperature() {
        val intent = Intent(this, TemperatureSliderActivity::class.java)
        startActivity(intent)
    }

    private fun openModelParams() {
        val intent = Intent(this, ModelParametersActivity::class.java)
        startActivity(intent)
        startActivity(intent)
    }

    private fun openSummary() {
        val intent = Intent(this, ConversationSummaryActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        llmClient.disconnect()
    }
}
