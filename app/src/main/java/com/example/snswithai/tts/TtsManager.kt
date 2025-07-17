package com.example.snswithai.tts

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.*
import java.io.File

class TtsManager(
    private val context: Context,
    private val apiKey: String,
    private val voiceId: String
) {
    private val client = ElevenLabsTtsClient(context, apiKey, voiceId)
    private val scope  = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // 재생 중인 MediaPlayer를 보관
    private var mediaPlayer: MediaPlayer? = null

    /**
     * 텍스트를 합성해서 재생합니다.
     * 이전에 재생 중이던 플레이어가 있으면 먼저 중단·해제합니다.
     */
    fun speak(text: String) {
        // 이전 플레이어 즉시 정리
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }

        scope.launch {
            try {
                val file: File = client.synthesize(text)
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(file.absolutePath)
                    prepareAsync()
                    setOnPreparedListener { it.start() }
                    setOnCompletionListener {
                        it.release()
                        mediaPlayer = null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 호출 시 재생 중인 TTS를 즉시 중단하고,
     * 남은 코루틴 작업을 모두 취소합니다.
     */
    fun destroy() {
        // 코루틴 취소
        scope.cancel()
        // MediaPlayer 즉시 중단
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
            mediaPlayer = null
        }
    }
}
