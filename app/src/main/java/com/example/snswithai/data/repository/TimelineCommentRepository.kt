package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.TimelineComment
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.tasks.await

class TimelineCommentRepository(private val db: FirebaseDatabase) {

    private val timelineCommentsRef = db.getReference("timelineComments")

    suspend fun createTimelineComment(comment: TimelineComment) {
        timelineCommentsRef.child(comment.commentId).setValue(comment).await()
    }

    suspend fun getTimelineComment(commentId: String): TimelineComment? {
        return timelineCommentsRef.child(commentId).get().await().getValue(TimelineComment::class.java)
    }

    suspend fun updateTimelineComment(comment: TimelineComment) {
        timelineCommentsRef.child(comment.commentId).setValue(comment).await()
    }

    suspend fun deleteTimelineComment(commentId: String) {
        timelineCommentsRef.child(commentId).removeValue().await()
    }

    suspend fun getCommentsForPost(postId: String): List<TimelineComment> {
        return timelineCommentsRef.orderByChild("postId").equalTo(postId).get().await().children.mapNotNull { it.getValue(TimelineComment::class.java) }
    }

    suspend fun incrementLikeCount(commentId: String) {
        timelineCommentsRef.child(commentId).child("likeCount").setValue(ServerValue.increment(1)).await()
    }

    suspend fun decrementLikeCount(commentId: String) {
        timelineCommentsRef.child(commentId).child("likeCount").setValue(ServerValue.increment(-1)).await()
    }
}
