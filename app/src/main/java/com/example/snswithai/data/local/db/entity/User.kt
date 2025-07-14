package com.example.snswithai.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String, // "user_alice_123"
    val nickname: String,
    val profileImageUrl: String,
    val createdAt: Long,
    val ageGroup: String
)