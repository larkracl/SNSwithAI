package com.example.snswithai.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "timeline_comments",
    foreignKeys = [
        ForeignKey(
            entity = TimelinePost::class,
            parentColumns = ["id"],
            childColumns = ["post_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TimelineComment::class,
            parentColumns = ["id"],
            childColumns = ["parent_comment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["post_id"]),
        Index(value = ["parent_comment_id"])
    ]
)
data class TimelineComment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "post_id", index = true)
    val postId: Long,

    @ColumnInfo(name = "parent_comment_id", index = true)
    val parentCommentId: Long?,

    @ColumnInfo(name = "author_id")
    val authorId: String,

    val content: String,

    @ColumnInfo(name = "like_count", defaultValue = "0")
    val likeCount: Int,

    @ColumnInfo(name = "liked_by")
    val likedBy: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
