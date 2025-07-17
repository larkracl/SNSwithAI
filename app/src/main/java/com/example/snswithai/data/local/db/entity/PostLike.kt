package com.example.snswithai.data.local.db.entity

data class PostLike(
    val likeId: String = "",
    val userId: String = "",
    val postId: String = "",
    val likedByUserId: String = "",
    val createdAt: Long = 0
)
