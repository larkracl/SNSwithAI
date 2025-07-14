package com.example.snswithai.tts

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.*
import java.io.File

class TtsManager(
    private val context: Context,
    private val apiKey: String,
    private val voiceId: String    // ☆ 추가
) {
    private val client = ElevenLabsTtsClient(context, apiKey, voiceId)
    private val scope  = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /** 프롬프트만 넘기면, 생성시 설정된 voiceId 로 합성합니다 */
    fun speak(text: String) {
        scope.launch {
            try {
                val file = client.synthesize(text)
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
