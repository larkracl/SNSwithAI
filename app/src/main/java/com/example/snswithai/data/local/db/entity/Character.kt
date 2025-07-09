package com.example.snswithai.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class Character(
    @PrimaryKey
    val id: String,

    val name: String,

    val personality: String,

    val appearance: String,

    @ColumnInfo(name = "is_custom", defaultValue = "0")
    val isCustom: Boolean,

    @ColumnInfo(name = "voice_id")
    val voiceId: Int
)
