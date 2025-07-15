package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.CallUtterance
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class CallUtteranceRepository(private val db: FirebaseDatabase) {

    private val callUtterancesRef = db.getReference("callUtterances")

    suspend fun createCallUtterance(utterance: CallUtterance) {
        callUtterancesRef.child(utterance.utteranceId).setValue(utterance).await()
    }

    suspend fun getCallUtterance(utteranceId: String): CallUtterance? {
        return callUtterancesRef.child(utteranceId).get().await().getValue(CallUtterance::class.java)
    }

    suspend fun updateCallUtterance(utterance: CallUtterance) {
        callUtterancesRef.child(utterance.utteranceId).setValue(utterance).await()
    }

    suspend fun deleteCallUtterance(utteranceId: String) {
        callUtterancesRef.child(utteranceId).removeValue().await()
    }

    suspend fun getUtterancesForCall(callId: String): List<CallUtterance> {
        return callUtterancesRef.orderByChild("callId").equalTo(callId).get().await().children.mapNotNull { it.getValue(CallUtterance::class.java) }
    }
}
