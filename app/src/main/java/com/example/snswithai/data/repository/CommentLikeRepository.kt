package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.CommentLike
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class CommentLikeRepository(private val db: FirebaseDatabase) {

    private val commentLikesRef = db.getReference("commentLikes")

    suspend fun addLikeToComment(like: CommentLike) {
        commentLikesRef.child(like.likeId).setValue(like).await()
    }

    suspend fun removeLikeFromComment(likeId: String) {
        commentLikesRef.child(likeId).removeValue().await()
    }

    suspend fun getLikesForComment(commentId: String): List<CommentLike> {
        return commentLikesRef.orderByChild("commentId").equalTo(commentId).get().await().children.mapNotNull { it.getValue(CommentLike::class.java) }
    }

    suspend fun hasUserLikedComment(userId: String, commentId: String): Boolean {
        val snapshot = commentLikesRef.orderByChild("userId_commentId").equalTo("${userId}_${commentId}").get().await()
        return snapshot.exists()
    }
}
