package com.example.snswithai.data.local.db.entity

data class TimelineComment(
    val commentId: String = "",
    val userId: String = "",
    val postId: String = "",
    val authorId: String = "",
    val content: String = "",
    val likeCount: Int = 0,
    val createdAt: Long = 0
)