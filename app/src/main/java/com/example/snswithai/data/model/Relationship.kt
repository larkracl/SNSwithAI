package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

// user_data.relationships 내부 객체
data class Relationship(
    @SerializedName("affinity_score")
    val affinityScore: Int = 0,
    val status: String = ""
)
