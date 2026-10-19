package com.example.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class AlicAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "AlicAccessibility"
        var instance: AlicAccessibilityService? = null
            private set

        val isServiceEnabled: Boolean
            get() = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "AlicAccessibilityService connected and ready.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Safe observation of authorized interaction events
    }

    override fun onInterrupt() {
        Log.w(TAG, "AlicAccessibilityService interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "AlicAccessibilityService destroyed.")
    }
}
