package com.example.snswithai.data.local.db.entity

data class ChatMessage(
    val messageId: String = "",
    val chatRoomId: String = "",
    val senderId: String = "",
    val content: String = "",
    val timestamp: Long = 0
)