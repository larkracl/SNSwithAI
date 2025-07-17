package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

// user_data.timeline 내부 객체
data class TimelinePost(
    val postId: String = "",
    val author_id: String = "",
    val author_name: String = "",
    val content: String = "",
    val created_at: Long = 0,
    val like_count: Int = 0,
    val liked_by: Map<String, Boolean>? = emptyMap()
)


