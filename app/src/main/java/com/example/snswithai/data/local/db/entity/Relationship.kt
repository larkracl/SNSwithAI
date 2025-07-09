package com.example.snswithai.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "relationships",
    primaryKeys = ["source_id", "target_id"]
)
data class Relationship(
    @ColumnInfo(name = "source_id")
    val sourceId: String,

    @ColumnInfo(name = "target_id")
    val targetId: String,

    @ColumnInfo(name = "affinity_score", defaultValue = "50")
    val affinityScore: Int,

    @ColumnInfo(name = "relationship_status", defaultValue = "normal")
    val relationshipStatus: String
)
