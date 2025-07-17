package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

data class Call(
    @SerializedName("caller_id")
    val callerId: String = "",
    @SerializedName("character_id")
    val characterId: String = "",
    @SerializedName("start_at")
    val startAt: Long = 0
)

