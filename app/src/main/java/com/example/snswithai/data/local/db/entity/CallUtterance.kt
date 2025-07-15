package com.example.snswithai.data.local.db.entity

data class CallUtterance(
    val utteranceId: String = "",
    val callId: String = "",
    val speaker: String = "",
    val text: String = "",
    val timestamp: Long = 0
)