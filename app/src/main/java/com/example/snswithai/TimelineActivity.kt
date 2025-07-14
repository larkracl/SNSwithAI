package com.example.snswithai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.snswithai.databinding.ActivityTimelineBinding
import org.json.JSONObject

class TimelineActivity : AppCompatActivity(), JsonMessageListener {

    private lateinit var binding: ActivityTimelineBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimelineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        JsonDataManager.registerListener(this)
        TimelineManager.startTimeline()
    }

    override fun onDestroy() {
        super.onDestroy()
        JsonDataManager.unregisterListener(this)
        TimelineManager.stopTimeline()
    }

    override fun onNewJsonMessage(json: String) {
        runOnUiThread {
            try {
                val jsonObject = JSONObject(json)
                val type = jsonObject.optString("type")
                if (type == "timeline_message") {
                    val message = jsonObject.optString("message", "메시지 없음")
                    val currentText = binding.timelineLog.text.toString()
                    binding.timelineLog.text = if (currentText.isEmpty()) message else "$currentText\n$message"
                }
            } catch (e: Exception) {
                // JSON 파싱 오류 처리
            }
        }
    }
}