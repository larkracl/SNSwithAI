package com.example.snswithai.data.local.db.entity

data class CommentLike(
    val likeId: String = "",
    val userId: String = "",
    val commentId: String = "",
    val likedByUserId: String = "",
    val createdAt: Long = 0
)
