package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.CallUtterance
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CallUtteranceRepository(private val db: FirebaseFirestore) {

    private val callUtterancesCollection = db.collection("callUtterances")

    suspend fun createCallUtterance(utterance: CallUtterance) {
        callUtterancesCollection.document(utterance.utteranceId).set(utterance).await()
    }

    suspend fun getCallUtterance(utteranceId: String): CallUtterance? {
        return callUtterancesCollection.document(utteranceId).get().await().toObject(CallUtterance::class.java)
    }

    suspend fun updateCallUtterance(utterance: CallUtterance) {
        callUtterancesCollection.document(utterance.utteranceId).set(utterance).await()
    }

    suspend fun deleteCallUtterance(utteranceId: String) {
        callUtterancesCollection.document(utteranceId).delete().await()
    }

    suspend fun getUtterancesForCall(callId: String): List<CallUtterance> {
        return callUtterancesCollection.whereEqualTo("callId", callId).orderBy("timestamp").get().await().toObjects(CallUtterance::class.java)
    }
}
