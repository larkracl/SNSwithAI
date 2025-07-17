package com.example.snswithai.data.local.db.converter

import androidx.room.TypeConverter
import com.example.snswithai.data.model.UserData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserDataConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromUserData(userData: UserData?): String? {
        return gson.toJson(userData)
    }

    @TypeConverter
    fun toUserData(userDataString: String?): UserData? {
        if (userDataString == null) {
            return null
        }
        val type = object : TypeToken<UserData>() {}.type
        return gson.fromJson(userDataString, type)
    }
}
