package com.example.snswithai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class NewActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var promptEditText: EditText
    private lateinit var generateImageButton: Button
    private lateinit var progressBar: ProgressBar

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private val IMG_API_KEY = BuildConfig.IMG_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new)

        imageView = findViewById(R.id.imageView)
        promptEditText = findViewById(R.id.promptEditText)
        generateImageButton = findViewById(R.id.generateImageButton)
        progressBar = findViewById(R.id.progressBar)

        generateImageButton.setOnClickListener {
            val prompt = promptEditText.text.toString()
            if (prompt.isNotBlank()) {
                setLoading(true)
                lifecycleScope.launch {
                    generateContent(prompt)
                }
            } else {
                Toast.makeText(this, "프롬프트를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        generateImageButton.isEnabled = !isLoading
    }

    private suspend fun generateContent(prompt: String) {
        val modelId = "gemini-2.0-flash-preview-image-generation"
        val generateContentApi = "streamGenerateContent"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/${modelId}:${generateContentApi}?key=${IMG_API_KEY}"

        val jsonBody = gson.toJson(mapOf(
            "contents" to listOf(mapOf(
                "role" to "user",
                "parts" to listOf(mapOf("text" to prompt))
            )),
            "generationConfig" to mapOf(
                "responseModalities" to listOf("IMAGE", "TEXT"),
                "responseMimeType" to "text/plain"
            )
        ))

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)
        val request = Request.Builder().url(url).post(requestBody).build()

        withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                    // [핵심 수정] 응답 문자열에서 Base64 데이터만 추출합니다.
                    val imageBase64 = extractBase64FromResponse(responseBody)

                    if (imageBase64 != null && isActive) {
                        val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        withContext(Dispatchers.Main) {
                            imageView.setImageBitmap(bitmap)
                            Toast.makeText(this@NewActivity, "이미지 생성 완료!", Toast.LENGTH_LONG).show()
                            saveImageToGallery(bitmap, "generated_image_${System.currentTimeMillis()}.jpg")
                        }
                    } else if (isActive) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@NewActivity, "응답에서 이미지 데이터를 추출하지 못했습니다.", Toast.LENGTH_LONG).show()
                        }
                    }
                } else if (isActive) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@NewActivity, "API 오류: ${response.code}", Toast.LENGTH_LONG).show()
                        Log.e("NewActivity", "API 오류: ${response.code} - $responseBody")
                    }
                }
            } catch (e: Exception) {
                if (isActive) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@NewActivity, "요청 오류: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("NewActivity", "네트워크 또는 디코딩 예외 발생", e)
                    }
                }
            } finally {
                if (isActive) {
                    withContext(Dispatchers.Main) {
                        setLoading(false)
                    }
                }
            }
        }
    }

    /**
     * [핵심 추가]
     * 정규식을 사용하여 API의 전체 응답 문자열에서 순수한 Base64 데이터만 추출하는 함수
     */
    private fun extractBase64FromResponse(response: String): String? {
        // "data": "..." 패턴을 찾기 위한 정규식
        val pattern = Pattern.compile("\"data\":\\s*\"([A-Za-z0-9+/=]+)\"")
        val matcher = pattern.matcher(response)

        return if (matcher.find()) {
            matcher.group(1) // 첫 번째 캡처 그룹(Base64 문자열)을 반환
        } else {
            null // 패턴을 찾지 못한 경우
        }
    }
//이디다가 저장 하는지?
            private fun saveImageToGallery(bitmap: Bitmap, displayName: String) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        uri?.let {
            try {
                resolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(it, values, null, null)
                }

                Log.d("NewActivity", "Image saved to gallery: $uri")
                Toast.makeText(this, "갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Log.e("NewActivity", "Error saving image to gallery", e)
                Toast.makeText(this, "이미지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Log.e("NewActivity", "Failed to create MediaStore entry.")
            Toast.makeText(this, "이미지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}