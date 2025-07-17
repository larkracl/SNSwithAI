package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

data class TimelineComment(
    val postId: String = "", // Add postId
    val commentId: String = "", // Add commentId
    @SerializedName("author_id")
    val authorId: String = "",
    @SerializedName("author_name")
    val authorName: String = "",
    val content: String = "",
    @SerializedName("parent_comment_id")
    val parentCommentId: String? = null,
    @SerializedName("created_at")    val createdAt: Long = 0,
    @SerializedName("like_count")
    val likeCount: Int = 0,
    @SerializedName("liked_by")
    val likedBy: Map<String, Boolean>? = null
)
