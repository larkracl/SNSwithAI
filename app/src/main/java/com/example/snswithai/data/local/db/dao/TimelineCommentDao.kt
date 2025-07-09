package com.example.snswithai.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.snswithai.data.local.db.entity.TimelineComment
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelineCommentDao {
    @Query("SELECT * FROM timeline_comments")
    fun getAllTimelineComments(): Flow<List<TimelineComment>>

    @Query("SELECT * FROM timeline_comments WHERE id = :commentId")
    suspend fun getTimelineCommentById(commentId: Long): TimelineComment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineComment(comment: TimelineComment): Long

    @Update
    suspend fun updateTimelineComment(comment: TimelineComment)

    @Delete
    suspend fun deleteTimelineComment(comment: TimelineComment)

    @Query("SELECT * FROM timeline_comments WHERE post_id = :postId ORDER BY created_at ASC")
    fun getCommentsForPost(postId: Long): Flow<List<TimelineComment>>

    @Query("SELECT * FROM timeline_comments WHERE parent_comment_id = :parentCommentId ORDER BY created_at ASC")
    fun getRepliesForComment(parentCommentId: Long): Flow<List<TimelineComment>>
}
