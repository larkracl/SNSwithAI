package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

// user_data.timeline 내부 객체
data class TimelinePost(
<<<<<<< HEAD
    val postId: String = "",
=======
>>>>>>> parent of 00d9748 (entity 삭제 , model 수정)
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
