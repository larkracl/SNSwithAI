package com.example.snswithai.tts

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class ElevenLabsTtsClient(
    private val context: Context,
    private val apiKey: String,
    private val voiceId: String
) {
    private val client = OkHttpClient()
    private val gson = Gson()

    suspend fun synthesize(text: String): File = withContext(Dispatchers.IO) {
        val hash = text.hashCode()
        val cacheFile = File(context.cacheDir, "tts_${voiceId}_$hash.mp3")
        if (cacheFile.exists()) return@withContext cacheFile

        val json = gson.toJson(mapOf("text" to text))
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        val req = Request.Builder()
            .url("https://api.elevenlabs.io/v1/text-to-speech/$voiceId")
            .addHeader("xi-api-key", apiKey)
            .post(body)
            .build()

        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw IOException("TTS 실패: HTTP ${resp.code}")
            resp.body!!.byteStream().use { input ->
                cacheFile.outputStream().use { output -> input.copyTo(output) }
            }
        }
        cacheFile
    }
}
