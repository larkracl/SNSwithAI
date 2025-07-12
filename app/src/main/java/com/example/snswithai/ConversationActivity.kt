package com.example.snswithai

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.snswithai.databinding.ActivityConversationBinding
import com.example.snswithai.tts.TtsMessageListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class ConversationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConversationBinding
    private val conversationManager = ConversationManager()
    private lateinit var ttsListener: TtsMessageListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TTS 리스너 설정
        ttsListener = TtsMessageListener(this, "eleven-key")
        JsonDataManager.registerListener(ttsListener)

        // 대화 시작 (초기화)
        initializeConversation()

        // 전송 버튼 클릭 리스너 설정
        binding.sendButton.setOnClickListener {
            val userInput = binding.messageInput.text.toString()
            if (userInput.isNotBlank()) {
                // 사용자의 메시지를 화면에 표시
                binding.conversationLog.append("나: $userInput\n")
                // 사용자의 메시지를 JSON으로 기록하고 처리
                handleJsonMessage(userInput, 0) // 0은 사용자를 의미
                sendMessageToAI(userInput)
                binding.messageInput.text.clear()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        JsonDataManager.unregisterListener(ttsListener)
        ttsListener.destroy()
    }

    private fun initializeConversation() {
        CoroutineScope(Dispatchers.Main).launch {
            conversationManager.startConversation(
                "대화 시작",
                { message ->
                    // AI의 응답을 화면에 표시
                    binding.conversationLog.append("$message\n")
                },
                { jsonMessage ->
                    // 이 콜백은 이제 sendMessage에서만 사용됩니다.
                    // 필요시 초기화 단계에서도 JSON 처리가 가능합니다.
                }
            )
        }
    }

    private fun sendMessageToAI(prompt: String) {
        CoroutineScope(Dispatchers.Main).launch {
            conversationManager.sendMessage(
                prompt,
                { message -> // onMessage: AI의 응답을 받아 화면에 표시
                    binding.conversationLog.append("$message\n")
                    Log.d("ConversationActivity", "AI Response: $message")
                },
                { jsonMessage -> // onJsonMessage: 사용자 및 AI 메시지를 JSON으로 처리
                    try {
                        val jsonObject = JSONObject(jsonMessage)
                        val aiNumber = jsonObject.getInt("AI의 번호")
                        val message = jsonObject.getString("대화 프롬프트")

                        val formattedMessage = if (aiNumber == 0) {
                            "나 (JSON): $message"
                        } else {
                            "AI $aiNumber (JSON): $message"
                        }
                        Log.d("JSON_Output_Parsed", formattedMessage)

                        // AI의 메시지만 TTS로 재생
                        if (aiNumber != 0) {
                            JsonDataManager.postJsonMessage(jsonMessage)
                        }

                    } catch (e: Exception) {
                        Log.e("JSON_Parse_Error", "JSON 파싱 오류: $jsonMessage", e)
                        binding.conversationLog.append("JSON 파싱 오류: ${e.message}\n")
                    }
                }
            )
        }
    }

    // 사용자의 메시지를 JSON으로 변환하고 게시하는 함수
    private fun handleJsonMessage(message: String, senderId: Int) {
        val jsonMessage = "{ \"AI의 번호\": $senderId, \"대화 프롬프트\": \"${message.replace("\"", "\\\"")}\" }"
        Log.d("JSON_Output_User", "User message logged: $jsonMessage")
        // 사용자의 메시지는 TTS로 재생하지 않으므로 JsonDataManager에 게시하지 않습니다.
    }
}