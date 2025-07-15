package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.ChatRoom
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class ChatRoomRepository(private val db: FirebaseDatabase) {

    private val chatRoomsRef = db.getReference("chatRooms")

    suspend fun createChatRoom(chatRoom: ChatRoom) {
        chatRoomsRef.child(chatRoom.chatRoomId).setValue(chatRoom).await()
    }

    suspend fun getChatRoom(chatRoomId: String): ChatRoom? {
        return chatRoomsRef.child(chatRoomId).get().await().getValue(ChatRoom::class.java)
    }

    suspend fun updateChatRoom(chatRoom: ChatRoom) {
        chatRoomsRef.child(chatRoom.chatRoomId).setValue(chatRoom).await()
    }

    suspend fun deleteChatRoom(chatRoomId: String) {
        chatRoomsRef.child(chatRoomId).removeValue().await()
    }

    suspend fun getChatRoomsForUser(userId: String): List<ChatRoom> {
        return chatRoomsRef.orderByChild("members/$userId").equalTo(true).get().await().children.mapNotNull { it.getValue(ChatRoom::class.java) }
    }

    suspend fun getChatRoomByUserAndCharacter(userId: String, characterId: String): ChatRoom? {
        return chatRoomsRef
            .orderByChild("userId_characterId")
            .equalTo("${userId}_${characterId}")
            .get()
            .await()
            .children
            .firstOrNull()
            ?.getValue(ChatRoom::class.java)
    }
}
