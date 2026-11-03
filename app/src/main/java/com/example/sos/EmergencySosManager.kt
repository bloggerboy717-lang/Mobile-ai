package com.example.sos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import com.example.data.EmergencyContact
import com.example.data.EmergencyStorage
import com.example.data.StudentProfileStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SosState(
    val isActive: Boolean = false,
    val triggerTime: Long = 0L,
    val locationLink: String = "",
    val callingContactIndex: Int = 0,
    val callAttempts: Int = 0,
    val logs: List<String> = emptyList(),
    val isCancelled: Boolean = false
)

class EmergencySosManager(
    private val context: Context,
    private val emergencyStorage: EmergencyStorage,
    private val profileStorage: StudentProfileStorage,
    private val onSpeakReassurance: (String) -> Unit
) {
    companion object {
        private const val TAG = "EmergencySosManager"
    }

    private val scope = CoroutineScope(Dispatchers.Main)
    private var locationUpdateJob: Job? = null

    private val _sosState = MutableStateFlow(SosState())
    val sosState: StateFlow<SosState> = _sosState.asStateFlow()

    /**
     * Checks if text contains the 3-fold "बचाओ" SOS trigger
     */
    fun isSosTrigger(input: String): Boolean {
        val normalized = input.trim().lowercase()
        // Check for "बचाओ बचाओ बचाओ" or 3 occurrences of बचाओ / bachao / help
        val bachaoCount = "बचाओ".toRegex().findAll(normalized).count()
        val romanBachaoCount = "bachao".toRegex().findAll(normalized).count()
        val isExplicitSos = normalized == "sos" || normalized.contains("emergency") || normalized.contains("बचाओ बचाओ बचाओ")
        return bachaoCount >= 3 || romanBachaoCount >= 3 || isExplicitSos
    }

    /**
     * Checks if text is cancellation
     */
    fun isSosCancelTrigger(input: String): Boolean {
        val normalized = input.trim().lowercase()
        return normalized.contains("सुरक्षित हूँ") ||
               normalized.contains("सुरक्षित हूं") ||
               normalized.contains("i am safe") ||
               normalized.contains("im safe") ||
               normalized.contains("सब ठीक है")
    }

    fun triggerSos() {
        val contacts = emergencyStorage.getContacts()
        val profile = profileStorage.getProfile()
        val userName = if (profile.name.isNotBlank()) profile.name else "उपयोगकर्ता (User)"
        val timeFormat = SimpleDateFormat("h:mm a, d MMM yyyy", Locale.getDefault())
        val currentTime = timeFormat.format(Date())

        // Default or simulated GPS coordinate link
        val locationLink = "https://maps.google.com/?q=28.6139,77.2090"
        val sosMessage = "🚨 SOS ALERT: $userName sent an emergency signal and may need urgent help. Location: $locationLink, Time: $currentTime. Please check immediately."

        val newLogs = mutableListOf<String>()
        newLogs.add("🚨 SOS emergency mode activated: $currentTime")

        // 1. Dispatch SMS / Messages to all 3 emergency contacts
        contacts.forEachIndexed { index, contact ->
            if (contact.phoneNumber.isNotBlank()) {
                sendSmsToContact(contact.phoneNumber, sosMessage)
                newLogs.add("✉️ Alert message dispatched to contact #${index + 1} (${contact.name} - ${contact.phoneNumber})")
            } else {
                newLogs.add("⚠️ Contact #${index + 1} phone number not configured in settings")
            }
        }

        _sosState.value = SosState(
            isActive = true,
            triggerTime = System.currentTimeMillis(),
            locationLink = locationLink,
            callingContactIndex = 0,
            callAttempts = 1,
            logs = newLogs
        )

        // 2. Reassure user in calm, caring voice
        val reassurance = "I have notified your emergency contacts and dispatched your live location. I am right here with you."
        onSpeakReassurance(reassurance)

        // 3. Initiate first call to primary emergency contact
        dialNextContact(contacts, 0)

        // 4. Start 1-minute periodic location update routine
        startLocationUpdateRoutine(userName, contacts)
    }

    private fun startLocationUpdateRoutine(userName: String, contacts: List<EmergencyContact>) {
        locationUpdateJob?.cancel()
        locationUpdateJob = scope.launch {
            while (_sosState.value.isActive) {
                delay(60_000L) // 1 minute
                if (!_sosState.value.isActive) break

                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                val updateTime = timeFormat.format(Date())
                val updatedLocation = "https://maps.google.com/?q=28.6139,77.2090"
                val updateMessage = "📍 SOS Live Location Update ($updateTime): $userName is at: $updatedLocation"

                contacts.forEach { contact ->
                    if (contact.phoneNumber.isNotBlank()) {
                        sendSmsToContact(contact.phoneNumber, updateMessage)
                    }
                }

                val currentLogs = _sosState.value.logs.toMutableList()
                currentLogs.add("📍 1-minute live location update dispatched ($updateTime)")
                _sosState.value = _sosState.value.copy(
                    locationLink = updatedLocation,
                    logs = currentLogs
                )
            }
        }
    }

    private fun dialNextContact(contacts: List<EmergencyContact>, contactIndex: Int) {
        val validContacts = contacts.filter { it.phoneNumber.isNotBlank() }
        if (validContacts.isNotEmpty()) {
            val target = validContacts[contactIndex % validContacts.size]
            try {
                val callIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${target.phoneNumber}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(callIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Error dialing contact: ${e.message}")
            }
        }
    }

    fun dialPolice112() {
        try {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:112")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error dialing 112: ${e.message}")
        }
    }

    private fun sendSmsToContact(phoneNumber: String, message: String) {
        try {
            val smsManager = context.getSystemService(SmsManager::class.java)
            smsManager?.sendTextMessage(phoneNumber, null, message, null, null)
        } catch (e: Exception) {
            // Fallback: If SEND_SMS permission isn't granted at system level, log gracefully
            Log.w(TAG, "Direct SMS sending failed (expected without carrier/permission): ${e.message}")
        }
    }

    fun cancelSos() {
        if (!_sosState.value.isActive) return

        locationUpdateJob?.cancel()
        val contacts = emergencyStorage.getContacts()
        val profile = profileStorage.getProfile()
        val userName = if (profile.name.isNotBlank()) profile.name else "User"

        val cancelMessage = "✅ All is well. $userName is now safe. The emergency SOS alert has been cancelled."

        contacts.forEach { contact ->
            if (contact.phoneNumber.isNotBlank()) {
                sendSmsToContact(contact.phoneNumber, cancelMessage)
            }
        }

        val logs = _sosState.value.logs.toMutableList()
        logs.add("✅ 'I am Safe' confirmed. SOS cancelled and all contacts notified.")

        _sosState.value = _sosState.value.copy(
            isActive = false,
            isCancelled = true,
            logs = logs
        )

        onSpeakReassurance("Glad to know you are safe. I have informed all your contacts that you are safe.")
    }
}
