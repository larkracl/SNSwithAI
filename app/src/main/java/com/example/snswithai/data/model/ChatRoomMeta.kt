package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

data class ChatRoomMeta(
    @SerializedName("created_at")
    val createdAt: Long = 0,
    @SerializedName("room_name")
    val roomName: String = "",
    @SerializedName("room_type")
    val roomType: String = ""
)
