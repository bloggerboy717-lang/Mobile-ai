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
    val server = remember { LocalAssetServer(context) }
    val activePopupDialogRef = remember { AtomicReference<Dialog?>(null) }

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

                    addJavascriptInterface(
                        AuthGateBridge(
                            onAccessGrantedCallback = onAccessGranted,
                            onSignOutCallback = onSignOut,
                            onAccessRevokedCallback = onAccessRevoked
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
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.userAgentString = view.settings.userAgentString
                                setBackgroundColor(AndroidColor.parseColor("#080C1E"))

                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        v: WebView?,
                                        req: WebResourceRequest?
                                    ): Boolean = false
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
