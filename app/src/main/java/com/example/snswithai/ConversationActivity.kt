package com.example.snswithai

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.snswithai.databinding.ActivityConversationBinding
import com.example.snswithai.tts.ElevenLabsTtsClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

class ConversationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConversationBinding
    private val conversationManager = ConversationManager()

    // ① ElevenLabs API Key (공통)
    private val elevenApiKey = "eleven-key"

    // ② AI 번호(1,2,3)에 대응하는 voiceId 매핑
    private val voiceIdMap = mapOf(
        1 to "29vD33N1CtxCmqQRPOHJ",  // A
        2 to "21m00Tcm4TlvDq8ikWAM",  // B
        3 to "5Q0t7uMcjvnagumLfvZi"   // C
    )

    // ③ AI 번호별 TTS 클라이언트 생성
    private val ttsClients: Map<Int, ElevenLabsTtsClient> by lazy {
        voiceIdMap.mapValues { (_, vid) ->
            ElevenLabsTtsClient(this, elevenApiKey, vid)
        }
    }

    // ④ TTS용 코루틴 스코프
    private val ttsScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        startConversation()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Activity 종료 시 코루틴 취소
        ttsScope.cancel()
    }

    private fun startConversation() {
        // 대화 시작 → onMessage, onJsonMessage 콜백 등록
        ttsScope.launch {
            conversationManager.startConversation(
                "대화 시작",  // 초기 프롬프트
                onMessage = { msg ->
                    Log.d("ConversationActivity", "Raw message: $msg")
                },
                onJsonMessage = { jsonMessage ->
                    try {
                        // 2) JSON 파싱
                        val obj = JSONObject(jsonMessage)
                        val aiNumber = obj.getInt("AI의 번호")               // 1,2,3
                        val prompt   = obj.getString("대화 프롬프트")          // 대화 내용
                        val display  = "AI $aiNumber: $prompt"

                        // 3) 화면에 파싱된 메시지 출력
                        runOnUiThread {
                            binding.conversationLog.append("$display\n")
                        }
                        Log.d("ConversationActivity", display)

                        // 4) 해당 AI 번호의 TTS 클라이언트 선택
                        val client = ttsClients[aiNumber] ?: ttsClients[1]!!

                        // 5) 음성 합성 & 재생
                        ttsScope.launch {
                            try {
                                val mp3 = client.synthesize(prompt)
                                playAudio(mp3)
                            } catch (e: Exception) {
                                Log.e("ConversationActivity", "TTS 오류", e)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ConversationActivity", "JSON 파싱 오류", e)
                        runOnUiThread {
                            binding.conversationLog.append("파싱 오류: ${e.message}\n")
                        }
                    }
                }
            )
        }
    }

    /** MediaPlayer 로 비동기 재생 */
    private fun playAudio(file: File) {
        MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepareAsync()
            setOnPreparedListener { it.start() }
            setOnCompletionListener { it.release() }
        }
    }
}
