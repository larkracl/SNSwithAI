package com.example.snswithai.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_rooms")
data class ChatRoom(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "room_name")
    val roomName: String?,

    @ColumnInfo(name = "room_type")
    val roomType: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "last_message")
    val lastMessage: String?,

    @ColumnInfo(name = "last_message_timestamp")
    val lastMessageTimestamp: Long?
)
