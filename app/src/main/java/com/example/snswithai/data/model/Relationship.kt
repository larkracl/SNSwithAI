package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

data class Relationship(
    @SerializedName("affinity_score")
    val affinityScore: Int = 0,
    val status: String = ""
)

