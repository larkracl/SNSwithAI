package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.TimelinePost
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.tasks.await

class TimelinePostRepository(private val db: FirebaseDatabase) {

    private val timelinePostsRef = db.getReference("timelinePosts")

    suspend fun createTimelinePost(post: TimelinePost) {
        timelinePostsRef.child(post.postId).setValue(post).await()
    }

    suspend fun getTimelinePost(postId: String): TimelinePost? {
        return timelinePostsRef.child(postId).get().await().getValue(TimelinePost::class.java)
    }

    suspend fun updateTimelinePost(post: TimelinePost) {
        timelinePostsRef.child(post.postId).setValue(post).await()
    }

    suspend fun deleteTimelinePost(postId: String) {
        timelinePostsRef.child(postId).removeValue().await()
    }

    suspend fun getTimelinePostsForUser(userId: String): List<TimelinePost> {
        return timelinePostsRef.orderByChild("userId").equalTo(userId).get().await().children.mapNotNull { it.getValue(TimelinePost::class.java) }
    }

    suspend fun incrementLikeCount(postId: String) {
        timelinePostsRef.child(postId).child("likeCount").setValue(ServerValue.increment(1)).await()
    }

    suspend fun decrementLikeCount(postId: String) {
        timelinePostsRef.child(postId).child("likeCount").setValue(ServerValue.increment(-1)).await()
    }

    suspend fun incrementCommentCount(postId: String) {
        timelinePostsRef.child(postId).child("commentCount").setValue(ServerValue.increment(1)).await()
    }

    suspend fun decrementCommentCount(postId: String) {
        timelinePostsRef.child(postId).child("commentCount").setValue(ServerValue.increment(-1)).await()
    }
}
