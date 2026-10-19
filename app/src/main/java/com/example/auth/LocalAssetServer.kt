package com.example.auth

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

/**
 * Lightweight local server that streams the Firebase JS SDK v10 Gate assets
 * from Android assets folder over http://localhost:<port>/
 *
 * This provides:
 * 1. A valid, secure origin (http://localhost:<port>) authorized by Firebase Auth
 * 2. Full access to window.crypto.subtle.digest for SHA-256 license hashing
 * 3. Native localStorage support for session persistence
 */
class LocalAssetServer(private val context: Context) {

    private val tag = "LocalAssetServer"
    private var serverSocket: ServerSocket? = null
    @Volatile
    private var isRunning = false
    private var assignedPort = 0

    @Synchronized
    fun start(): Int {
        if (isRunning && assignedPort > 0) {
            return assignedPort
        }

        try {
            // Bind to loopback interface on an ephemeral available port
            serverSocket = ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))
            assignedPort = serverSocket!!.localPort
            isRunning = true

            val listenerThread = Thread({
                while (isRunning) {
                    try {
                        val socket = serverSocket?.accept() ?: break
                        handleClient(socket)
                    } catch (e: Exception) {
                        if (!isRunning) break
                        Log.w(tag, "Accept error", e)
                    }
                }
            }, "AlicLocalServer-Accept")
            listenerThread.isDaemon = true
            listenerThread.start()

            Log.i(tag, "LocalAssetServer started on port $assignedPort")
        } catch (e: Exception) {
            Log.e(tag, "Failed to start LocalAssetServer", e)
        }

        return assignedPort
    }

    private fun handleClient(socket: Socket) {
        Thread({
            try {
                socket.soTimeout = 8000
                val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
                val requestLine = reader.readLine() ?: return@Thread
                val parts = requestLine.split(" ")
                val requestPath = if (parts.size > 1) parts[1].substringBefore("?") else "/"

                val assetPath = when {
                    requestPath == "/" || requestPath == "/index.html" || requestPath.isEmpty() -> "gate/index.html"
                    requestPath.startsWith("/gate/") -> requestPath.removePrefix("/")
                    requestPath.startsWith("/") -> "gate" + requestPath
                    else -> "gate/$requestPath"
                }

                val output: OutputStream = socket.getOutputStream()
                try {
                    val bytes = context.assets.open(assetPath).use { it.readBytes() }
                    val contentType = when {
                        assetPath.endsWith(".html") -> "text/html; charset=utf-8"
                        assetPath.endsWith(".js") -> "application/javascript; charset=utf-8"
                        assetPath.endsWith(".css") -> "text/css; charset=utf-8"
                        assetPath.endsWith(".json") -> "application/json; charset=utf-8"
                        assetPath.endsWith(".png") -> "image/png"
                        assetPath.endsWith(".svg") -> "image/svg+xml"
                        else -> "application/octet-stream"
                    }

                    val header = ("HTTP/1.1 200 OK\r\n" +
                            "Content-Type: $contentType\r\n" +
                            "Content-Length: ${bytes.size}\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "Cache-Control: no-cache, no-store, must-revalidate\r\n" +
                            "Connection: close\r\n\r\n").toByteArray(Charsets.UTF_8)

                    output.write(header)
                    output.write(bytes)
                } catch (e: Exception) {
                    val notFound = ("HTTP/1.1 404 Not Found\r\n" +
                            "Content-Type: text/plain\r\n" +
                            "Content-Length: 9\r\n" +
                            "Connection: close\r\n\r\n" +
                            "Not Found").toByteArray(Charsets.UTF_8)
                    output.write(notFound)
                }
                output.flush()
                socket.close()
            } catch (e: Exception) {
                try { socket.close() } catch (_: Exception) {}
            }
        }, "AlicLocalServer-Worker").start()
    }

    @Synchronized
    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            Log.w(tag, "Error closing serverSocket", e)
        }
        serverSocket = null
        assignedPort = 0
    }
}
