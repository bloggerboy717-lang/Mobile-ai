package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

/**
 * IncomingSmsReceiver:
 * Intercepts incoming SMS messages and announces them aloud via ALIC-AI TTS:
 * "जब मैसेज आए तो मैसेज पढ़कर मुझे बताना।"
 */
class IncomingSmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "IncomingSmsReceiver"

        // Listener for active ViewModel / TTS announcement
        var onSmsReceivedListener: ((sender: String, message: String) -> Unit)? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            try {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                if (!messages.isNullOrEmpty()) {
                    val sender = messages[0].displayOriginatingAddress ?: "Unknown"
                    val fullBody = messages.joinToString(separator = "") { it.displayMessageBody ?: "" }

                    Log.i(TAG, "Incoming SMS from $sender: $fullBody")

                    // Notify active listener for speech announcement
                    onSmsReceivedListener?.invoke(sender, fullBody)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing incoming SMS", e)
            }
        }
    }
}
