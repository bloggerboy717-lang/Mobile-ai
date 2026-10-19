package com.example

import android.app.Application
import java.io.File

class AlicApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ensureChromiumCacheDirectories()
    }

    private fun ensureChromiumCacheDirectories() {
        try {
            // Pre-create the directory structure that Chromium simple_file_enumerator expects
            val baseCache = cacheDir
            val subdirs = listOf(
                "WebView/Default/HTTP Cache/Code Cache/js",
                "WebView/Default/HTTP Cache/Code Cache/wasm",
                "WebView/Default/HTTP Cache"
            )
            for (sub in subdirs) {
                val dir = File(baseCache, sub)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
            }
        } catch (_: Exception) {
            // Ignore if restricted
        }
    }
}
