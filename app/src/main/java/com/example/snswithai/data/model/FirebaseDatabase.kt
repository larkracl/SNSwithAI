package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

data class FirebaseDatabase(
    val metadata: Metadata? = null,
    @SerializedName("default_settings")
    val defaultSettings: DefaultSettings? = null,
    val users: Map<String, User>? = emptyMap(),
    val characters: Map<String, CharacterModel>? = emptyMap(),
    @SerializedName("user_data")
    val userData: Map<String, UserData>? = emptyMap(),
    @SerializedName("timeline_comments")
    val timelineComments: Map<String, Map<String, TimelineComment>>? = emptyMap(),
    @SerializedName("chat_rooms_meta")
    val chatRoomsMeta: Map<String, ChatRoomMeta>? = emptyMap(),
    @SerializedName("chat_room_members")
    val chatRoomMembers: Map<String, Map<String, ChatRoomMember>>? = emptyMap(),
    @SerializedName("chat_messages")
    val chatMessages: Map<String, Map<String, ChatMessage>>? = emptyMap(),
    @SerializedName("call_utterances")
    val callUtterances: Map<String, Map<String, CallUtterance>>? = emptyMap()
)
