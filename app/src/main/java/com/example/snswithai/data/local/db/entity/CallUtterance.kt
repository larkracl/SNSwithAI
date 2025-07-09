package com.example.snswithai.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "call_utterances",
    foreignKeys = [
        ForeignKey(
            entity = Call::class,
            parentColumns = ["id"],
            childColumns = ["call_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["call_id"])]
)
data class CallUtterance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "call_id", index = true)
    val callId: Long,

    val sequence: Int,

    @ColumnInfo(name = "speaker_id")
    val speakerId: String,

    val transcript: String,

    val timestamp: Long
)
