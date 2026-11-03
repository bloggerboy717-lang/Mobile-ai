package com.example.agent

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.Settings
import android.telecom.TelecomManager
import android.telephony.SmsManager
import android.util.Log
import com.example.service.AlicAccessibilityService
import java.util.UUID

enum class ActionType {
    OPEN_APP,
    SEARCH_YOUTUBE,
    NAVIGATE_MAPS,
    TOGGLE_FLASHLIGHT,
    OPEN_SETTINGS,
    SET_ALARM,
    SHARE_CONTENT,
    ADJUST_VOLUME,
    SYSTEM_NAV,
    ANALYZE_SCREEN,
    CUSTOM_EXPLANATION,
    ANSWER_CALL,
    END_CALL,
    SEND_SMS,
    READ_SMS
}

enum class ActionStatus {
    PLANNED,
    WAITING_CONFIRMATION,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class AgentAction(
    val id: String = UUID.randomUUID().toString(),
    val type: ActionType,
    val title: String,
    val description: String,
    val params: Map<String, String> = emptyMap(),
    val requiresConfirmation: Boolean = false,
    var status: ActionStatus = ActionStatus.PLANNED,
    var resultMessage: String? = null
)

class AgentActionEngine(private val context: Context) {

    companion object {
        private const val TAG = "AgentActionEngine"
        private var isFlashlightOn = false
    }

