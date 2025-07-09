package com.example.snswithai.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "calls",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["character_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["character_id"])
    ]
)
data class Call(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "user_id", index = true)
    val userId: String,

    @ColumnInfo(name = "character_id", index = true)
    val characterId: String,

    @ColumnInfo(name = "caller_id")
    val callerId: String,

    @ColumnInfo(name = "start_at")
    val startAt: Long,

    @ColumnInfo(name = "end_at")
    val endAt: Long?,

    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Int?,

    val summary: String?,

    @ColumnInfo(name = "audio_file_path")
    val audioFilePath: String?
)
