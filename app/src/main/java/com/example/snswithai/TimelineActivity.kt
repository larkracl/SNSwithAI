package com.example.snswithai

import android.R.id.message
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.snswithai.data.model.TimelinePost
import com.example.snswithai.data.repository.TimelineCommentRepository
import com.example.snswithai.data.repository.TimelinePostRepository
import com.example.snswithai.databinding.ActivityTimelineBinding
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class TimelineActivity : AppCompatActivity(), JsonMessageListener {

    private lateinit var binding: ActivityTimelineBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimelineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        JsonDataManager.registerListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        JsonDataManager.unregisterListener(this)
    }

    override fun onNewJsonMessage(json: String) {
        runOnUiThread {
            try {
                val jsonObject = JSONObject(json)
                val type = jsonObject.optString("대화 방식")
                if (type == "timeline") {
                    val aiNumber = jsonObject.optInt("AI의 번호")
                    val message = jsonObject.optString("대화 프롬프트", "메시지 없음")
                    val newLog = "AI $aiNumber: $message"
                    val currentText = binding.timelineLog.text.toString()
                    binding.timelineLog.text = if (currentText.isEmpty()) newLog else "$currentText\n$newLog"

                    // 프스트 저장
                    val db = FirebaseDatabase.getInstance()
                    val repo = TimelinePostRepository(db)
                    val timelinePost = TimelinePost(
                        authorId = "ai_$aiNumber",
                        authorName = "AI $aiNumber",
                        content = message,
                        createdAt = System.currentTimeMillis(),
                        likeCount = 0,
                        likedBy = emptyMap()
                    )

                    lifecycleScope.launch{
                        try {
                            repo.createTimelinePost(timelinePost)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                }
            } catch (e: Exception) {
                // JSON 파싱 오류 처리
                e.printStackTrace()
            }
        }
    }

}