    /**
     * Parses English, Hindi (Devanagari), and Hinglish natural speech into actionable device operations
     */
    fun parseUserIntent(prompt: String): AgentAction? {
        val lower = prompt.lowercase().trim()

        return when {
            // Answer incoming call ("फोन उठाओ", "कॉल उठाओ", "phone uthao", "answer call", "pick up", "receive call")
            lower.contains("phone uthao") || lower.contains("call uthao") || lower.contains("answer call") ||
                    lower.contains("pick up") || lower.contains("receive call") || lower.contains("call pick") ||
                    lower.contains("फोन उठाओ") || lower.contains("कॉल उठाओ") || lower.contains("फोन रिसीव") ||
                    lower.contains("कॉल रिसीव") || lower.contains("उठा लो") || lower.contains("उठाओ") && (lower.contains("phone") || lower.contains("call") || lower.contains("फोन") || lower.contains("कॉल")) -> {
                AgentAction(
                    type = ActionType.ANSWER_CALL,
                    title = "Answer Incoming Call",
                    description = "फोन उठा रहा हूँ (Answering phone call)"
                )
            }

            // End / cut call ("फोन काटो", "कॉल काटो", "phone cut", "end call", "reject call", "hang up", "cut karo")
            lower.contains("phone kato") || lower.contains("call kato") || lower.contains("phone cut") ||
                    lower.contains("call cut") || lower.contains("end call") || lower.contains("reject call") ||
                    lower.contains("hang up") || lower.contains("disconnect call") || lower.contains("cut phone") ||
                    lower.contains("फोन काटो") || lower.contains("कॉल काटो") || lower.contains("फोन काट दो") ||
                    lower.contains("कॉल काट दो") || lower.contains("कॉल डिस्कनेक्ट") || lower.contains("काट दो") && (lower.contains("phone") || lower.contains("call") || lower.contains("फोन") || lower.contains("कॉल")) -> {
                AgentAction(
                    type = ActionType.END_CALL,
                    title = "End Phone Call",
                    description = "फोन काट रहा हूँ (Ending phone call)"
                )
            }

            // Send SMS / Message ("मैसेज भेजो", "send message", "send sms", "sms bhejo")
            lower.contains("message bhejo") || lower.contains("sms bhejo") || lower.contains("send message") ||
                    lower.contains("send sms") || lower.contains("मैसेज भेजो") || lower.contains("एसएमएस भेजो") ||
                    lower.contains("मैसेज करो") || lower.contains("संदेश भेजो") || lower.contains("message karo") -> {
                val recipient = extractSmsRecipient(prompt)
                val msg = extractSmsMessage(prompt)
                AgentAction(
                    type = ActionType.SEND_SMS,
                    title = if (recipient.isNotBlank()) "Send Message to $recipient" else "Send SMS",
                    description = if (msg.isNotBlank()) "मैसेज: '$msg'" else "SMS कंपोज़ कर रहा हूँ",
                    params = mapOf("recipient" to recipient, "message" to msg)
                )
            }

            // Read SMS / Message ("मैसेज पढ़ो", "मैसेज देखना", "मैसेज सुनाओ", "read message", "read sms")
            lower.contains("message padho") || lower.contains("message dekhna") || lower.contains("read message") ||
                    lower.contains("read sms") || lower.contains("check message") || lower.contains("मैसेज पढ़ो") ||
                    lower.contains("मैसेज देखना") || lower.contains("मैसेज सुनाओ") || lower.contains("संदेश पढ़ो") ||
                    lower.contains("नया मैसेज") || lower.contains("मैसेज बताओ") || lower.contains("show message") -> {
                AgentAction(
                    type = ActionType.READ_SMS,
                    title = "Read Latest Messages",
                    description = "इनबॉक्स के मैसेज पढ़कर सुना रहा हूँ"
                )
            }

            // Flashlight / Torch (English & Hindi: "torch jalao", "torch on karo", "टॉर्च जलाओ", "टॉर्च चालू करो", "flashlight band karo", "टॉर्च बंद करो")
            lower.contains("torch") || lower.contains("flashlight") || lower.contains("टॉर्च") || lower.contains("फ्लैशलाइट") -> {
                val turnOff = lower.contains("off") || lower.contains("band") || lower.contains("bujhao") ||
                        lower.contains("बंद") || lower.contains("बुझाओ")
                val turnOn = !turnOff
                AgentAction(
                    type = ActionType.TOGGLE_FLASHLIGHT,
                    title = if (turnOn) "Turn On Flashlight" else "Turn Off Flashlight",
                    description = if (turnOn) "टॉर्च चालू कर रहा हूँ" else "टॉर्च बंद कर रहा हूँ",
                    params = mapOf("turnOn" to turnOn.toString())
                )
            }

            // YouTube Song / Video play:
            // "YouTube ओपन करके यो यो हनी सिंह का पहला गाना प्ले करो", "यूट्यूब में हनी सिंह का पहला गाना चलाओ", "play Yo Yo Honey Singh first song"
            lower.contains("youtube") || lower.contains("यूट्यूब") || lower.contains("यू ट्यूब") ||
                    lower.contains("gana play") || lower.contains("song play") || lower.contains("गाना प्ले") ||
                    lower.contains("गाना चलाओ") || lower.contains("gana chalao") || lower.contains("play song") -> {
                val autoPlay = lower.contains("pehla") || lower.contains("first") || lower.contains("पहला") ||
                        lower.contains("play") || lower.contains("chalao") || lower.contains("sunao") ||
                        lower.contains("प्ले") || lower.contains("चलाओ") || lower.contains("सुनाओ")
                val cleanQuery = extractSongOrVideoQuery(prompt)
                AgentAction(
                    type = ActionType.SEARCH_YOUTUBE,
                    title = if (cleanQuery.isNotBlank()) "Play: $cleanQuery" else "Open YouTube",
                    description = if (cleanQuery.isNotBlank()) "YouTube पर '$cleanQuery' का पहला गाना प्ले कर रहा हूँ" else "Open YouTube application",
                    params = mapOf(
                        "query" to cleanQuery,
                        "autoPlay" to autoPlay.toString()
                    )
                )
            }

            // Maps / Navigation (English & Hindi: "rasta dikhao", "navigate to", "map kholo", "रास्ता दिखाओ", "मैप खोलो")
            lower.contains("navigate") || lower.contains("directions") || lower.contains("rasta") ||
                    lower.contains("रास्ता") || lower.contains("दिशा") ||
                    (lower.contains("map") && !lower.contains("bitmap")) || lower.contains("मैप") -> {
                val destination = extractDestination(prompt)
                AgentAction(
                    type = ActionType.NAVIGATE_MAPS,
                    title = "Open Maps",
                    description = if (destination.isNotBlank()) "Navigate to $destination" else "Open Maps",
                    params = mapOf("destination" to destination)
                )
            }

            // Wi-Fi
            lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("वाईफाई") || lower.contains("वाई-फाई") -> {
                AgentAction(
                    type = ActionType.OPEN_SETTINGS,
                    title = "Open Wi-Fi Settings",
                    description = "Configure Wi-Fi connections",
                    params = mapOf("setting" to "wifi")
                )
            }

            // Bluetooth
            lower.contains("bluetooth") || lower.contains("ब्लूटूथ") -> {
                AgentAction(
                    type = ActionType.OPEN_SETTINGS,
                    title = "Open Bluetooth Settings",
                    description = "Manage paired accessories",
                    params = mapOf("setting" to "bluetooth")
                )
            }

            // Sound & Volume (Require explicit intent to change volume or open sound settings)
            ((lower.contains("volume") || lower.contains("वॉल्यूम") || lower.contains("sound setting") || lower.contains("साउंड सेटिंग")) &&
                    (lower.contains("badhao") || lower.contains("kam") || lower.contains("up") || lower.contains("down") ||
                            lower.contains("increase") || lower.contains("decrease") || lower.contains("बढ़ाओ") || lower.contains("कम") ||
                            lower.contains("तेज") || lower.contains("धीमा") || lower.contains("open") || lower.contains("kholo") || lower.contains("खोलो"))) ||
                    (lower.contains("awaz") || lower.contains("आवाज")) && (lower.contains("badhao") || lower.contains("kam karo") || lower.contains("kam kar") || lower.contains("बढ़ाओ") || lower.contains("कम करो") || lower.contains("तेज करो") || lower.contains("धीमी करो")) -> {
                if (lower.contains("badhao") || lower.contains("up") || lower.contains("increase") || lower.contains("बढ़ाओ") || lower.contains("तेज")) {
                    AgentAction(
                        type = ActionType.ADJUST_VOLUME,
                        title = "Volume Up",
                        description = "Increase media volume",
                        params = mapOf("direction" to "up")
                    )
                } else if (lower.contains("kam") || lower.contains("down") || lower.contains("decrease") || lower.contains("कम") || lower.contains("धीमा") || lower.contains("धीमी")) {
                    AgentAction(
                        type = ActionType.ADJUST_VOLUME,
                        title = "Volume Down",
                        description = "Decrease media volume",
                        params = mapOf("direction" to "down")
                    )
                } else {
                    AgentAction(
                        type = ActionType.OPEN_SETTINGS,
                        title = "Open Sound Settings",
                        description = "Configure volume and ringtones",
                        params = mapOf("setting" to "sound")
                    )
                }
            }

            // Alarm / Clock
            lower.contains("alarm") || lower.contains("wake me") || lower.contains("uthana") || lower.contains("अलार्म") || lower.contains("उठाना") -> {
                AgentAction(
                    type = ActionType.SET_ALARM,
                    title = "Set Alarm",
                    description = "Configure system alarm clock",
                    params = mapOf("prompt" to prompt),
                    requiresConfirmation = false
                )
            }

            // Screen Vision request (English & Hindi: "screen dekho", "what is on my screen", "screen me kya hai", "स्क्रीन देखो", "स्क्रीन पर क्या है")
            (lower.contains("screen") || lower.contains("स्क्रीन")) &&
                    (lower.contains("what") || lower.contains("see") || lower.contains("analyze") ||
                            lower.contains("dekho") || lower.contains("kya hai") || lower.contains("देखो") || lower.contains("क्या है")) -> {
                AgentAction(
                    type = ActionType.ANALYZE_SCREEN,
                    title = "Analyze Screen Vision",
                    description = "Capture user-authorized screen frame and evaluate with Gemini Vision"
                )
            }

            // Open Apps (English & Hindi: "open WhatsApp", "Instagram kholo", "Camera chalu karo", "व्हाट्सएप खोलो", "कैमरा चालू करो")
            lower.contains("open") || lower.contains("kholo") || lower.contains("chalu karo") || lower.contains("launch") ||
                    lower.contains("खोलो") || lower.contains("चालू करो") || lower.contains("खोल दो") || lower.contains("ओपन करो") -> {
                val appName = extractAppName(prompt)
                if (appName.isNotBlank()) {
                    AgentAction(
                        type = ActionType.OPEN_APP,
                        title = "Open ${appName.replaceFirstChar { it.uppercase() }}",
                        description = "Launch application: $appName",
                        params = mapOf("appName" to appName)
                    )
                } else null
            }

            else -> null
        }
    }

