package com.example.snswithai

import android.util.Log

object JsonDataManager {

    private val listeners = mutableListOf<JsonMessageListener>()

    fun registerListener(listener: JsonMessageListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
            Log.d("JsonDataManager", "Listener registered: ${listener.javaClass.simpleName}")
        }
    }

    fun unregisterListener(listener: JsonMessageListener) {
        listeners.remove(listener)
        Log.d("JsonDataManager", "Listener unregistered: ${listener.javaClass.simpleName}")
    }

    fun postJsonMessage(json: String) {
        Log.d("JsonDataManager", "Posting JSON message to ${listeners.size} listeners.")
        listeners.forEach { listener ->
            listener.onNewJsonMessage(json)
        }
    }
}