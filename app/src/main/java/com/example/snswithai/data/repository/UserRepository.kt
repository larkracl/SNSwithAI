package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class UserRepository(private val db: FirebaseDatabase) {

    private val usersRef = db.getReference("users")

    suspend fun createUser(user: User) {
        usersRef.child(user.userId).setValue(user).await()
    }

    suspend fun getUser(userId: String): User? {
        return usersRef.child(userId).get().await().getValue(User::class.java)
    }

    suspend fun updateUser(user: User) {
        usersRef.child(user.userId).setValue(user).await()
    }

    suspend fun deleteUser(userId: String) {
        usersRef.child(userId).removeValue().await()
    }
}
