package com.example.voice

import android.content.Context
import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * High-fidelity Audio Player for Gemini Voice Models.
 * Plays official Gemini human-like audio (Aoede, Charon, Puck, Fenrir, Kore)
 * directly from Gemini Multimodal Audio responses, completely bypassing robotic phone TTS.
 */
class GeminiAudioPlayer(
    private val context: Context,
    private val onPlayingStateChanged: (Boolean) -> Unit
) {
    companion object {
        private const val TAG = "GeminiAudioPlayer"
    }

    private var mediaPlayer: MediaPlayer? = null
    var isCurrentlyPlaying: Boolean = false
        private set

    /**
     * Plays base64-encoded audio received from Gemini API.
     * Supports audio/wav, audio/mp3, and raw audio/pcm (auto-wrapped with WAV header).
     */
    fun playBase64Audio(
        base64Data: String,
        mimeType: String = "audio/wav",
        onCompleted: (() -> Unit)? = null
    ): Boolean {
        stop()

        try {
            val audioBytes = Base64.decode(base64Data, Base64.DEFAULT)
            if (audioBytes.isEmpty()) {
                Log.w(TAG, "Audio data was empty.")
                return false
            }

            val finalAudioBytes = if (mimeType.contains("pcm", ignoreCase = true) || !isWavOrMp3(audioBytes)) {
                // Wrap raw PCM 24kHz with standard WAV header
                wrapPcmWithWavHeader(audioBytes, sampleRate = 24000)
            } else {
                audioBytes
            }

            val tempFile = File(context.cacheDir, "gemini_voice_stream.wav")
            FileOutputStream(tempFile).use { fos ->
                fos.write(finalAudioBytes)
                fos.flush()
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                setOnPreparedListener { mp ->
                    mp.start()
                    isCurrentlyPlaying = true
                    onPlayingStateChanged(true)
                }
                setOnCompletionListener {
                    isCurrentlyPlaying = false
                    onPlayingStateChanged(false)
                    onCompleted?.invoke()
                    cleanup()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    isCurrentlyPlaying = false
                    onPlayingStateChanged(false)
                    cleanup()
                    true
                }
                prepareAsync()
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play Gemini audio", e)
            isCurrentlyPlaying = false
            onPlayingStateChanged(false)
            cleanup()
            return false
        }
    }

    fun stop() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            cleanup()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping media player", e)
        } finally {
            isCurrentlyPlaying = false
            onPlayingStateChanged(false)
        }
    }

    private fun cleanup() {
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
        mediaPlayer = null
    }

    private fun isWavOrMp3(data: ByteArray): Boolean {
        if (data.size < 4) return false
        // Check RIFF header
        if (data[0] == 'R'.code.toByte() && data[1] == 'I'.code.toByte() &&
            data[2] == 'F'.code.toByte() && data[3] == 'F'.code.toByte()
        ) {
            return true
        }
        // Check ID3 tag for MP3
        if (data[0] == 'I'.code.toByte() && data[1] == 'D'.code.toByte() && data[2] == '3'.code.toByte()) {
            return true
        }
        return false
    }

    private fun wrapPcmWithWavHeader(pcmData: ByteArray, sampleRate: Int = 24000, channels: Int = 1, bitDepth: Int = 16): ByteArray {
        val totalDataLen = pcmData.size + 36
        val byteRate = sampleRate * channels * bitDepth / 8
        val header = ByteArray(44)

        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // PCM
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * bitDepth / 8).toByte()
        header[33] = 0
        header[34] = bitDepth.toByte()
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (pcmData.size and 0xff).toByte()
        header[41] = ((pcmData.size shr 8) and 0xff).toByte()
        header[42] = ((pcmData.size shr 16) and 0xff).toByte()
        header[43] = ((pcmData.size shr 24) and 0xff).toByte()

        return header + pcmData
    }
}