    private fun extractSongOrVideoQuery(text: String): String {
        var query = text
        val removeKeywords = listOf(
            // English & Hinglish
            "youtube open karke",
            "youtube me",
            "youtube par",
            "youtube pe",
            "youtube",
            "open karke",
            "open youtube and play",
            "open youtube",
            "ka pehla gana play karo",
            "ka pehla gana",
            "ka first song play karo",
            "ka first song",
            "ka gana play karo",
            "ka gana sunao",
            "ka gana chalao",
            "play karo",
            "play song",
            "play video",
            "first song",
            "pehla gana",
            "chalao",
            "sunao",
            "play",
            "search",
            // Devanagari Hindi
            "यूट्यूब ओपन करके",
            "यूट्यूब खोलकर",
            "यूट्यूब खोल के",
            "यूट्यूब में",
            "यूट्यूब पर",
            "यूट्यूब",
            "ओपन करके",
            "खोलकर",
            "खोल के",
            "का पहला गाना प्ले करो",
            "का पहला गाना",
            "का पहला गाना चलाओ",
            "का पहला गाना सुनाओ",
            "का गाना प्ले करो",
            "का गाना चलाओ",
            "का गाना सुनाओ",
            "पहला गाना प्ले करो",
            "पहला गाना चलाओ",
            "पहला गाना",
            "गाना प्ले करो",
            "गाना चलाओ",
            "गाना सुनाओ",
            "प्ले करो",
            "चलाओ",
            "सुनाओ",
            "खोलो और",
            "खोलो"
        )

        for (kw in removeKeywords) {
            query = query.replace(Regex(Regex.escape(kw), RegexOption.IGNORE_CASE), " ")
        }
        return query.trim()
    }

