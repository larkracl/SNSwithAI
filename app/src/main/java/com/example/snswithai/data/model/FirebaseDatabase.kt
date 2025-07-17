package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

data class FirebaseDatabase(
    @SerializedName("call_utterances")
    val callUtterances: Map<String, Map<String, CallUtterance>>? = null,
    val characters: Map<String, CharacterModel>? = null,
    @SerializedName("chat_messages")
    val chatMessages: Map<String, Map<String, ChatMessage>>? = null,
    @SerializedName("chat_room_members")
    val chatRoomMembers: Map<String, Map<String, ChatRoomMember>>? = null,
    @SerializedName("chat_rooms_meta")
    val chatRoomsMeta: Map<String, ChatRoomMeta>? = null,
    val metadata: Metadata? = null,
    @SerializedName("timeline_comments")
    val timelineComments: Map<String, Map<String, TimelineComment>>? = null,
    @SerializedName("user_data")
    val userData: Map<String, UserData>? = null,
    val users: Map<String, User>? = null
)

