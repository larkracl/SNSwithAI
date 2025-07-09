package com.example.snswithai.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timeline_posts")
data class TimelinePost(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "author_id")
    val authorId: String,

    @ColumnInfo(name = "author_name")
    val authorName: String,

    @ColumnInfo(name = "author_profile_image_url")
    val authorProfileImageUrl: String?,

    val content: String,

    @ColumnInfo(name = "like_count", defaultValue = "0")
    val likeCount: Int,

    @ColumnInfo(name = "liked_by")
    val likedBy: String?,

    @ColumnInfo(name = "comment_count", defaultValue = "0")
    val commentCount: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
