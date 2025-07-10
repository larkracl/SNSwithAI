package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ChatMessageRepository(private val db: FirebaseFirestore) {

    private val chatMessagesCollection = db.collection("chatMessages")

    suspend fun createChatMessage(chatMessage: ChatMessage) {
        chatMessagesCollection.document(chatMessage.messageId).set(chatMessage).await()
    }

    suspend fun getChatMessage(messageId: String): ChatMessage? {
        return chatMessagesCollection.document(messageId).get().await().toObject(ChatMessage::class.java)
    }

    suspend fun updateChatMessage(chatMessage: ChatMessage) {
        chatMessagesCollection.document(chatMessage.messageId).set(chatMessage).await()
    }

    suspend fun deleteChatMessage(messageId: String) {
        chatMessagesCollection.document(messageId).delete().await()
    }

    suspend fun getChatMessagesForChatRoom(chatRoomId: String): List<ChatMessage> {
        return chatMessagesCollection.whereEqualTo("chatRoomId", chatRoomId).orderBy("timestamp").get().await().toObjects(ChatMessage::class.java)
    }
}
