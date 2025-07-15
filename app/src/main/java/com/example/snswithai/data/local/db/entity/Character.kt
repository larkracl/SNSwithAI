package com.example.snswithai.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class Character(
    @PrimaryKey val charId: String, // "char101", "custom_alice_01"
    val charNo: Int,
    val name: String,
    val ageGroup: String,
    val description: String,
    val personality: String,
    val hobby: String,
    val imageUrl: String, // imageURL -> imageUrl
    val voiceId: String  // voiceID -> voiceId
)