package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.PostLike
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class PostLikeRepository(private val db: FirebaseDatabase) {

    private val postLikesRef = db.getReference("postLikes")

    suspend fun addLikeToPost(like: PostLike) {
        postLikesRef.child(like.likeId).setValue(like).await()
    }

    suspend fun removeLikeFromPost(likeId: String) {
        postLikesRef.child(likeId).removeValue().await()
    }

    suspend fun getLikesForPost(postId: String): List<PostLike> {
        return postLikesRef.orderByChild("postId").equalTo(postId).get().await().children.mapNotNull { it.getValue(PostLike::class.java) }
    }

    suspend fun hasUserLikedPost(userId: String, postId: String): Boolean {
        val snapshot = postLikesRef.orderByChild("userId_postId").equalTo("${userId}_${postId}").get().await()
        return snapshot.exists()
    }
}
