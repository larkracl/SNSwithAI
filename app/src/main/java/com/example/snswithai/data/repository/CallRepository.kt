package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.Call
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CallRepository(private val db: FirebaseFirestore) {

    private val callsCollection = db.collection("calls")

    suspend fun createCall(call: Call) {
        callsCollection.document(call.callId).set(call).await()
    }

    suspend fun getCall(callId: String): Call? {
        return callsCollection.document(callId).get().await().toObject(Call::class.java)
    }

    suspend fun updateCall(call: Call) {
        callsCollection.document(call.callId).set(call).await()
    }

    suspend fun deleteCall(callId: String) {
        callsCollection.document(callId).delete().await()
    }

    suspend fun getCallsForUser(userId: String): List<Call> {
        return callsCollection.whereEqualTo("userId", userId).orderBy("startTime", com.google.firebase.firestore.Query.Direction.DESCENDING).get().await().toObjects(Call::class.java)
    }
}
