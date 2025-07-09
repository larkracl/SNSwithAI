package com.example.snswithai.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "chat_room_members",
    primaryKeys = ["room_id", "member_id"],
    foreignKeys = [
        ForeignKey(
            entity = ChatRoom::class,
            parentColumns = ["id"],
            childColumns = ["room_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["room_id"])]
)
data class ChatRoomMember(
    @ColumnInfo(name = "room_id", index = true)
    val roomId: String,

    @ColumnInfo(name = "member_id")
    val memberId: String,

    @ColumnInfo(name = "member_role", defaultValue = "member")
    val memberRole: String,

    @ColumnInfo(name = "joined_at")
    val joinedAt: Long
)
