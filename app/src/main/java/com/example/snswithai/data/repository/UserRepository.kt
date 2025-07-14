package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.dao.UserDao
import com.example.snswithai.data.local.db.dao.UserDataDao
import com.example.snswithai.data.local.db.entity.User
import com.example.snswithai.data.local.db.entity.UserDataEntity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val userDao: UserDao,
    private val userDataDao: UserDataDao,
    private val database: FirebaseDatabase
) {

    // Firebase에서 데이터를 가져와 로컬 DB에 캐시하는 함수
    suspend fun refreshUsers() {
        try {
            val snapshot = database.getReference("users").get().await()
            val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
            userDao.insertUsers(users)
        } catch (e: Exception) {
            // Handle error
        }
    }

        suspend fun refreshUserData(userId: String) {
        try {
            val snapshot = database.getReference("user_data").child(userId).get().await()

            // 1. 먼저, JSON 구조와 일치하는 'UserData' 모델로 변환합니다.
            val userData = snapshot.getValue(com.example.snswithai.data.model.UserData::class.java)

            // 2. 변환된 'userData'와 파라미터로 받은 'userId'를 사용하여 'UserDataEntity'를 수동으로 생성합니다.
            if (userData != null) {
                val userDataEntity = UserDataEntity(userId = userId, userData = userData)
                // 3. 올바르게 생성된 Entity를 데이터베이스에 저장합니다.
                userDataDao.insertUserData(userDataEntity)
            }
        } catch (e: Exception) {
            // Handle error
            android.util.Log.e("UserRepository", "Failed to refresh user data", e)
        }
    }

    // UI에서는 로컬 DB의 Flow를 관찰
    fun getUser(userId: String) = userDao.getUser(userId)
    fun getUserData(userId: String) = userDataDao.getUserData(userId)
}