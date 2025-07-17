package com.example.snswithai.data.repository

import com.example.snswithai.data.model.TimelineComment
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.tasks.await

class TimelineCommentRepository(private val db: FirebaseDatabase) {

    private val timelineCommentsRef = db.getReference("timeline_comments")

    suspend fun createTimelineComment(comment: TimelineComment) {
        val commentRef = if (comment.commentId.isEmpty()) {
            timelineCommentsRef.child(comment.postId).push()
        } else {
            timelineCommentsRef.child(comment.postId).child(comment.commentId)
        }
        commentRef.setValue(comment.copy(commentId = commentRef.key ?: comment.commentId)).await()
    }

    suspend fun getTimelineComment(postId: String, commentId: String): TimelineComment? {
        return timelineCommentsRef.child(postId).child(commentId).get().await().getValue(TimelineComment::class.java)
    }

    suspend fun updateTimelineComment(comment: TimelineComment) {
        timelineCommentsRef.child(comment.postId).child(comment.commentId).setValue(comment).await()
    }

    suspend fun deleteTimelineComment(postId: String, commentId: String) {
        timelineCommentsRef.child(postId).child(commentId).removeValue().await()
    }

    suspend fun getCommentsForPost(postId: String): List<TimelineComment> {
        return timelineCommentsRef.child(postId).get().await().children.mapNotNull { it.getValue(TimelineComment::class.java) }
    }

    suspend fun incrementLikeCount(postId: String, commentId: String) {
        timelineCommentsRef.child(postId).child(commentId).child("likeCount").setValue(ServerValue.increment(1)).await()
    }

    suspend fun decrementLikeCount(postId: String, commentId: String) {
        timelineCommentsRef.child(postId).child(commentId).child("likeCount").setValue(ServerValue.increment(-1)).await()
    }
}
