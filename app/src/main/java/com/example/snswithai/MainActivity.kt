package com.example.snswithai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // "대화 시작" 버튼이 캐릭터 선택 화면으로 이동하도록 변경
        val conversationTestButton = findViewById<Button>(R.id.conversation_test_button)
        conversationTestButton.setOnClickListener {
            val intent = Intent(this, CharacterSelectionActivity::class.java)
            startActivity(intent)
        }

        // 타임라인 테스트 버튼
        val timelineTestButton = findViewById<Button>(R.id.timeline_test_button)
        timelineTestButton.setOnClickListener {
            val intent = Intent(this, TimelineActivity::class.java)
            startActivity(intent)
        }

        // 음성 테스트 버튼
        val voiceTestButton = findViewById<Button>(R.id.voice_test_button)
        voiceTestButton.setOnClickListener {
            startActivity(Intent(this, ConversationActivity_Call::class.java))
        }

    }
}