package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.TimelinePost
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

class TimelinePostRepository(private val db: FirebaseFirestore) {

    private val timelinePostsCollection = db.collection("timelinePosts")

    suspend fun createTimelinePost(post: TimelinePost) {
        timelinePostsCollection.document(post.postId).set(post).await()
    }

    suspend fun getTimelinePost(postId: String): TimelinePost? {
        return timelinePostsCollection.document(postId).get().await().toObject(TimelinePost::class.java)
    }

    suspend fun updateTimelinePost(post: TimelinePost) {
        timelinePostsCollection.document(post.postId).set(post).await()
    }

    suspend fun deleteTimelinePost(postId: String) {
        timelinePostsCollection.document(postId).delete().await()
    }

    suspend fun getTimelinePostsForUser(userId: String): List<TimelinePost> {
        return timelinePostsCollection.whereEqualTo("userId", userId).orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING).get().await().toObjects(TimelinePost::class.java)
    }

    suspend fun incrementLikeCount(postId: String) {
        timelinePostsCollection.document(postId).update("likeCount", FieldValue.increment(1)).await()
    }

    suspend fun decrementLikeCount(postId: String) {
        timelinePostsCollection.document(postId).update("likeCount", FieldValue.increment(-1)).await()
    }

    suspend fun incrementCommentCount(postId: String) {
        timelinePostsCollection.document(postId).update("commentCount", FieldValue.increment(1)).await()
    }

    suspend fun decrementCommentCount(postId: String) {
        timelinePostsCollection.document(postId).update("commentCount", FieldValue.increment(-1)).await()
    }
}
