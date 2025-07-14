package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

// user_data.calls 내부 객체
data class Call(
    @SerializedName("character_id")
    val characterId: String = "",
    @SerializedName("start_at")
    val startAt: Long = 0,
    @SerializedName("caller_id")
    val callerId: String = ""
)
