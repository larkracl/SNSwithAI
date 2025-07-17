package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

data class CustomCharacter(
    val age: String = "",
    val charNo: Int = 0,
    val description: String = "",
    val hobby: String = "",
    @SerializedName("imageURL")
    val imageUrl: String = "",
    val name: String = "",
    val personality: String = "",
    @SerializedName("userID")
    val userId: String = "",
    @SerializedName("voiceID")
    val voiceId: String = ""
)

