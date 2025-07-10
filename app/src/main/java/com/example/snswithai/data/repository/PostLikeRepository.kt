package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.PostLike
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PostLikeRepository(private val db: FirebaseFirestore) {

    private val postLikesCollection = db.collection("postLikes")

    suspend fun createPostLike(postLike: PostLike) {
        postLikesCollection.document(postLike.likeId).set(postLike).await()
    }

    suspend fun getPostLike(likeId: String): PostLike? {
        return postLikesCollection.document(likeId).get().await().toObject(PostLike::class.java)
    }

    suspend fun deletePostLike(likeId: String) {
        postLikesCollection.document(likeId).delete().await()
    }

    suspend fun getPostLikesForPost(postId: String): List<PostLike> {
        return postLikesCollection.whereEqualTo("postId", postId).get().await().toObjects(PostLike::class.java)
    }

    suspend fun getPostLikeByUserAndPost(userId: String, postId: String): PostLike? {
        return postLikesCollection
            .whereEqualTo("likedByUserId", userId)
            .whereEqualTo("postId", postId)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(PostLike::class.java)
    }
}
