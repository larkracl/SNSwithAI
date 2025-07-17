// src/main/java/com/example/snswithai/Post.kt
package com.example.snswithai

data class Post(
    val authorName: String,
    val authorImageUrl: String,
    val content: String,
    val createdAt: Long,
    val heartCount: Int,
    val commentCount: Int
)
