package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

data class ChatMessage(
    @SerializedName("sender_id")
    val senderId: String = "",
    val message: String = "",
    @SerializedName("sent_at")
    val sentAt: Long = 0
)
