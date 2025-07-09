package com.example.snswithai.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.snswithai.data.local.db.entity.Relationship
import kotlinx.coroutines.flow.Flow

@Dao
interface RelationshipDao {
    @Query("SELECT * FROM relationships")
    fun getAllRelationships(): Flow<List<Relationship>>

    @Query("SELECT * FROM relationships WHERE source_id = :sourceId AND target_id = :targetId")
    suspend fun getRelationship(sourceId: String, targetId: String): Relationship?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationship(relationship: Relationship)

    @Update
    suspend fun updateRelationship(relationship: Relationship)

    @Delete
    suspend fun deleteRelationship(relationship: Relationship)

    @Query("SELECT * FROM relationships WHERE source_id = :userId OR target_id = :userId")
    fun getRelationshipsForUser(userId: String): Flow<List<Relationship>>
}
