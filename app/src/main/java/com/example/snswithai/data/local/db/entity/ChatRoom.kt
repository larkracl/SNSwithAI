package com.example.snswithai.data.local.db.entity

data class ChatRoom(
    val chatRoomId: String = "",
    val userId: String = "",
    val characterId: String = "",
    val name: String = "",
    val createdAt: Long = 0,
    val members: List<String> = emptyList()
)