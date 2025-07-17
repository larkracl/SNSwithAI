
// ProfileViewModel.kt
package com.example.snswithai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ktx.database
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    data class Profile(
        val name: String = "",
        val keywords: List<String> = emptyList(),
        val imageUrl: String = "",
        val isFollowing: Boolean = false
    )

    private val _profile = MutableLiveData<Profile>()
    val profile: LiveData<Profile> = _profile

    fun loadProfile(charId: String = "char101") {
        // Realtime Database 인스턴스와 경로
        val dbRef = Firebase
            .database("https://snswithai-29d1f-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("characters")
            .child(charId)

        // 한 번만 읽어오기
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // snapshot.child("name").getValue(String::class.java) 등으로 각 필드를 추출
                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val desc = snapshot.child("description").getValue(String::class.java) ?: ""
                val imageUrl = snapshot.child("imageURL").getValue(String::class.java) ?: ""
                // description을 키워드 리스트로 변환
                val keywords = desc.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                // UI 스레드에 값 업데이트
                viewModelScope.launch(Dispatchers.Main) {
                    _profile.value = Profile(
                        name = name,
                        keywords = keywords,
                        imageUrl = imageUrl,
                        isFollowing = _profile.value?.isFollowing ?: false
                    )
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // 에러 처리 (로그 출력 등)
            }
        })
    }

    fun toggleFollow() {
        _profile.value?.let {
            _profile.value = it.copy(isFollowing = !it.isFollowing)
            // TODO: 실제 팔로우 상태를 서버나 로컬에 저장하려면 여기서 호출
        }
    }
}
