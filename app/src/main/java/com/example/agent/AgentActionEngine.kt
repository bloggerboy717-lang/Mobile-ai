package com.example.agent

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.Settings
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
    CUSTOM_EXPLANATION
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

data class TaskPlan(
    val id: String = UUID.randomUUID().toString(),
    val userGoal: String,
    val actions: List<AgentAction>,
    var currentActionIndex: Int = 0,
    var isExecuting: Boolean = false,
    var isCompleted: Boolean = false
)

class AgentActionEngine(private val context: Context) {

    companion object {
        private const val TAG = "AgentActionEngine"
        private var isFlashlightOn = false
    }

    /**
     * Attempts to parse user prompt into actionable steps if applicable
     */
    fun parseUserIntent(prompt: String): AgentAction? {
        val lower = prompt.lowercase().trim()

        return when {
            // Flashlight controls
            lower.contains("torch") || lower.contains("flashlight") -> {
                val turnOn = !lower.contains("off")
                AgentAction(
                    type = ActionType.TOGGLE_FLASHLIGHT,
                    title = if (turnOn) "Turn On Flashlight" else "Turn Off Flashlight",
                    description = "Control device camera LED torch",
                    params = mapOf("turnOn" to turnOn.toString())
                )
            }

            // YouTube commands
            lower.contains("youtube") -> {
                val query = extractQueryAfter(lower, listOf("youtube", "play", "search", "watch"))
                AgentAction(
                    type = ActionType.SEARCH_YOUTUBE,
                    title = "Open YouTube",
                    description = if (query.isNotBlank()) "Search YouTube for: $query" else "Open YouTube application",
                    params = mapOf("query" to query)
                )
            }

            // Maps / Navigation
            lower.contains("navigate") || lower.contains("directions") || (lower.contains("map") && !lower.contains("bitmap")) -> {
                val destination = extractQueryAfter(lower, listOf("to", "navigate to", "directions to", "find", "map of"))
                AgentAction(
                    type = ActionType.NAVIGATE_MAPS,
                    title = "Open Google Maps",
                    description = if (destination.isNotBlank()) "Navigate to $destination" else "Open Maps",
                    params = mapOf("destination" to destination)
                )
            }

            // System Settings
            lower.contains("wifi") || lower.contains("wi-fi") -> {
                AgentAction(
                    type = ActionType.OPEN_SETTINGS,
                    title = "Open Wi-Fi Settings",
                    description = "Navigate to system Wi-Fi configuration",
                    params = mapOf("setting" to "wifi")
                )
            }
            lower.contains("bluetooth") -> {
                AgentAction(
                    type = ActionType.OPEN_SETTINGS,
                    title = "Open Bluetooth Settings",
                    description = "Navigate to system Bluetooth configuration",
                    params = mapOf("setting" to "bluetooth")
                )
            }
            lower.contains("sound setting") || lower.contains("volume setting") -> {
                AgentAction(
                    type = ActionType.OPEN_SETTINGS,
                    title = "Open Sound Settings",
                    description = "Configure system audio and ringtones",
                    params = mapOf("setting" to "sound")
                )
            }

            // Alarm / Timer
            lower.startsWith("set alarm") || lower.contains("wake me up") -> {
                AgentAction(
                    type = ActionType.SET_ALARM,
                    title = "Set System Alarm",
                    description = "Launch system clock to set alarm",
                    params = mapOf("prompt" to prompt),
                    requiresConfirmation = true
                )
            }

            // Screen Vision request
            lower.contains("what is on my screen") || lower.contains("see my screen") || lower.contains("analyze screen") -> {
                AgentAction(
                    type = ActionType.ANALYZE_SCREEN,
                    title = "Analyze Screen Vision",
                    description = "Capture user-authorized screen frame and evaluate with Gemini Vision"
                )
            }

            // Open specific common apps
            lower.startsWith("open ") || lower.startsWith("launch ") -> {
                val appName = lower.removePrefix("open ").removePrefix("launch ").trim()
                AgentAction(
                    type = ActionType.OPEN_APP,
                    title = "Open $appName",
                    description = "Launch application: $appName",
                    params = mapOf("appName" to appName)
                )
            }

            else -> null
        }
    }

    private fun extractQueryAfter(text: String, keywords: List<String>): String {
        for (kw in keywords) {
            val idx = text.indexOf(kw)
            if (idx != -1) {
                val remainder = text.substring(idx + kw.length).trim()
                if (remainder.isNotBlank()) return remainder
            }
        }
        return ""
    }

    /**
     * Executes the requested agent action using official Android APIs
     */
    fun executeAction(action: AgentAction): Boolean {
        action.status = ActionStatus.RUNNING
        return try {
            val success = when (action.type) {
                ActionType.TOGGLE_FLASHLIGHT -> {
                    val turnOn = action.params["turnOn"]?.toBoolean() ?: true
                    toggleFlashlight(turnOn)
                }
                ActionType.OPEN_APP -> {
                    val appName = action.params["appName"] ?: ""
                    openAppByName(appName)
                }
                ActionType.SEARCH_YOUTUBE -> {
                    val query = action.params["query"] ?: ""
                    openYouTube(query)
                }
                ActionType.NAVIGATE_MAPS -> {
                    val dest = action.params["destination"] ?: ""
                    openMaps(dest)
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
                ActionType.CUSTOM_EXPLANATION -> true
            }

            if (success) {
                action.status = ActionStatus.COMPLETED
                if (action.resultMessage == null) {
                    action.resultMessage = "Action completed successfully."
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

    private fun openAppByName(name: String): Boolean {
        val pm = context.packageManager
        val lowerName = name.lowercase()

        // Known mapping shortcuts
        val targetPackage = when {
            lowerName.contains("youtube") -> "com.google.android.youtube"
            lowerName.contains("chrome") -> "com.android.chrome"
            lowerName.contains("map") -> "com.google.android.apps.maps"
            lowerName.contains("camera") -> {
                val cameraIntent = Intent("android.media.action.IMAGE_CAPTURE").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                return runCatching { context.startActivity(cameraIntent); true }.getOrDefault(false)
            }
            lowerName.contains("setting") -> {
                val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                return runCatching { context.startActivity(settingsIntent); true }.getOrDefault(false)
            }
            lowerName.contains("instagram") -> "com.instagram.android"
            lowerName.contains("whatsapp") -> "com.whatsapp"
            lowerName.contains("spotify") -> "com.spotify.music"
            else -> null
        }

        if (targetPackage != null) {
            val launchIntent = pm.getLaunchIntentForPackage(targetPackage)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return true
            }
        }

        // Search installed applications
        val installedApps = pm.getInstalledApplications(0)
        for (app in installedApps) {
            val appLabel = pm.getApplicationLabel(app).toString().lowercase()
            if (appLabel.contains(lowerName)) {
                val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return true
                }
            }
        }

        // Fallback: search web or play store
        val searchIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$name")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return runCatching { context.startActivity(searchIntent); true }.getOrDefault(false)
    }

    private fun openYouTube(query: String): Boolean {
        return try {
            val intent = if (query.isNotBlank()) {
                Intent(Intent.ACTION_SEARCH).apply {
                    setPackage("com.google.android.youtube")
                    putExtra("query", query)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                val webUrl = if (query.isNotBlank()) "https://www.youtube.com/results?search_query=$query" else "https://www.youtube.com"
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                true
            }
        } catch (e: Exception) {
            false
        }
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
            "back" -> accessibility.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
            "home" -> accessibility.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME)
            "notifications" -> accessibility.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
            "recents" -> accessibility.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS)
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
}
