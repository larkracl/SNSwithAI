package com.example.snswithai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    data class Profile(
        val name: String = "",
        val description: String = "",        // ← 추가
        val keywords: List<String> = emptyList(),
        val imageUrl: String = "",
        val isFollowing: Boolean = false
    )

    private val _profile = MutableLiveData<Profile>()
    val profile: LiveData<Profile> = _profile

    fun loadProfile(userId: String) {
        // 1) users/{userId}에서 이름·키워드(Bio)·이미지 URL·팔로우 상태 읽기
        val userRef = Firebase
            .database("https://snswithai-29d1f-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
            .child(userId)

        // 2) user_data/{userId}/description 읽기
        val dataRef = Firebase
            .database("https://snswithai-29d1f-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("user_data")
            .child(userId)
            .child("description")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name      = snapshot.child("name").getValue(String::class.java) ?: ""
                val bio       = snapshot.child("bio").getValue(String::class.java) ?: ""
                val avatarUrl = snapshot.child("profileImageUrl")
                    .getValue(String::class.java)
                    ?: snapshot.child("profile_image_url").getValue(String::class.java)
                    ?: ""
                val following = snapshot.child("following")
                    .getValue(Boolean::class.java) ?: false

                val keywords = bio.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                // description만 따로 읽어서 최종 Profile 생성
                dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(ds2: DataSnapshot) {
                        val desc = ds2.getValue(String::class.java) ?: ""

                        viewModelScope.launch(Dispatchers.Main) {
                            _profile.value = Profile(
                                name        = name,
                                description = desc,        // ← 여기에 실제 데이터 반영
                                keywords    = keywords,
                                imageUrl    = avatarUrl,
                                isFollowing = following
                            )
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        // description 로드 실패해도 기본 Profile 세팅
                        viewModelScope.launch(Dispatchers.Main) {
                            _profile.value = Profile(
                                name        = name,
                                description = "",
                                keywords    = keywords,
                                imageUrl    = avatarUrl,
                                isFollowing = following
                            )
                        }
                    }
                })
            }
            override fun onCancelled(error: DatabaseError) {
                // users 데이터 로드 실패 시 로깅 등 처리
            }
        })
    }

    fun toggleFollow() {
        _profile.value?.let {
            _profile.value = it.copy(isFollowing = !it.isFollowing)
            // TODO: 서버/로컬 저장 로직
        }
    }
}
