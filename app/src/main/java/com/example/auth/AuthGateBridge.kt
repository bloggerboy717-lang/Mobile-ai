package com.example.auth

import android.webkit.JavascriptInterface

/**
 * JavaScript interface bridge between Firebase JS SDK v10 Gate in WebView
 * and native Android Alic AI assistant.
 *
 * Strict Security Rules:
 * - NEVER log or accept raw access codes here
 * - Only access grant/revoke/signout signals are communicated
 */
class AuthGateBridge(
    private val onAccessGrantedCallback: (email: String) -> Unit,
    private val onSignOutCallback: () -> Unit,
    private val onAccessRevokedCallback: () -> Unit,
    private val onRequestNativeGoogleSignInCallback: () -> Unit,
    private val onDismissPopupCallback: () -> Unit = {}
) {

    @JavascriptInterface
    fun dismissPopup() {
        onDismissPopupCallback()
    }

    @JavascriptInterface
    fun requestNativeGoogleSignIn() {
        onRequestNativeGoogleSignInCallback()
    }

    @JavascriptInterface
    fun onAccessGranted(email: String?) {
        val safeEmail = email ?: ""
        onAccessGrantedCallback(safeEmail)
    }

    @JavascriptInterface
    fun onSignOut() {
        onSignOutCallback()
    }

    @JavascriptInterface
    fun onAccessRevoked() {
        onAccessRevokedCallback()
    }
}
