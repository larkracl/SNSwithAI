package com.example.snswithai

import android.util.Log
import com.google.firebase.ai.type.content
import kotlinx.coroutines.coroutineScope

class ConversationManager {

    // AI에게 인격을 부여합니다.
    private val aiService = AIService(
        systemInstruction = content { text("당신은 사용자와 대화하는 AI입니다. 사용자의 질문에 친절하고 상세하게 답변해주세요.") }
    )

    private var isConversationStarted = false

    suspend fun startConversation(
        initialPrompt: String,
        onMessage: (String) -> Unit,
        onJsonMessage: (String) -> Unit
    ) {
        if (!isConversationStarted) {
            onMessage("User: $initialPrompt")
            Log.d("ConversationManager", "Initial prompt: $initialPrompt")
            // 초기 프롬프트는 AI에게 보내지 않고, 대화 시작을 알리는 용도로만 사용합니다.
            // 필요하다면 초기 프롬프트를 AI에게 보내고 응답을 받을 수도 있습니다.
            isConversationStarted = true
            Log.d("ConversationManager", "Conversation started.")
        }
    }

    suspend fun sendMessage(
        prompt: String,
        onMessage: (String) -> Unit,
        onJsonMessage: (String) -> Unit
    ) = coroutineScope {
        // 사용자 메시지 JSON으로 기록
        val userJsonMessage = "{ \"AI의 번호\": 0, \"대화 프롬프트\": \"${prompt.replace("\"", "\\\"")}\" }"
        onJsonMessage(userJsonMessage)
        Log.d("ConversationManager", "User message logged: $userJsonMessage")


        Log.d("ConversationManager", "AI generating content with prompt: $prompt")
        val response = aiService.generateContent(prompt)
        onMessage("AI: $response")
        val aiJsonMessage = "{ \"AI의 번호\": 1, \"대화 프롬프트\": \"${response.replace("\"", "\\\"")}\" }"
        onJsonMessage(aiJsonMessage) // JSON 메시지 콜백 호출
        Log.d("ConversationManager", "AI response: $response")
    }
}
