package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

data class CallUtterance(
    val sequence: Int = 0,
    @SerializedName("speaker_id")
    val speakerId: String = "",
    val transcript: String = "",
    val timestamp: Long = 0
)
