package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.TimelineComment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

class TimelineCommentRepository(private val db: FirebaseFirestore) {

    private val timelineCommentsCollection = db.collection("timelineComments")

    suspend fun createTimelineComment(comment: TimelineComment) {
        timelineCommentsCollection.document(comment.commentId).set(comment).await()
    }

    suspend fun getTimelineComment(commentId: String): TimelineComment? {
        return timelineCommentsCollection.document(commentId).get().await().toObject(TimelineComment::class.java)
    }

    suspend fun updateTimelineComment(comment: TimelineComment) {
        timelineCommentsCollection.document(comment.commentId).set(comment).await()
    }

    suspend fun deleteTimelineComment(commentId: String) {
        timelineCommentsCollection.document(commentId).delete().await()
    }

    suspend fun getCommentsForPost(postId: String): List<TimelineComment> {
        return timelineCommentsCollection.whereEqualTo("postId", postId).orderBy("createdAt").get().await().toObjects(TimelineComment::class.java)
    }

    suspend fun incrementLikeCount(commentId: String) {
        timelineCommentsCollection.document(commentId).update("likeCount", FieldValue.increment(1)).await()
    }

    suspend fun decrementLikeCount(commentId: String) {
        timelineCommentsCollection.document(commentId).update("likeCount", FieldValue.increment(-1)).await()
    }
}
