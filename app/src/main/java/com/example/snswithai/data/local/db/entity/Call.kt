package com.example.snswithai.data.local.db.entity

data class Call(
    val callId: String = "",
    val userId: String = "",
    val characterId: String = "",
    val startTime: Long = 0,
    val endTime: Long = 0
)