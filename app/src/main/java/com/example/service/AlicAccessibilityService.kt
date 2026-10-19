package com.example.service

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AlicAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "AlicAccessibility"

        var instance: AlicAccessibilityService? = null
            private set

        val isServiceEnabled: Boolean
            get() = instance != null

        // Pending automation task parameters
        var pendingYouTubeAutoPlayQuery: String? = null
        var shouldAutoClickFirstResult: Boolean = false
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var hasClickedFirstResultForSession = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "AlicAccessibilityService connected and ready for agentic execution.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: ""

        // Automated YouTube Auto-Play Execution
        if (shouldAutoClickFirstResult && (packageName.contains("youtube") || packageName.contains("chrome"))) {
            if (!hasClickedFirstResultForSession) {
                mainHandler.postDelayed({
                    attemptClickFirstVideo()
                }, 1200) // Small delay to let search results render
            }
        }
    }

    fun triggerYouTubeAutoPlay(query: String) {
        pendingYouTubeAutoPlayQuery = query
        shouldAutoClickFirstResult = true
        hasClickedFirstResultForSession = false

        // Schedule multiple safe retries in case results take 1-2 seconds to load
        mainHandler.postDelayed({ attemptClickFirstVideo() }, 1500)
        mainHandler.postDelayed({ attemptClickFirstVideo() }, 3000)
    }

    private fun attemptClickFirstVideo(): Boolean {
        if (hasClickedFirstResultForSession) return true

        val root = rootInActiveWindow ?: return false
        val clicked = findAndClickFirstPlayableItem(root)
        if (clicked) {
            hasClickedFirstResultForSession = true
            shouldAutoClickFirstResult = false
            Log.d(TAG, "Successfully clicked first video in YouTube!")
        }
        return clicked
    }

    private fun findAndClickFirstPlayableItem(node: AccessibilityNodeInfo): Boolean {
        // Look for common YouTube video item identifiers and clickable titles
        val targetIds = listOf(
            "com.google.android.youtube:id/thumbnail",
            "com.google.android.youtube:id/video_info_view",
            "com.google.android.youtube:id/title",
            "com.google.android.youtube:id/compact_renderer",
            "com.google.android.youtube:id/video_item"
        )

        for (id in targetIds) {
            val nodes = node.findAccessibilityNodeInfosByViewId(id)
            for (target in nodes) {
                if (performClickRecursive(target)) {
                    return true
                }
            }
        }

        // Generic fallback: traverse hierarchy and click first clickable item that isn't the search bar or back button
        return clickFirstViableChild(node, 0)
    }

    private fun clickFirstViableChild(node: AccessibilityNodeInfo, depth: Int): Boolean {
        if (depth > 8) return false

        val text = node.text?.toString() ?: ""
        val desc = node.contentDescription?.toString() ?: ""
        val lower = (text + " " + desc).lowercase()

        // Skip toolbar buttons like back, search, cast
        if (lower.contains("search") || lower.contains("back") || lower.contains("navigate up") || lower.contains("cast")) {
            // Keep looking into children
        } else if (node.isClickable && (text.length > 5 || desc.length > 5)) {
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (clickFirstViableChild(child, depth + 1)) {
                return true
            }
        }
        return false
    }

    private fun performClickRecursive(node: AccessibilityNodeInfo?): Boolean {
        var current = node
        while (current != null) {
            if (current.isClickable) {
                return current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            current = current.parent
        }
        return false
    }

    fun clickNodeByText(text: String, exact: Boolean = false): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(text)
        for (node in nodes) {
            val nodeText = node.text?.toString() ?: ""
            if (!exact || nodeText.equals(text, ignoreCase = true)) {
                if (performClickRecursive(node)) {
                    return true
                }
            }
        }
        return false
    }

    fun pressBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun pressHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun openNotifications(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    }

    fun openRecents(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_RECENTS)
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
