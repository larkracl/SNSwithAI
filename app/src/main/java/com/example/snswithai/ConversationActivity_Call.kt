package com.example.snswithai

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
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

    companion object {
        // 하드코딩된 Eleven Labs API 키와 Voice ID
        private const val ELEVEN_LABS_API_KEY = "sk_f461177ce2f5907ce7f8439d03dfdb9d885732d088b6613f"
        private const val CUSTOM_VOICE_ID     = "yWgKxYsEV963gbBj3E4Z"
    }

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

        // TTS/STT 미리 초기화
        initTts()
        initStt()

        // Chronometer 시작
        binding.chronometer.base = SystemClock.elapsedRealtime()
        binding.chronometer.start()

        initButtons()
        loadCharacterFromFirebase()
    }

    /** Firebase에서 캐릭터 정보 로드 후 UI, AI 서비스 초기화 */
    private fun loadCharacterFromFirebase() {
        Firebase.database
            .getReference("characters/char101")
            .get()
            .addOnSuccessListener { snap: DataSnapshot ->
                currentChar = snap.getValue(Character::class.java) ?: Character()
                onCharacterReady()
            }
            .addOnFailureListener { e ->
                Log.e("ConversationActivity_Call", "char101 load failed", e)
                currentChar = Character()
                onCharacterReady()
            }
    }

    /** 캐릭터 로딩 완료 시점에 호출 */
    private fun onCharacterReady() {
        initUi()
        initAiService()
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
            append("성격 특성: ${currentChar.personality}. ")
            append("응답에는 이모티콘이나 별표(*) 같은 특수문자, 마크다운 문법을 포함하지 말고, ")
            append("자연스럽고 간결한 한국어 문장만 사용하세요.")
        }
        aiService = AIService(
            systemInstruction = content { text(systemPrompt) }
        )
    }

    /** TTS 매니저 초기화 */
    private fun initTts() {
        ttsManager = TtsManager(
            context = this,
            apiKey   = ELEVEN_LABS_API_KEY,
            voiceId  = CUSTOM_VOICE_ID
        )
    }

    /** STT 매니저 초기화 */
    private fun initStt() {
        sttManager = STTManager(
            context = this,
            onResult = { text ->
                runOnUiThread {
                    appendLog("나(음성): $text")
                    sendMessageToAi(text)
                }
            },
            onRms = { /* 비사용 */ }
        )
    }

    private fun initButtons() {
        binding.btnBack.setOnClickListener { finish() }
        binding.sendButton.setOnClickListener {
            val txt = binding.messageInput.text.toString().trim()
            if (txt.isNotEmpty()) {
                appendLog("나: $txt")
                sendMessageToAi(txt)
                binding.messageInput.text?.clear()
            }
        }

        binding.btnMic.setOnClickListener {
            if (!::sttManager.isInitialized) {
                Toast.makeText(this, "잠시만 기다려주세요…", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                sttManager.startListening()
            }
        }

        binding.btnEndCall.setOnClickListener {
            ttsManager.destroy()
            binding.chronometer.stop()
            showCallSummaryDialog()
        }
    }

    private fun showCallSummaryDialog() {
        val elapsed  = SystemClock.elapsedRealtime() - binding.chronometer.base
        val hours    = (elapsed / 3_600_000).toInt()
        val minutes  = ((elapsed % 3_600_000) / 60_000).toInt()
        val seconds  = ((elapsed % 60_000) / 1_000).toInt()
        val durationText = buildString {
            if (hours > 0) append("${hours}시간 ")
            append(String.format("%02d분 %02d초", minutes, seconds))
        }

        val fullLog = binding.conversationLog.text.toString()
        val prompt = "다음은 사용자와 AI의 대화입니다. 요약해주세요:\n\n$fullLog"

        CoroutineScope(Dispatchers.Main).launch {
            val summary = try {
                aiService.generateContent(prompt)
            } catch (e: Exception) {
                "요약 중 오류 발생"
            }

            val dialogView = layoutInflater.inflate(
                R.layout.dialog_call_summary, null
            )
            dialogView.findViewById<TextView>(R.id.tvCallDuration).text = "통화 시간: $durationText"
            dialogView.findViewById<TextView>(R.id.tvCallSummary).text  = summary

            AlertDialog.Builder(this@ConversationActivity_Call)
                .setView(dialogView)
                .setPositiveButton("확인") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }
    }

    private fun sendMessageToAi(prompt: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val response = try {
                aiService.generateContent(prompt)
            } catch (e: Exception) {
                Log.e("ConversationActivity_Call", "AI generate error", e)
                "죄송해요, 응답을 가져오지 못했어요."
            }

            appendLog("AI: $response")

            // TTS는 IO 스레드에서 실행하며 예외 로깅
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    ttsManager.speak(response)
                } catch (e: Exception) {
                    Log.e("ConversationActivity_Call", "TTS speak error", e)
                }
            }
        }
    }

    private fun appendLog(line: String) {
        binding.conversationLog.append("$line\n\n")
        binding.scrollView.post { binding.scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.chronometer.stop()
        if (::sttManager.isInitialized) sttManager.destroy()
        ttsManager.destroy()
    }
}
