package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

// Firebase의 characters 하위 데이터 객체와 일치하는 모델
data class CharacterModel(
    val charNo: Int = 0,
    val name: String = "",
    val age: String = "",
    val description: String = "",
    val personality: String = "",
    val hobby: String = "",
    @SerializedName("imageURL")
    val imageUrl: String = "",
    @SerializedName("voiceID")
    val voiceId: String = ""
)
