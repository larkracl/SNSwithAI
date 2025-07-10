package com.example.snswithai.tts

import android.content.Context
import android.util.Log
import com.example.snswithai.JsonMessageListener
import org.json.JSONObject

/**
 * JsonDataManager 로부터 JSON 메시지를 받아
 * TtsManager 로 음성 합성·재생을 위임합니다.
 */
class TtsMessageListener(
    context: Context,
    apiKey: String
) : JsonMessageListener {

    // 내부에 TtsManager 하나만 가집니다
    private val ttsManager = TtsManager(context, apiKey)

    override fun onNewJsonMessage(json: String) {
        try {
            val obj = JSONObject(json)
            val aiNumber = obj.getInt("AI의 번호")
            val prompt   = obj.getString("대화 프롬프트")
            // 실제 합성 & 재생
            ttsManager.speak(aiNumber, prompt)
        } catch (e: Exception) {
            Log.e("TtsMessageListener", "TTS 처리 중 오류", e)
        }
    }

    /** Activity/Fragment가 종료될 때 반드시 호출하세요 */
    fun destroy() {
        ttsManager.destroy()
    }
}
