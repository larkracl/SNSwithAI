package com.example.snswithai

import android.util.Log
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay // delay import 추가
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.Content

class ConversationManager {

    // AI 1에게 첫 번째 인격을 부여합니다.
    private val aiService1 = AIService(
        systemInstruction = content { text("당신의 이름은 김민혁입니다. 당신은 고등학교 1학년 17살입니다. 친구인 이현규와 반말로 일상적인 대화를 하십시오. 단, 응답은 분량은 문장 1~2개로, 짧게 짧게 대화해야 합니다. 상대가 \"대화 시작\" 이라고 말한다면 그 때부터 대화를 시작하면 됩니다.") }
    )

    // AI 2에게 두 번째 인격을 부여합니다.
    private val aiService2 = AIService(
        systemInstruction = content { text("당신의 이름은 이현규입니다. 당신은 고등학교 1학년 17살입니다. 친구인 김민혁과 반말로 일상적인 대화를 하십시오. 단, 응답은 분량은 문장 1~2개로, 짧게 짧게 대화해야 합니다. 상대가 \"대화 시작\" 이라고 말한다면 그 때부터 대화를 시작하면 됩니다.") }
    )

    suspend fun startConversation(
        initialPrompt: String,
        onMessage: (String) -> Unit,
        onJsonMessage: (String) -> Unit // JSON 메시지를 전달할 새 콜백 추가
    ) = coroutineScope {
        var currentPrompt = initialPrompt
        onMessage("User: $currentPrompt")
        Log.d("ConversationManager", "Initial prompt: $currentPrompt")

        for (i in 1..5) {
            Log.d("ConversationManager", "Turn $i - AI 1 generating content with prompt: $currentPrompt")
            val response1 = aiService1.generateContent(currentPrompt)
            onMessage("AI 1: $response1")
            // JSON 키와 값 내부의 큰따옴표 모두 이스케이프
            val jsonMessage1 = "{ \"AI의 번호\": 1, \"대화 프롬프트\": \"${response1.replace("\"", "\\\"")}\" }"
            onJsonMessage(jsonMessage1) // JSON 메시지 콜백 호출
            Log.d("ConversationManager", "Turn $i - AI 1 response: $response1")
            delay(5000L) // 5초 지연 추가

            Log.d("ConversationManager", "Turn $i - AI 2 generating content with prompt: $response1")
            val response2 = aiService2.generateContent(response1)
            onMessage("AI 2: $response2")
            // JSON 키와 값 내부의 큰따옴표 모두 이스케이프
            val jsonMessage2 = "{ \"AI의 번호\": 2, \"대화 프롬프트\": \"${response2.replace("\"", "\\\"")}\" }"
            onJsonMessage(jsonMessage2) // JSON 메시지 콜백 호출
            Log.d("ConversationManager", "Turn $i - AI 2 response: $response2")
            delay(5000L) // 5초 지연 추가

            currentPrompt = response2
            Log.d("ConversationManager", "Turn $i - Next prompt will be: $currentPrompt")
        }
        Log.d("ConversationManager", "Conversation finished.")
    }
}