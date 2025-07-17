package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

// user_data.custom_characters 내부 객체
data class CustomCharacter(
    @SerializedName("charNo")
    val charNo: Int = 0,
    @SerializedName("age_group")
    val ageGroup: String = "",
    val name: String = "",
    val description: String = "",
    val personality: String = "",
    val hobby: String = "",
    @SerializedName("imageURL")
    val imageUrl: String = "",
    @SerializedName("voiceID")
    val voiceId: String = "",
    @SerializedName("userID")
    val userId: String = ""
)
