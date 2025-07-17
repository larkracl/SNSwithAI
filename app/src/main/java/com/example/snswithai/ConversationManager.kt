package com.example.snswithai

import android.util.Log
import com.google.firebase.ai.type.content
import kotlinx.coroutines.coroutineScope

/**
 * ConversationManager는 CharacterSelectionFragment 또는 ConversationActivity로부터
 * 전달받은 시스템 프롬프트(systemPrompt)를 이용해 AIService를 초기화하고,
 * 사용자-봇 간의 대화 흐름(startConversation, sendMessage)을 관리합니다.
 */
class ConversationManager(
    private val systemPrompt: String
) {
    // systemPrompt를 그대로 AIService(systemInstruction)로 전달
    private val aiService = AIService(
        systemInstruction = content { text(systemPrompt) }
    )

    // 초기 대화가 이미 시작됐는지 추적
    private var isConversationStarted = false

    /**
     * 대화를 한 번만 초기화하는 메서드.
     * initialPrompt는 사용자의 첫 메시지(예: "안녕, 나는 너와 대화하고 싶어.")이고,
     * onMessage 콜백을 통해 UI에 표시하며, onJsonMessage 콜백으로는
     * JSON 로그를 남깁니다.
     */
    suspend fun startConversation(
        initialPrompt: String,
        onMessage: (String) -> Unit,
        onJsonMessage: (String) -> Unit
    ) {
        if (!isConversationStarted) {
            // 1) 사용자 초기 프롬프트를 UI에 표시
            onMessage("User: $initialPrompt")
            Log.d("ConversationManager", "Initial prompt: $initialPrompt")

            // 2) JSON 로그 남기기
            val initJson = """
                { "AI의 번호": "NULL", "대화 프롬프트": "${initialPrompt.replace("\"","\\\"")}", "대화 방식": "conversation" }
            """.trimIndent()
            onJsonMessage(initJson)
            Log.d("ConversationManager", "Initial JSON: $initJson")

            isConversationStarted = true
            Log.d("ConversationManager", "Conversation started with systemPrompt: $systemPrompt")
        }
    }

    /**
     * 사용자가 메시지를 보낼 때 호출합니다.
     * - prompt: 사용자가 입력한 메시지
     * - onMessage: AI의 텍스트 응답을 UI에 표시
     * - onJsonMessage: 주고받은 JSON 로그를 서버 등에 전송
     */
    suspend fun sendMessage(
        prompt: String,
        onMessage: (String) -> Unit,
        onJsonMessage: (String) -> Unit
    ) = coroutineScope {
        // 1) 사용자 메시지 JSON 로그
        val userJson = """
            { "AI의 번호": "NULL", "대화 프롬프트": "${prompt.replace("\"","\\\"")}", "대화 방식": "conversation" }
        """.trimIndent()
        onJsonMessage(userJson)
        Log.d("ConversationManager", "User JSON: $userJson")

        // 2) AIService 호출
        Log.d("ConversationManager", "Generating AI response with systemPrompt.")
        val response = aiService.generateContent(prompt)

        // 3) AI 텍스트 응답 UI 표시
        onMessage("AI: $response")
        Log.d("ConversationManager", "AI response text: $response")

        // 4) AI 응답 JSON 로그
        val aiJson = """
            { "AI의 번호": "${systemPrompt.take(10)}…", "대화 프롬프트": "${response.replace("\"","\\\"")}", "대화 방식": "conversation" }
        """.trimIndent()
        onJsonMessage(aiJson)
        Log.d("ConversationManager", "AI JSON: $aiJson")
    }
}
