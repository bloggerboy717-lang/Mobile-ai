package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color as AndroidColor
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.auth.AuthGateBridge
import com.example.auth.LocalAssetServer
import com.example.ui.theme.SpaceDark
import java.util.concurrent.atomic.AtomicReference

import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import com.example.R

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AuthGateScreen(
    onAccessGranted: (email: String) -> Unit,
    onSignOut: () -> Unit,
    onAccessRevoked: () -> Unit,
    revalidationTrigger: Long = 0L,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val server = remember { LocalAssetServer(context) }
    val activePopupDialogRef = remember { AtomicReference<Dialog?>(null) }
    val webViewRef = remember { AtomicReference<WebView?>(null) }

    DisposableEffect(Unit) {
        val port = server.start()
        onDispose {
            try {
                activePopupDialogRef.getAndSet(null)?.dismiss()
            } catch (_: Exception) {}
            server.stop()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    // Use software layer rendering to prevent Mesa from querying missing DRM rendernodes
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                    setBackgroundColor(AndroidColor.parseColor("#080C1E"))

                    // Ensure Chromium cache directories exist
                    try {
                        val jsDir = File(ctx.cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
                        if (!jsDir.exists()) jsDir.mkdirs()
                        val wasmDir = File(ctx.cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm")
                        if (!wasmDir.exists()) wasmDir.mkdirs()
                    } catch (_: Exception) {}

                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        setSupportMultipleWindows(true)
                        javaScriptCanOpenWindowsAutomatically = true
                        allowFileAccess = true
                        useWideViewPort = true
                        loadWithOverviewMode = true

                        // Modern Chrome User Agent without "; wv" to allow Google OAuth
                        val defaultUa = userAgentString
                        userAgentString = defaultUa
                            .replace("; wv", "")
                            .replace("Version/4.0 ", "")
                    }

                    webViewRef.set(this)

                    addJavascriptInterface(
                        AuthGateBridge(
                            onAccessGrantedCallback = onAccessGranted,
                            onSignOutCallback = onSignOut,
                            onAccessRevokedCallback = onAccessRevoked,
                            onRequestNativeGoogleSignInCallback = {
                                coroutineScope.launch {
                                    val activity = (context as? android.app.Activity)
                                        ?: (webViewRef.get()?.context as? android.app.Activity)
                                    val targetContext = activity ?: context

                                    try {
                                        val credentialManager = CredentialManager.create(targetContext)
                                        val serverClientId = context.getString(R.string.default_web_client_id)
                                        
                                        val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                                            .build()

                                        val request = GetCredentialRequest.Builder()
                                            .addCredentialOption(googleIdOption)
                                            .build()

                                        val result = credentialManager.getCredential(
                                            request = request,
                                            context = targetContext
                                        )

                                        val credential = result.credential
                                        if (credential is androidx.credentials.CustomCredential &&
                                            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                        ) {
                                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                            val idToken = googleIdTokenCredential.idToken
                                            webViewRef.get()?.post {
                                                webViewRef.get()?.evaluateJavascript(
                                                    "if (window.signInWithGoogleIdToken) { window.signInWithGoogleIdToken('$idToken'); }",
                                                    null
                                                )
                                            }
                                        } else {
                                            // Fallback to web sign in if unexpected credential type
                                            webViewRef.get()?.post {
                                                webViewRef.get()?.evaluateJavascript(
                                                    "if (window.startWebGoogleSignIn) { window.startWebGoogleSignIn(); } else if (window.hideOverlay) { window.hideOverlay(); }",
                                                    null
                                                )
                                            }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("AuthGate", "Native Credential Manager sign-in failed, falling back to Web popup", e)
                                        // If native CredentialManager fails (e.g. SHA-1 fingerprint not yet registered in Firebase Console or user dismissed),
                                        // seamlessly trigger the web Google popup so the user is never stuck loading!
                                        webViewRef.get()?.post {
                                            webViewRef.get()?.evaluateJavascript(
                                                "if (window.startWebGoogleSignIn) { window.startWebGoogleSignIn(); } else if (window.hideOverlay) { window.hideOverlay(); }",
                                                null
                                            )
                                        }
                                    }
                                }
                            },
                            onDismissPopupCallback = {
                                activePopupDialogRef.getAndSet(null)?.dismiss()
                            }
                        ),
                        "AndroidBridge"
                    )

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url?.toString() ?: return false
                            // If user clicked the external website link, open it in the default system browser
                            if (url.startsWith("https://alic-ai-zeta.vercel.app") ||
                                (!url.contains("127.0.0.1") && !url.contains("localhost") && !url.contains("accounts.google.com") && !url.contains("firebaseapp.com"))
                            ) {
                                try {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse(url)
                                    )
                                    view?.context?.startActivity(intent)
                                    return true
                                } catch (_: Exception) {}
                            }
                            return false
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onCreateWindow(
                            view: WebView?,
                            isDialog: Boolean,
                            isUserGesture: Boolean,
                            resultMsg: Message?
                        ): Boolean {
                            val popupWebView = WebView(view!!.context).apply {
                                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                                val popupCookieManager = CookieManager.getInstance()
                                popupCookieManager.setAcceptCookie(true)
                                popupCookieManager.setAcceptThirdPartyCookies(this, true)

                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    userAgentString = view.settings.userAgentString
                                }
                                setBackgroundColor(AndroidColor.parseColor("#080C1E"))

                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        v: WebView?,
                                        req: WebResourceRequest?
                                    ): Boolean {
                                        val u = req?.url?.toString() ?: ""
                                        // When Google OAuth finishes redirecting back to localhost / firebase handler
                                        if (u.contains("localhost") || u.contains("127.0.0.1") || u.contains("/__/auth/handler")) {
                                            v?.postDelayed({
                                                try {
                                                    activePopupDialogRef.getAndSet(null)?.dismiss()
                                                } catch (_: Exception) {}
                                            }, 500)
                                        }
                                        return false
                                    }

                                    override fun onPageFinished(v: WebView?, url: String?) {
                                        super.onPageFinished(v, url)
                                        val u = url ?: ""
                                        if (u.contains("/__/auth/handler") || (u.contains("accounts.google.com") && u.contains("close"))) {
                                            v?.postDelayed({
                                                try {
                                                    activePopupDialogRef.getAndSet(null)?.dismiss()
                                                } catch (_: Exception) {}
                                            }, 800)
                                        }
                                    }
                                }
                                webChromeClient = object : WebChromeClient() {
                                    override fun onCloseWindow(window: WebView?) {
                                        try {
                                            activePopupDialogRef.getAndSet(null)?.dismiss()
                                        } catch (_: Exception) {}
                                    }
                                }
                            }

                            val dialog = Dialog(view.context, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
                                setContentView(popupWebView)
                                window?.setLayout(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setOnDismissListener {
                                    popupWebView.destroy()
                                }
                                show()
                            }
                            activePopupDialogRef.set(dialog)

                            val transport = resultMsg?.obj as? WebView.WebViewTransport
                            transport?.webView = popupWebView
                            resultMsg?.sendToTarget()
                            return true
                        }

                        override fun onCloseWindow(window: WebView?) {
                            try {
                                activePopupDialogRef.getAndSet(null)?.dismiss()
                            } catch (_: Exception) {}
                        }
                    }

                    val port = server.start()
                    loadUrl("http://localhost:$port/index.html")
                }
            },
            update = { webView ->
                if (revalidationTrigger > 0L) {
                    webView.evaluateJavascript("if (window.triggerForegroundCheck) { window.triggerForegroundCheck(); }", null)
                }
            }
        )
    }
}
