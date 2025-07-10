package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.CommentLike
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CommentLikeRepository(private val db: FirebaseFirestore) {

    private val commentLikesCollection = db.collection("commentLikes")

    suspend fun createCommentLike(commentLike: CommentLike) {
        commentLikesCollection.document(commentLike.likeId).set(commentLike).await()
    }

    suspend fun getCommentLike(likeId: String): CommentLike? {
        return commentLikesCollection.document(likeId).get().await().toObject(CommentLike::class.java)
    }

    suspend fun deleteCommentLike(likeId: String) {
        commentLikesCollection.document(likeId).delete().await()
    }

    suspend fun getCommentLikesForComment(commentId: String): List<CommentLike> {
        return commentLikesCollection.whereEqualTo("commentId", commentId).get().await().toObjects(CommentLike::class.java)
    }

    suspend fun getCommentLikeByUserAndComment(userId: String, commentId: String): CommentLike? {
        return commentLikesCollection
            .whereEqualTo("likedByUserId", userId)
            .whereEqualTo("commentId", commentId)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(CommentLike::class.java)
    }
}
