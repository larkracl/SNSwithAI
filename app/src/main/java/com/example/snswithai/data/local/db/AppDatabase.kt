package com.example.snswithai.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.snswithai.data.local.db.dao.*
import com.example.snswithai.data.local.db.entity.*

@Database(
    entities = [
        Call::class,
        CallUtterance::class,
        Character::class,
        ChatMessage::class,
        ChatRoom::class,
        ChatRoomMember::class,
        Relationship::class,
        TimelineComment::class,
        TimelinePost::class,
        User::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun callDao(): CallDao
    abstract fun callUtteranceDao(): CallUtteranceDao
    abstract fun characterDao(): CharacterDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun chatRoomDao(): ChatRoomDao
    abstract fun chatRoomMemberDao(): ChatRoomMemberDao
    abstract fun relationshipDao(): RelationshipDao
    abstract fun timelineCommentDao(): TimelineCommentDao
    abstract fun timelinePostDao(): TimelinePostDao
}
