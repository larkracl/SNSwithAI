package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

// user_data.timeline.{postId}.comments 내부 객체
data class TimelineComment(
    @SerializedName("author_id")
    val authorId: String = "",
    @SerializedName("author_name")
    val authorName: String = "",
    val text: String = "",
    @SerializedName("created_at")
    val createdAt: Long = 0
)
