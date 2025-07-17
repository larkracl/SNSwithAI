package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val nickname: String = "",
    @SerializedName("profile_image_url")
    val profileImageUrl: String = "",
    @SerializedName("created_at")
    val createdAt: Long = 0,
    @SerializedName("age_group")
    val ageGroup: String? = null
)
