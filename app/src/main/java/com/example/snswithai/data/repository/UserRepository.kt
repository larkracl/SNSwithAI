package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(private val db: FirebaseFirestore) {

    private val usersCollection = db.collection("users")

    suspend fun createUser(user: User) {
        usersCollection.document(user.userId).set(user).await()
    }

    suspend fun getUser(userId: String): User? {
        return usersCollection.document(userId).get().await().toObject(User::class.java)
    }

    suspend fun updateUser(user: User) {
        usersCollection.document(user.userId).set(user).await()
    }

    suspend fun deleteUser(userId: String) {
        usersCollection.document(userId).delete().await()
    }
}
