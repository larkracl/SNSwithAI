package com.example.snswithai.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.snswithai.data.local.db.converter.UserDataConverter
import com.example.snswithai.data.model.UserData

@Entity(tableName = "user_data")
@TypeConverters(UserDataConverter::class)
data class UserDataEntity(
    @PrimaryKey val userId: String, // "user_alice_123"
    val userData: UserData? // This object will be stored as a JSON String
)
