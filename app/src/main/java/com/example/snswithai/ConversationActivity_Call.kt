package com.example.snswithai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.snswithai.databinding.ActivityConversationCallBinding
import com.example.snswithai.stt.STTManager
import com.example.snswithai.tts.TtsManager
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

    // 오디오 권한 요청
    private val requestAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) sttManager.startListening()
            else Log.e("ConversationActivity", "Audio permission denied")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversationCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Firebase에서 캐릭터 정보 로드
        Firebase.database
            .getReference("characters/char101")
            .get()
            .addOnSuccessListener { snap ->
                currentChar = snap.getValue(Character::class.java) ?: Character()
                initUi()
                initAiService()
                initTts()
                initStt()
                initButtons()
            }
            .addOnFailureListener { e ->
                Log.e("ConversationActivity", "char101 load failed", e)
                currentChar = Character()
                initUi()
                initAiService()
                initTts()
                initStt()
                initButtons()
            }
    }

    private fun initUi() {
        binding.imagePath         = currentChar.imageURL
        binding.charName          = currentChar.name
        binding.charAge           = currentChar.age
        binding.charHobby         = currentChar.hobby
        binding.charDescription   = currentChar.description
        binding.charPersonality   = currentChar.personality
    }

    private fun initAiService() {
        val systemPrompt = "당신은 ${currentChar.name} (나이 ${currentChar.age}, 취미 ${currentChar.hobby}) 입니다. " +
                "${currentChar.description}. 성격 특성: ${currentChar.personality}."
        aiService = AIService(
            systemInstruction = content { text(systemPrompt) }
        )
    }


    private fun initTts() {
        // voiceID를 넘겨 주고, speak(text) 호출만 하면 됩니다
        ttsManager = TtsManager(
            context = this,
            apiKey   = BuildConfig.ELEVEN_LABS_API_KEY,
            voiceId  = currentChar.voiceID
        )
    }

    private fun initStt() {
        sttManager = STTManager(
            context  = this,
            onResult = { text ->
                runOnUiThread {
                    appendLog("나(음성): $text")
                    sendMessageToAi(text)
                }
            },
            onRms    = { /* 필요없으면 빈 람다 */ }
        )
    }

    private fun initButtons() {
        // 텍스트 전송
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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                sttManager.startListening()
            }
        }
    }

    private fun sendMessageToAi(prompt: String) {
        // 1) AI 호출
        CoroutineScope(Dispatchers.Main).launch {
            val response = aiService.generateContent(prompt)

            // 2) UI에 표시
            appendLog("AI: $response")

            // 3) TTS 재생
            ttsManager.speak(response)
        }
    }

    private fun appendLog(line: String) {
        binding.conversationLog.append("$line\n\n")
        binding.scrollView.post { binding.scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    override fun onDestroy() {
        super.onDestroy()
        sttManager.destroy()
        ttsManager.destroy()
    }
}