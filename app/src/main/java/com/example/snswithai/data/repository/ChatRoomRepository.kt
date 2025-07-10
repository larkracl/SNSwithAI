package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.ChatRoom
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ChatRoomRepository(private val db: FirebaseFirestore) {

    private val chatRoomsCollection = db.collection("chatRooms")

    suspend fun createChatRoom(chatRoom: ChatRoom) {
        chatRoomsCollection.document(chatRoom.chatRoomId).set(chatRoom).await()
    }

    suspend fun getChatRoom(chatRoomId: String): ChatRoom? {
        return chatRoomsCollection.document(chatRoomId).get().await().toObject(ChatRoom::class.java)
    }

    suspend fun updateChatRoom(chatRoom: ChatRoom) {
        chatRoomsCollection.document(chatRoom.chatRoomId).set(chatRoom).await()
    }

    suspend fun deleteChatRoom(chatRoomId: String) {
        chatRoomsCollection.document(chatRoomId).delete().await()
    }

    suspend fun getChatRoomsForUser(userId: String): List<ChatRoom> {
        return chatRoomsCollection.whereArrayContains("members", userId).get().await().toObjects(ChatRoom::class.java)
    }

    suspend fun getChatRoomByUserAndCharacter(userId: String, characterId: String): ChatRoom? {
        return chatRoomsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("characterId", characterId)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(ChatRoom::class.java)
    }
}
