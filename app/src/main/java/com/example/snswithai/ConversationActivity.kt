package com.example.snswithai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.snswithai.databinding.ActivityConversationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log // Log import 추가
import org.json.JSONObject // JSONObject import 추가

class ConversationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConversationBinding
    private val conversationManager = ConversationManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startConversation()
    }

    private fun startConversation() {
        CoroutineScope(Dispatchers.Main).launch {
            conversationManager.startConversation(
                "대화 시작", // initialPrompt
                { message -> // onMessage 콜백: 이제 화면에 직접 출력하지 않고, 필요시 Logcat에만 출력
                    Log.d("ConversationActivity", "Raw message received: $message") // 디버깅 용도로 유지
                },
                { jsonMessage -> // onJsonMessage 콜백: JSON 메시지를 파싱하여 출력
                    try {
                        val jsonObject = JSONObject(jsonMessage)
                        val aiNumber = jsonObject.getInt("AI의 번호") // AI의 번호 가져오기
                        val prompt = jsonObject.getString("대화 프롬프트") // 대화 프롬프트 가져오기
                        
                        val formattedMessage = "AI $aiNumber (JSON): $prompt"
                        binding.conversationLog.append(formattedMessage + "\n") // JSON 파싱 결과만 화면에 출력, 줄바꿈 추가
                        Log.d("JSON_Output_Parsed", formattedMessage) // 파싱된 메시지 Logcat 출력

                        // JSON 메시지를 JsonDataManager를 통해 게시
                        JsonDataManager.postJsonMessage(jsonMessage)

                    } catch (e: Exception) {
                        Log.e("JSON_Parse_Error", "JSON 파싱 오류: $jsonMessage", e)
                        binding.conversationLog.append("JSON 파싱 오류: ${e.message}\n") // 줄바꿈 추가
                    }
                }
            )
        }
    }
}