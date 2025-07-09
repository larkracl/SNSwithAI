package com.example.snswithai.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.snswithai.data.local.db.entity.TimelinePost
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelinePostDao {
    @Query("SELECT * FROM timeline_posts ORDER BY created_at DESC")
    fun getAllTimelinePosts(): Flow<List<TimelinePost>>

    @Query("SELECT * FROM timeline_posts WHERE id = :postId")
    suspend fun getTimelinePostById(postId: Long): TimelinePost?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelinePost(post: TimelinePost): Long

    @Update
    suspend fun updateTimelinePost(post: TimelinePost)

    @Delete
    suspend fun deleteTimelinePost(post: TimelinePost)

    @Query("SELECT * FROM timeline_posts WHERE author_id = :authorId ORDER BY created_at DESC")
    fun getPostsByAuthor(authorId: String): Flow<List<TimelinePost>>
}
