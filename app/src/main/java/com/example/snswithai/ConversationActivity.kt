package com.example.snswithai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.snswithai.databinding.ActivityConversationBinding
import kotlinx.coroutines.launch

class ConversationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConversationBinding
    private lateinit var conversationManager: ConversationManager // lateinit으로 변경

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_conversation)

        // Intent에서 캐릭터 ID를 가져옵니다. 기본값은 1입니다.
        val characterId = intent.getIntExtra("CHARACTER_ID", 1)
        // 가져온 ID로 ConversationManager를 초기화합니다.
        conversationManager = ConversationManager(characterId)
        // 액티비티 제목을 설정합니다.
        title = "AI #$characterId 와의 대화"

        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString()
            if (message.isNotEmpty()) {
                // 사용자가 보낸 메시지를 화면에 즉시 표시
                binding.conversationLog.append("나: $message\n\n")

                lifecycleScope.launch {
                    conversationManager.sendMessage(
                        prompt = message,
                        onMessage = { response ->
                            runOnUiThread {
                                binding.conversationLog.append("$response\n\n")
                            }
                        },
                        onJsonMessage = { jsonResponse ->
                            JsonDataManager.postJsonMessage(jsonResponse)
                        }
                    )
                }
                binding.messageInput.text.clear()
            }
        }

        // 초기 대화 시작
        lifecycleScope.launch {
            conversationManager.startConversation(
                initialPrompt = "안녕, 나는 너와 대화하고 싶어.", // ذ  일반적인 인사말로 변경
                onMessage = { initialMessage ->
                    runOnUiThread {
                        binding.conversationLog.append("$initialMessage\n\n")
                    }
                },
                onJsonMessage = { json ->
                    JsonDataManager.postJsonMessage(json)
                }
            )
        }
    }
}