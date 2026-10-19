package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class ApiKeyStorage(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "ApiKeyStorage"
        private const val PREFS_NAME = "alic_ai_secure_prefs"
        private const val KEY_ENCRYPTED_API_KEY = "enc_gemini_api_key"
        private const val KEY_ENCRYPTED_IV = "enc_gemini_iv"
        private const val KEY_SELECTED_MODEL = "selected_gemini_model"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "AlicAiGeminiMasterKey"
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val DEFAULT_MODEL = "gemini-2.5-flash"
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            if (entry != null) {
                return entry.secretKey
            }
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val keyGenSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(keyGenSpec)
        return keyGenerator.generateKey()
    }

    /**
     * Encrypts and saves the Gemini API key using Android Keystore AES/GCM.
     */
    fun saveApiKey(rawApiKey: String): Boolean {
        val cleanKey = rawApiKey.trim()
        if (cleanKey.isEmpty()) return false

        return try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(cleanKey.toByteArray(Charsets.UTF_8))

            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val encBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)

            prefs.edit()
                .putString(KEY_ENCRYPTED_IV, ivBase64)
                .putString(KEY_ENCRYPTED_API_KEY, encBase64)
                .apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encrypt and save API key: ${e.message}")
            false
        }
    }

    /**
     * Decrypts and retrieves the stored API key.
     * Falls back to non-placeholder BuildConfig.GEMINI_API_KEY if no user key is saved.
     */
    fun getApiKey(): String? {
        val ivBase64 = prefs.getString(KEY_ENCRYPTED_IV, null)
        val encBase64 = prefs.getString(KEY_ENCRYPTED_API_KEY, null)

        if (ivBase64 != null && encBase64 != null) {
            try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
                keyStore.load(null)
                val secretKey = (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
                if (secretKey != null) {
                    val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
                    val encBytes = Base64.decode(encBase64, Base64.NO_WRAP)

                    val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
                    val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

                    val decryptedBytes = cipher.doFinal(encBytes)
                    return String(decryptedBytes, Charsets.UTF_8)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Decryption error: ${e.message}")
            }
        }

        // Check BuildConfig fallback if available and not dummy template
        val buildConfigKey: String? = try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String
        } catch (e: Throwable) {
            null
        }

        if (!buildConfigKey.isNullOrBlank() &&
            !buildConfigKey.contains("MY_GEMINI_API_KEY", ignoreCase = true) &&
            buildConfigKey != "null"
        ) {
            return buildConfigKey.trim()
        }

        return null
    }

    fun hasApiKey(): Boolean {
        return !getApiKey().isNullOrBlank()
    }

    fun hasUserSavedKey(): Boolean {
        return prefs.contains(KEY_ENCRYPTED_API_KEY)
    }

    fun deleteApiKey() {
        prefs.edit()
            .remove(KEY_ENCRYPTED_API_KEY)
            .remove(KEY_ENCRYPTED_IV)
            .apply()

        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.deleteEntry(KEY_ALIAS)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing KeyStore alias: ${e.message}")
        }
    }

    fun getMaskedApiKey(): String {
        val key = getApiKey() ?: return "No key configured"
        return maskKey(key)
    }

    fun maskKey(key: String): String {
        val trimmed = key.trim()
        return if (trimmed.length > 10) {
            val prefix = trimmed.take(6)
            val suffix = trimmed.takeLast(4)
            "$prefix••••••••$suffix"
        } else if (trimmed.isNotEmpty()) {
            "••••••••"
        } else {
            "No key"
        }
    }

    fun getSelectedModel(): String {
        return prefs.getString(KEY_SELECTED_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
    }

    fun setSelectedModel(modelId: String) {
        prefs.edit().putString(KEY_SELECTED_MODEL, modelId).apply()
    }
}