    private fun extractDestination(text: String): String {
        val keywords = listOf("to", "navigate to", "directions to", "rasta dikhao", "le chalo", "ka rasta", "रास्ता दिखाओ", "ले चलो", "का रास्ता")
        for (kw in keywords) {
            val idx = text.indexOf(kw, ignoreCase = true)
            if (idx != -1) {
                return text.substring(idx + kw.length)
                    .replace(Regex("ka rasta|rasta dikhao|का रास्ता|रास्ता दिखाओ", RegexOption.IGNORE_CASE), "")
                    .trim()
            }
        }
        return ""
    }

    private fun extractAppName(text: String): String {
        return text
            .replace(Regex("open|launch|kholo|chalu karo|karo|खोलो|चालू करो|खोल दो|ओपन करो|ओपन", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    /**
     * Executes the requested agent action using official Android APIs and Accessibility Automation
     */
    fun executeAction(action: AgentAction): Boolean {
        action.status = ActionStatus.RUNNING
        return try {
            val success = when (action.type) {
                ActionType.TOGGLE_FLASHLIGHT -> {
                    val turnOn = action.params["turnOn"]?.toBoolean() ?: true
                    toggleFlashlight(turnOn)
                }
                ActionType.SEARCH_YOUTUBE -> {
                    val query = action.params["query"] ?: ""
                    val autoPlay = action.params["autoPlay"]?.toBoolean() ?: false
                    openYouTube(query, autoPlay)
                }
                ActionType.NAVIGATE_MAPS -> {
                    val dest = action.params["destination"] ?: ""
                    openMaps(dest)
                }
                ActionType.OPEN_APP -> {
                    val appName = action.params["appName"] ?: ""
                    openAppByName(appName)
                }
                ActionType.OPEN_SETTINGS -> {
                    val setting = action.params["setting"] ?: ""
                    openSystemSettings(setting)
                }
                ActionType.SET_ALARM -> {
                    openAlarmApp()
                }
                ActionType.ADJUST_VOLUME -> {
                    adjustVolume(action.params["direction"] == "up")
                }
                ActionType.SYSTEM_NAV -> {
                    val globalAction = action.params["action"] ?: "back"
                    performSystemNav(globalAction)
                }
                ActionType.SHARE_CONTENT -> {
                    val content = action.params["content"] ?: ""
                    shareText(content)
                }
                ActionType.ANALYZE_SCREEN -> {
                    action.resultMessage = "Vision capture triggered."
                    true
                }
                ActionType.ANSWER_CALL -> {
                    answerCall(action)
                }
                ActionType.END_CALL -> {
                    endCall(action)
                }
                ActionType.SEND_SMS -> {
                    val recipient = action.params["recipient"] ?: ""
                    val message = action.params["message"] ?: ""
                    sendSms(recipient, message, action)
                }
                ActionType.READ_SMS -> {
                    readLatestSms(action)
                }
                ActionType.CUSTOM_EXPLANATION -> true
            }

            if (success) {
                action.status = ActionStatus.COMPLETED
                if (action.resultMessage == null) {
                    action.resultMessage = "Action completed."
                }
            } else {
                action.status = ActionStatus.FAILED
                if (action.resultMessage == null) {
                    action.resultMessage = "Could not execute action on this device."
                }
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error executing action: ${e.message}")
            action.status = ActionStatus.FAILED
            action.resultMessage = "Execution failed: ${e.localizedMessage ?: "Unknown error"}"
            false
        }
    }

    private fun toggleFlashlight(turnOn: Boolean): Boolean {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            ?: return false
        return try {
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return false
            cameraManager.setTorchMode(cameraId, turnOn)
            isFlashlightOn = turnOn
            true
        } catch (e: Exception) {
            Log.w(TAG, "Flashlight toggle failed: ${e.message}")
            false
        }
    }

    private fun openYouTube(query: String, autoPlay: Boolean): Boolean {
        return try {
            val searchQuery = if (query.isNotBlank()) query else "Yo Yo Honey Singh"

            // If auto-play is requested and Accessibility Service is connected, activate auto-click
            if (autoPlay && AlicAccessibilityService.isServiceEnabled) {
                AlicAccessibilityService.instance?.triggerYouTubeAutoPlay(searchQuery)
            }

            val webUrl = "https://www.youtube.com/results?search_query=" + Uri.encode(searchQuery)

            val appIntent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra("query", searchQuery)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (appIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(appIntent)
                true
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "YouTube launch error: ${e.message}")
            false
        }
    }

    private fun openAppByName(name: String): Boolean {
        val pm = context.packageManager
        val lowerName = name.lowercase().trim()

        val knownPackages = mapOf(
            "youtube" to "com.google.android.youtube",
            "यूट्यूब" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "क्रोम" to "com.android.chrome",
            "maps" to "com.google.android.apps.maps",
            "मैप्स" to "com.google.android.apps.maps",
            "instagram" to "com.instagram.android",
            "इंस्टाग्राम" to "com.instagram.android",
            "whatsapp" to "com.whatsapp",
            "व्हाट्सएप" to "com.whatsapp",
            "spotify" to "com.spotify.music",
            "स्पॉटिफ़ाई" to "com.spotify.music",
            "facebook" to "com.facebook.katana",
            "फेसबुक" to "com.facebook.katana",
            "telegram" to "org.telegram.messenger",
            "टेलीग्राम" to "org.telegram.messenger",
            "calculator" to "com.google.android.calculator",
            "कैलकुलेटर" to "com.google.android.calculator"
        )

        for ((key, pkg) in knownPackages) {
            if (lowerName.contains(key)) {
                val launchIntent = pm.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return true
                }
            }
        }

        // Camera direct intent
        if (lowerName.contains("camera") || lowerName.contains("कैमरा")) {
            val cameraIntent = Intent("android.media.action.IMAGE_CAPTURE").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            return runCatching { context.startActivity(cameraIntent); true }.getOrDefault(false)
        }

        // Settings direct intent
        if (lowerName.contains("setting") || lowerName.contains("सेटिंग")) {
            val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            return runCatching { context.startActivity(settingsIntent); true }.getOrDefault(false)
        }

        // Search installed apps dynamically
        val installedApps = pm.getInstalledApplications(0)
        for (app in installedApps) {
            val label = pm.getApplicationLabel(app).toString().lowercase()
            if (label.contains(lowerName)) {
                val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return true
                }
            }
        }

        // Fallback: Web search
        val searchIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(name))).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return runCatching { context.startActivity(searchIntent); true }.getOrDefault(false)
    }

