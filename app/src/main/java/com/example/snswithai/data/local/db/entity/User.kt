package com.example.snswithai.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey
    val id: String,

    val nickname: String,

    val email: String,

    @ColumnInfo(name = "profile_image_url")
    val profileImageUrl: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
