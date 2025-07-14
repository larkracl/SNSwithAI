package com.example.snswithai

import android.util.Log
import com.google.firebase.ai.type.content
import kotlinx.coroutines.coroutineScope

class ConversationManager(private val characterId: Int) {

    // 5가지 AI 캐릭터의 성격을 정의합니다.
    private val aiPersonalities = mapOf(
        1 to "당신은 1번 AI, 친절하고 상세한 설명가입니다. 모든 질문에 최대한 자세하고 상냥하게 답변해주세요.",
        2 to "당신은 2번 AI, 츤데레 캐릭터입니다. 겉으로는 퉁명스럽지만, 속으로는 사용자를 걱정하며 은근히 도움을 주는 방식으로 답변해주세요.",
        3 to "당신은 3번 AI, 유쾌하고 활기찬 친구입니다. 항상 긍정적이고 재미있는 농담을 섞어가며 대화에 활기를 불어넣어 주세요.",
        4 to "당신은 4번 AI, 모든 것을 알고 있는 박사님입니다. 어떤 질문이든 학문적이고 전문적인 지식을 바탕으로 깊이 있게 설명해주세요.",
        5 to "당신은 5번 AI, 순수한 어린아이입니다. 세상을 처음 보는 듯한 순수한 호기심을 가지고, 쉽고 간단한 단어로 감정을 표현하며 대화해주세요."

    )

    // 선택된 characterId에 따라 다른 시스템 지침을 사용합니다.
    private val aiService = AIService(
        systemInstruction = content { text(aiPersonalities[characterId] ?: aiPersonalities[1]!!) }
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
            isConversationStarted = true
            Log.d("ConversationManager", "Conversation started with AI character #$characterId.")
        }
    }

    suspend fun sendMessage(
        prompt: String,
        onMessage: (String) -> Unit,
        onJsonMessage: (String) -> Unit
    ) = coroutineScope {
        val userJsonMessage = "{ \"AI의 번호\": \"NULL\", \"대화 프롬프트\": \"${prompt.replace("\"", "\\\"")}\", \"대화 방식\": \"conversation\" }"
        onJsonMessage(userJsonMessage)
        Log.d("ConversationManager", "User message logged: $userJsonMessage")


        Log.d("ConversationManager", "AI #$characterId generating content with prompt: $prompt")
        val response = aiService.generateContent(prompt)
        onMessage("AI #$characterId: $response")
        // JSON 메시지에 선택된 AI의 번호를 포함합니다.
        val aiJsonMessage = "{ \"AI의 번호\": $characterId, \"대화 프롬프트\": \"${response.replace("\"", "\\\"")}\", \"대화 방식\": \"conversation\" }"
        onJsonMessage(aiJsonMessage) // JSON 메시지 콜백 호출
        Log.d("ConversationManager", "AI #$characterId response: $response")
    }
}