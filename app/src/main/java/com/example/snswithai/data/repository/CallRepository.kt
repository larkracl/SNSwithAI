package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.Call
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class CallRepository(private val db: FirebaseDatabase) {

    private val callsRef = db.getReference("calls")

    suspend fun createCall(call: Call) {
        callsRef.child(call.callId).setValue(call).await()
    }

    suspend fun getCall(callId: String): Call? {
        return callsRef.child(callId).get().await().getValue(Call::class.java)
    }

    suspend fun updateCall(call: Call) {
        callsRef.child(call.callId).setValue(call).await()
    }

    suspend fun deleteCall(callId: String) {
        callsRef.child(callId).removeValue().await()
    }

    suspend fun getCallsForUser(userId: String): List<Call> {
        return callsRef.orderByChild("userId").equalTo(userId).get().await().children.mapNotNull { it.getValue(Call::class.java) }
    }
}
