package com.example.snswithai.tts

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.*
import java.io.File

class TtsManager(
    private val context: Context,
    private val apiKey: String
) {
    private val voiceIdMap = mapOf(
        1 to "29vD33N1CtxCmqQRPOHJ",
        2 to "21m00Tcm4TlvDq8ikWAM",
        3 to "5Q0t7uMcjvnagumLfvZi"
    )
    private val clients by lazy {
        voiceIdMap.mapValues { ElevenLabsTtsClient(context, apiKey, it.value) }
    }
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun speak(aiNumber: Int, text: String) {
        val client = clients[aiNumber] ?: clients[1]!!
        scope.launch {
            try {
                val file: File = client.synthesize(text)
                MediaPlayer().apply {
                    setDataSource(file.absolutePath)
                    prepareAsync()
                    setOnPreparedListener { it.start() }
                    setOnCompletionListener { it.release() }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun destroy() {
        scope.cancel()
    }
}
