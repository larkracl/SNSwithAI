package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

data class ChatMessage(
    val message: String = "",
    @SerializedName("sender_id")
    val senderId: String = "",
    @SerializedName("sent_at")
    val sentAt: Long = 0
)

