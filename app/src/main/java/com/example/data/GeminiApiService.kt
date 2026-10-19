package com.example.data

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class GeminiApiService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()
) {

    companion object {
        private const val TAG = "GeminiApiService"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        const val DEFAULT_SYSTEM_PROMPT =
            "You are Alic AI, a calm, friendly, and highly capable agentic AI voice assistant developed by Rohit Raj. " +
            "Your answers will be converted directly into speech via native Text-to-Speech. " +
            "Always be conversational, direct, and concise (typically 2-4 sentences unless detailed explanation is asked). " +
            "Do NOT use markdown styling like asterisks, bullet points, hashes, or code blocks because they sound awkward when read aloud. " +
            "Speak smoothly, naturally, and warmly."
    }

    /**
     * Sends a real ping/test request to Gemini to verify credentials and quota.
     */
    suspend fun testConnection(apiKey: String, modelId: String): ConnectionStatus =
        withContext(Dispatchers.IO) {
            val trimmedKey = apiKey.trim()
            if (trimmedKey.isEmpty()) {
                return@withContext ConnectionStatus.InvalidApiKey("API key cannot be empty.")
            }

            val startTime = System.currentTimeMillis()
            val url = "$BASE_URL/$modelId:generateContent?key=$trimmedKey"

            val jsonPayload = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Ping. Respond with: Alic AI is connected and ready.")
                            })
                        })
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("maxOutputTokens", 30)
                    put("temperature", 0.2)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonPayload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .header("Content-Type", "application/json")
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val latency = System.currentTimeMillis() - startTime
                    val responseBody = response.body?.string() ?: ""

                    when (response.code) {
                        200 -> {
                            val json = JSONObject(responseBody)
                            if (json.has("candidates")) {
                                ConnectionStatus.Connected(model = modelId, latencyMs = latency)
                            } else {
                                ConnectionStatus.GeneralError("Connected, but response format was unexpected.")
                            }
                        }
                        400, 403 -> {
                            val parsedMsg = parseErrorMessage(responseBody)
                            ConnectionStatus.InvalidApiKey(parsedMsg.ifEmpty { "Invalid API Key or unauthorized model access (HTTP ${response.code})." })
                        }
                        429 -> {
                            val parsedMsg = parseErrorMessage(responseBody)
                            ConnectionStatus.RateLimit(parsedMsg.ifEmpty { "Rate limit or quota exceeded for this API key (HTTP 429)." })
                        }
                        500, 503 -> {
                            ConnectionStatus.NetworkError("Gemini server error (${response.code}). Please try again later.")
                        }
                        else -> {
                            val parsedMsg = parseErrorMessage(responseBody)
                            ConnectionStatus.GeneralError("Request failed (${response.code}): $parsedMsg")
                        }
                    }
                }
            } catch (e: UnknownHostException) {
                ConnectionStatus.NetworkError("No internet connection or DNS lookup failed. Please check your network.")
            } catch (e: SocketTimeoutException) {
                ConnectionStatus.NetworkError("Connection timed out while contacting Gemini API.")
            } catch (e: IOException) {
                ConnectionStatus.NetworkError("Network communication error: ${e.localizedMessage ?: "Unknown I/O error"}")
            } catch (e: Exception) {
                ConnectionStatus.GeneralError("Failed to test connection: ${e.localizedMessage ?: e.javaClass.simpleName}")
            }
        }

    /**
     * Sends the user's prompt (with persona prompt instructions and chat history) to Gemini.
     */
    suspend fun generateAssistantResponse(
        apiKey: String,
        modelId: String,
        userPrompt: String,
        systemInstructions: String = DEFAULT_SYSTEM_PROMPT,
        recentHistory: List<ChatMessage> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured."))
        }

        val url = "$BASE_URL/$modelId:generateContent?key=$trimmedKey"

        try {
            val jsonPayload = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstructions)
                        })
                    })
                })

                val contentsArray = JSONArray()
                val contextualHistory = recentHistory.takeLast(6)
                for (msg in contextualHistory) {
                    if (msg.isError) continue
                    val role = if (msg.sender == MessageSender.USER) "user" else "model"
                    contentsArray.put(JSONObject().apply {
                        put("role", role)
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", msg.text)
                            })
                        })
                    })
                }

                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", userPrompt)
                        })
                    })
                })

                put("contents", contentsArray)

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 1000)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonPayload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .header("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    val errorDetail = parseErrorMessage(bodyString)
                    val friendlyError = when (response.code) {
                        400, 403 -> "Invalid API Key or unauthorized request ($errorDetail)"
                        429 -> "Gemini API rate limit reached ($errorDetail)"
                        500, 503 -> "Gemini service temporarily unavailable ($errorDetail)"
                        else -> "API error HTTP ${response.code}: $errorDetail"
                    }
                    return@withContext Result.failure(Exception(friendlyError))
                }

                val responseJson = JSONObject(bodyString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val replyText = parts.getJSONObject(0).optString("text", "")
                        if (replyText.isNotBlank()) {
                            val cleanedText = cleanTextForSpeech(replyText)
                            return@withContext Result.success(cleanedText)
                        }
                    }
                }

                Result.failure(Exception("Received empty response from Gemini."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Multimodal Screen Understanding: sends captured screenshot to Gemini Vision
     */
    suspend fun analyzeScreenVision(
        apiKey: String,
        modelId: String,
        bitmap: Bitmap,
        userQuery: String,
        systemInstructions: String = DEFAULT_SYSTEM_PROMPT
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured."))
        }

        val url = "$BASE_URL/$modelId:generateContent?key=$trimmedKey"

        try {
            // Compress bitmap to JPEG
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
            val imageBytes = outputStream.toByteArray()
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            val jsonPayload = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "$systemInstructions You are analyzing an authorized screenshot of the user's phone screen. Explain clearly, identify relevant buttons, texts, or options, and provide helpful guidance.")
                        })
                    })
                })

                val partsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        val promptText = if (userQuery.isNotBlank()) userQuery else "What is on my screen? Explain the current view and highlight key options or text."
                        put("text", promptText)
                    })
                    put(JSONObject().apply {
                        put("inline_data", JSONObject().apply {
                            put("mime_type", "image/jpeg")
                            put("data", base64Image)
                        })
                    })
                }

                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", partsArray)
                    })
                }

                put("contents", contentsArray)

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                    put("maxOutputTokens", 800)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonPayload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .header("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    val errorDetail = parseErrorMessage(bodyString)
                    return@withContext Result.failure(Exception("Vision analysis error HTTP ${response.code}: $errorDetail"))
                }

                val responseJson = JSONObject(bodyString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val replyText = parts.getJSONObject(0).optString("text", "")
                        if (replyText.isNotBlank()) {
                            return@withContext Result.success(cleanTextForSpeech(replyText))
                        }
                    }
                }
                Result.failure(Exception("Received empty response for screen vision."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(jsonString: String): String {
        return try {
            val json = JSONObject(jsonString)
            if (json.has("error")) {
                val errorObj = json.getJSONObject("error")
                errorObj.optString("message", "")
            } else ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun cleanTextForSpeech(raw: String): String {
        return raw
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("\\*(.*?)\\*"), "$1")
            .replace(Regex("#{1,6}\\s*"), "")
            .replace(Regex("`{1,3}.*?`{1,3}"), "")
            .trim()
    }
}
