package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

data class TimelinePost(
    val postId: String = "",
    @SerializedName("author_id")
    val authorId: String = "",
    val authorType: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: Long = 0,
    @SerializedName("liked_by")
    val likedBy: Map<String, Boolean>? = emptyMap()
)
