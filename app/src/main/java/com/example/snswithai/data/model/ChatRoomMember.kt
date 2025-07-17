package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

data class ChatRoomMember(
    @SerializedName("joined_at")
    val joinedAt: Long = 0,
    val role: String = ""
)

