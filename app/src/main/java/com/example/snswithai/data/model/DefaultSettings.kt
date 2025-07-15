package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

data class DefaultSettings(
    @SerializedName("character_relationships")
    val characterRelationships: Map<String, Map<String, Relationship>>? = emptyMap()
)
