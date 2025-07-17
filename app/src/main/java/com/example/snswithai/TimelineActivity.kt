package com.example.snswithai

import android.R.id.message
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.snswithai.data.model.TimelinePost
import com.example.snswithai.data.repository.TimelineCommentRepository
import com.example.snswithai.data.repository.TimelinePostRepository
import com.example.snswithai.databinding.ActivityTimelineBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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

                    //UID 사용
                    //val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid // 실제 로그인한 사용자 UID 가져오기
//                    if (currentUserUid == null) {
//                        Log.e("TimelineActivity", "User not logged in. Cannot save post.")
//                        // 적절한 오류 처리 (예: Toast 메시지, 로그인 화면으로 이동 등)
//                        return@runOnUiThread
//                    }

                    // 1. 특정 사용자의 타임라인 경로 가져오기
                    val userTimelineRef = db.getReference("user_data")
                        .child("ZCtJmhJKr7RklOwkOjJa2OvunbA3") // 현재 로그인한 사용자의 UID 사용, currentUserUid가 원래 들어가야함, 현재 이주영 하드코딩
                        .child("timeline")

                    // 2. push()를 호출하여 userTimelineRef 하위에 새로운 고유 키를 가진 위치 참조 생성
                    // 이 newPostRef가 "-Nq_Post_Alice_01"와 같은 고유 키를 가진 경로를 가리킴
                    // 예: user_data/{currentUserUid}/timeline/{자동생성된고유키}
                    val newPostRef = userTimelineRef.push()
                    // (선택사항) 생성된 고유 키를 가져와서 객체 내에도 저장하고 싶다면
//                    val generatedPostId = newPostRef.key

                    val repo = TimelinePostRepository(db)
                    val postRef = db.getReference("user_data").child("ZCtJmhJKr7RklOwkOjJa2OvunbA3").child("timeline").push()
                    val timelinePost = TimelinePost(
                        authorId = "ai_$aiNumber",
                        authorName = "AI $aiNumber",
                        content = message,
                        createdAt = System.currentTimeMillis(),
//                        imgURL = ""
                        likeCount = 0,
                        likedBy = emptyMap()
                    )

                    // 4. push()로 얻은 참조(newPostRef)에 직접 데이터 저장
                    lifecycleScope.launch(Dispatchers.IO) { // IO Dispatcher 사용 권장 (네트워크 작업)
                        try {
                            newPostRef.setValue(timelinePost).await() // 코루틴 내에서 비동기 작업 완료 대기
                            Log.d("TimelineActivity", "New post saved successfully with key: ${newPostRef.key}")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.e("TimelineActivity", "Error saving post: ${e.message}")
                            // UI 스레드에서 Toast 등을 보여주려면 withContext(Dispatchers.Main) 사용
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@TimelineActivity, "게시물 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
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