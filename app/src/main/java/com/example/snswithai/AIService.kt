package com.example.snswithai

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.Chat
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.Content // Content 타입 import 추가

class AIService(systemInstruction: Content? = null) { // 생성자로 systemInstruction 받기

  private val chat: Chat

  init {
    val generationConfig = generationConfig {
      responseMimeType = "text/plain"
    }

    val model = Firebase.ai.generativeModel(
      modelName = "gemini-2.5-flash",
      generationConfig = generationConfig,
      systemInstruction = systemInstruction // 생성자로 받은 systemInstruction 사용
    )
    chat = model.startChat()
  }

  suspend fun generateContent(prompt: String): String {
    val message = content("user") {
      text(prompt)
    }

    val response = chat.sendMessage(message)
    return response.text ?: ""
  }
}