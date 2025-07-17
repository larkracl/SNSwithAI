package com.example.snswithai.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("age_group")
    val ageGroup: String? = null,
    @SerializedName("created_at")
    val createdAt: Long? = null,
    val nickname: String? = null,
    @SerializedName("profileImageUrl")
    val profileImageUrl: String? = null, // 통일된 필드명
    val email: String? = null,
    val name: String? = null
)

