package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

data class ChatRoomMember(
    val role: String = "",
    @SerializedName("joined_at")
    val joinedAt: Long = 0
)
