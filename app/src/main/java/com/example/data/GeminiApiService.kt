package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class GeminiApiService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()
) {

    companion object {
        private const val TAG = "GeminiApiService"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        const val SYSTEM_PROMPT =
            "You are Alic AI, a calm, friendly, and highly capable AI voice assistant developed by Rohit Raj. " +
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
     * Sends the user's prompt (with chat history) to Gemini.
     */
    suspend fun generateAssistantResponse(
        apiKey: String,
        modelId: String,
        userPrompt: String,
        recentHistory: List<ChatMessage> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured."))
        }

        val url = "$BASE_URL/$modelId:generateContent?key=$trimmedKey"

        try {
            val jsonPayload = JSONObject().apply {
                // System instructions
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", SYSTEM_PROMPT)
                        })
                    })
                })

                // Contents including recent history for context
                val contentsArray = JSONArray()

                // Include last 6 messages of history for conversational context
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

                // Add the current prompt
                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", userPrompt)
                        })
                    })
                })

                put("contents", contentsArray)

                // Generation settings
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
                            // Strip any residual awkward markdown asterisks for smooth TTS playback
                            val cleanedText = cleanTextForSpeech(replyText)
                            return@withContext Result.success(cleanedText)
                        }
                    }
                }

                Result.failure(Exception("Received empty response from Gemini."))
            }
        } catch (e: UnknownHostException) {
            Result.failure(Exception("Network error: Cannot reach Gemini API. Please check your internet connection."))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Connection timed out waiting for Gemini response."))
        } catch (e: IOException) {
            Result.failure(Exception("Network communication error: ${e.message}"))
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

    /**
     * Cleans up markdown formatting (*, #, _, ```) so text reads cleanly through Text-to-Speech
     */
    private fun cleanTextForSpeech(raw: String): String {
        return raw
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("\\*(.*?)\\*"), "$1")
            .replace(Regex("#{1,6}\\s*"), "")
            .replace(Regex("`{1,3}.*?`{1,3}"), "")
            .trim()
    }
}
