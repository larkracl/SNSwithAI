package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

// user_data.timeline 내부 객체
data class TimelinePost(
    val postId: String = "",
    @SerializedName("author_id")
    val authorId: String = "",
    @SerializedName("author_name")
    val authorName: String = "",
    val content: String = "",
    @SerializedName("created_at")
    val createdAt: Long = 0,
    @SerializedName("like_count")
    val likeCount: Int = 0,
    @SerializedName("liked_by")
    val likedBy: Map<String, Boolean>? = emptyMap()
)
