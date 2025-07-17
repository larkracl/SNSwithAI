package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

// 최상위 user_data 객체 전체를 나타내는 모델
data class UserData(
    @SerializedName("custom_characters")
    val customCharacters: Map<String, CustomCharacter>? = emptyMap(),
    val relationships: Map<String, Relationship>? = emptyMap(),
    @SerializedName("character_relationships")
    val characterRelationships: Map<String, Map<String, Relationship>>? = emptyMap(),
    val timeline: Map<String, TimelinePost>? = emptyMap(),
    val calls: Map<String, Call>? = emptyMap(),
    @SerializedName("chat_rooms")
    val chatRooms: Map<String, Boolean>? = emptyMap()
)