    private fun openMaps(destination: String): Boolean {
        val uri = if (destination.isNotBlank()) {
            Uri.parse("google.navigation:q=" + Uri.encode(destination))
        } else {
            Uri.parse("geo:0,0")
        }
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            true
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/" + Uri.encode(destination))).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            runCatching { context.startActivity(webIntent); true }.getOrDefault(false)
        }
    }

    private fun openSystemSettings(type: String): Boolean {
        val intentAction = when (type.lowercase()) {
            "wifi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "sound" -> Settings.ACTION_SOUND_SETTINGS
            "display" -> Settings.ACTION_DISPLAY_SETTINGS
            "location" -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        val intent = Intent(intentAction).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return runCatching { context.startActivity(intent); true }.getOrDefault(false)
    }

    private fun openAlarmApp(): Boolean {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return runCatching { context.startActivity(intent); true }.getOrDefault(false)
    }

    private fun adjustVolume(up: Boolean): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return false
        val direction = if (up) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        audioManager.adjustVolume(direction, AudioManager.FLAG_SHOW_UI)
        return true
    }

    private fun performSystemNav(action: String): Boolean {
        val accessibility = AlicAccessibilityService.instance ?: return false
        return when (action.lowercase()) {
            "back" -> accessibility.pressBack()
            "home" -> accessibility.pressHome()
            "notifications" -> accessibility.openNotifications()
            "recents" -> accessibility.openRecents()
            else -> false
        }
    }

