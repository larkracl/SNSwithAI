package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

data class UserData(
    val calls: Map<String, Call>? = null,
    @SerializedName("character_relationships")
    val characterRelationships: Map<String, Map<String, Relationship>>? = null,
    @SerializedName("chat_rooms")
    val chatRooms: Map<String, Boolean>? = null,
    @SerializedName("custom_characters")
    val customCharacters: Map<String, CustomCharacter>? = null,
    val relationships: Map<String, Relationship>? = null,
    val timeline: Map<String, TimelinePost>? = null
)
