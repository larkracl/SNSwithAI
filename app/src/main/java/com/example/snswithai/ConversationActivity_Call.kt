package com.example.snswithai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.snswithai.databinding.ActivityConversationCallBinding
import com.example.snswithai.stt.STTManager
import com.example.snswithai.tts.TtsManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConversationActivity_Call : AppCompatActivity() {

    private lateinit var binding: ActivityConversationCallBinding
    private lateinit var sttManager: STTManager
    private lateinit var ttsManager: TtsManager
    private lateinit var aiService: AIService
    private lateinit var currentChar: Character

    // RECORD_AUDIO 권한 요청 콜백
    private val requestAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) sttManager.startListening()
            else Log.e("ConversationActivity_Call", "Audio permission denied")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversationCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Chronometer 시작 (한 번만)
        binding.chronometer.base = SystemClock.elapsedRealtime()
        binding.chronometer.start()

        // 2) 버튼 리스너 초기화
        initButtons()

        // 3) Firebase에서 캐릭터 정보 로드
        Firebase.database
            .getReference("characters/char102")
            .get()
            .addOnSuccessListener { snap: DataSnapshot ->
                currentChar = snap.getValue(Character::class.java) ?: Character()
                initUi()
                initAiService()
                initTts()
                initStt()
            }
            .addOnFailureListener { e ->
                Log.e("ConversationActivity_Call", "char101 load failed", e)
                currentChar = Character()
                initUi()
                initAiService()
                initTts()
                initStt()
            }
    }

    private fun initUi() {
        binding.imagePath       = currentChar.imageURL
        binding.charName        = currentChar.name
        binding.charAge         = currentChar.age
        binding.charHobby       = currentChar.hobby
        binding.charDescription = currentChar.description
        binding.charPersonality = currentChar.personality
    }

    private fun initAiService() {
        val systemPrompt = buildString {
            append("당신은 ${currentChar.name} (나이 ${currentChar.age}, 취미 ${currentChar.hobby}) 입니다. ")
            append("${currentChar.description}. ")
            append("성격 특성: ${currentChar.personality}.")
        }
        aiService = AIService(
            systemInstruction = content { text(systemPrompt) }
        )
    }

    private fun initTts() {
        ttsManager = TtsManager(
            context = this,
            apiKey   = BuildConfig.ELEVEN_LABS_API_KEY,
            voiceId  = currentChar.voiceID
        )
    }

    private fun initStt() {
        sttManager = STTManager(
            context = this,
            onResult = { text ->
                runOnUiThread {
                    appendLog("나(음성): $text")
                    sendMessageToAi(text)
                }
            },
            onRms = { /* 빈 람다 */ }
        )
    }

    private fun initButtons() {
        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            finish()
        }
        // 텍스트 전송 버튼
        binding.sendButton.setOnClickListener {
            val txt = binding.messageInput.text.toString().trim()
            if (txt.isNotEmpty()) {
                appendLog("나: $txt")
                sendMessageToAi(txt)
                binding.messageInput.text?.clear()
            }
        }
        // 음성 입력 버튼
        binding.btnMic.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                sttManager.startListening()
            }
        }
        // 통화 종료 버튼
        binding.btnEndCall.setOnClickListener {
            // 타이머 정지
            binding.chronometer.stop()
            // 요약 다이얼로그 표시
            showCallSummaryDialog()
        }
    }

    private fun showCallSummaryDialog() {
        // 1) 통화 시간 계산
        val elapsed  = SystemClock.elapsedRealtime() - binding.chronometer.base
        val hours    = (elapsed / 3_600_000).toInt()
        val minutes  = ((elapsed % 3_600_000) / 60_000).toInt()
        val seconds  = ((elapsed % 60_000) / 1_000).toInt()
        val durationText = buildString {
            if (hours > 0) append(String.format("%d시간 ", hours))
            append(String.format("%02d분 %02d초", minutes, seconds))
        }

        // 2) 전체 대화 로그 가져오기
        val fullLog = binding.conversationLog.text.toString()

        // 3) 요약용 프롬프트 (다음 단계에서 AI 요약 기능 사용)
        val prompt = buildString {
            append("다음은 사용자와 AI가 나눈 대화입니다. 이 내용을 한국어로 간결하게 요약해 주세요.\n\n")
            append(fullLog)
        }

        // 4) AIService 호출 및 다이얼로그
        CoroutineScope(Dispatchers.Main).launch {
            val summary = try {
                aiService.generateContent(prompt)
            } catch (e: Exception) {
                "요약 중 오류가 발생했습니다."
            }

            val message = "통화 시간: $durationText\n\n$summary"
            AlertDialog.Builder(this@ConversationActivity_Call)
                .setTitle("통화 요약")
                .setMessage(message)
                .setPositiveButton("확인") { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun sendMessageToAi(prompt: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val response = aiService.generateContent(prompt)
            appendLog("AI: $response")
            ttsManager.speak(response)
        }
    }

    private fun appendLog(line: String) {
        binding.conversationLog.append("$line\n\n")
        binding.scrollView.post { binding.scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.chronometer.stop()
        sttManager.destroy()
        ttsManager.destroy()
    }
}
