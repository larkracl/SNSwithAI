package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.ChatMessage
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class ChatMessageRepository(private val db: FirebaseDatabase) {

    private val chatMessagesRef = db.getReference("chatMessages")

    suspend fun createChatMessage(message: ChatMessage) {
        chatMessagesRef.child(message.messageId).setValue(message).await()
    }

    suspend fun getMessagesForChatRoom(chatRoomId: String): List<ChatMessage> {
        return chatMessagesRef.orderByChild("chatRoomId").equalTo(chatRoomId).get().await().children.mapNotNull { it.getValue(ChatMessage::class.java) }
    }
}
