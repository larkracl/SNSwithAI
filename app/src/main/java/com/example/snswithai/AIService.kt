package com.example.snswithai

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.Chat
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.Content

class AIService(
    systemInstruction: Content? = null,
    history: List<Content> = emptyList() // history 추가
) {

    private val chat: Chat

    init {
        val generationConfig = generationConfig {
            responseMimeType = "text/plain"
        }

        val model = Firebase.ai.generativeModel(
            modelName = "gemini-2.5-flash",
            generationConfig = generationConfig,
            systemInstruction = systemInstruction
        )
        // history를 사용하여 챗 초기화
        chat = model.startChat(history)
    }

    suspend fun generateContent(prompt: String): String {
        val message = content("user") {
            text(prompt)
        }

        val response = chat.sendMessage(message)
        return response.text ?: ""
    }
}