    private fun shareText(text: String): Boolean {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share with Alic AI").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return runCatching { context.startActivity(shareIntent); true }.getOrDefault(false)
    }

    private fun answerCall(action: AgentAction): Boolean {
        var answered = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val telecom = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            if (context.checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
                try {
                    telecom?.acceptRingingCall()
                    answered = true
                } catch (e: Exception) {
                    Log.d(TAG, "TelecomManager acceptRingingCall exception: ${e.message}")
                }
            }
        }
        if (!answered) {
            answered = AlicAccessibilityService.instance?.answerIncomingCall() ?: false
        }
        action.resultMessage = if (answered) "फोन उठा लिया गया है (Call answered)." else "कॉल उठाने का प्रयास किया गया (Attempted to answer call)."
        return answered
    }

    private fun endCall(action: AgentAction): Boolean {
        var ended = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val telecom = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            if (context.checkSelfPermission(Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
                try {
                    ended = telecom?.endCall() ?: false
                } catch (e: Exception) {
                    Log.d(TAG, "TelecomManager endCall exception: ${e.message}")
                }
            }
        }
        if (!ended) {
            ended = AlicAccessibilityService.instance?.endCurrentCall() ?: false
        }
        action.resultMessage = if (ended) "फोन काट दिया गया है (Call ended)." else "फोन काटने का प्रयास किया गया (Attempted to end call)."
        return ended
    }

    private fun sendSms(recipient: String, message: String, action: AgentAction): Boolean {
        var sent = false
        if (recipient.isNotBlank() && context.checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
                smsManager?.sendTextMessage(recipient, null, message, null, null)
                sent = true
                action.resultMessage = "मैसेज '$message' $recipient को भेज दिया गया।"
            } catch (e: Exception) {
                Log.e(TAG, "Direct SMS send failed: ${e.message}")
            }
        }
        if (!sent) {
            try {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:${if (recipient.isNotBlank()) Uri.encode(recipient) else ""}")
                    putExtra("sms_body", message)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                sent = true
                action.resultMessage = "मैसेज भेजने के लिए SMS ऐप खोल दिया गया है।"
            } catch (e: Exception) {
                action.resultMessage = "मैसेज भेजने में समस्या: ${e.message}"
            }
        }
        return sent
    }

    private fun readLatestSms(action: AgentAction): Boolean {
        return if (context.checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val cursor = context.contentResolver.query(
                    Uri.parse("content://sms/inbox"),
                    arrayOf("address", "body"),
                    null, null, "date DESC LIMIT 3"
                )
                val list = mutableListOf<String>()
                cursor?.use {
                    while (it.moveToNext()) {
                        val sender = it.getString(it.getColumnIndexOrThrow("address")) ?: "Unknown"
                        val body = it.getString(it.getColumnIndexOrThrow("body")) ?: ""
                        list.add("$sender: $body")
                    }
                }
                if (list.isNotEmpty()) {
                    action.resultMessage = "नवीनतम मैसेज:\n" + list.joinToString("\n\n")
                    true
                } else {
                    action.resultMessage = "इनबॉक्स में कोई नया मैसेज नहीं मिला।"
                    true
                }
            } catch (e: Exception) {
                action.resultMessage = "मैसेज पढ़ने में त्रुटि: ${e.message}"
                false
            }
        } else {
            action.resultMessage = "मैसेज पढ़ने के लिए Permissions Center में SMS अनुमति दें।"
            false
        }
    }

    private fun extractSmsRecipient(text: String): String {
        val lower = text.lowercase()
        // Extract numbers like 9876543210
        val phoneRegex = Regex("\\b\\d{10}\\b|\\b\\+?\\d{11,13}\\b")
        val match = phoneRegex.find(text)
        if (match != null) return match.value

        val keywords = listOf("ko message", "ko sms", "to", "send message to", "ko", "को मैसेज", "को एसएमएस", "को")
        for (kw in keywords) {
            val idx = lower.indexOf(kw)
            if (idx != -1) {
                val before = text.substring(0, idx).trim()
                val words = before.split(" ")
                if (words.isNotEmpty()) return words.last()
            }
        }
        return ""
    }

    private fun extractSmsMessage(text: String): String {
        val lower = text.lowercase()
        val keywords = listOf("bhejo", "likho", "send", "भेजो", "लिखो", "बोलो", "message:", "sms:")
        for (kw in keywords) {
            val idx = lower.indexOf(kw)
            if (idx != -1 && idx + kw.length < text.length) {
                val content = text.substring(idx + kw.length).trim()
                if (content.isNotBlank()) return content
            }
        }
        return text
    }
}
