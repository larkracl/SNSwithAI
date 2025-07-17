package com.example.snswithai

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.snswithai.databinding.ActivityChatroomBinding
import com.google.firebase.database.*
import kotlinx.coroutines.launch

class ConversationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatroomBinding
    private lateinit var conversationManager: ConversationManager

    // 캐릭터 이미지 리소스 ID
    private var characterResId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewBinding 초기화
        binding = ActivityChatroomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // CHARACTER_KEY 수신
        val charKey = intent.getStringExtra("CHARACTER_KEY")
            ?: run {
                Toast.makeText(this, "캐릭터 정보를 받지 못했습니다.", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

        // Firebase 에서 캐릭터 정보(name, description, hobby, imageURL) 로딩
        FirebaseDatabase.getInstance()
            .reference
            .child("characters")
            .child(charKey)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name        = snapshot.child("name").getValue(String::class.java)       ?: "AI"
                    val description = snapshot.child("description").getValue(String::class.java)?: ""
                    val hobby       = snapshot.child("hobby").getValue(String::class.java)     ?: ""
                    val imageUrl    = snapshot.child("imageURL").getValue(String::class.java)  ?: "@drawable/ic_profile_placeholder"

                    // ① "@drawable/chatbot_101" → "chatbot_101"
                    val resName = imageUrl.substringAfter("/")
                    // ② 리소스 ID 계산
                    characterResId = resources.getIdentifier(resName, "drawable", packageName)
                        .takeIf { it != 0 }
                        ?: R.drawable.ic_profile_placeholder


                    // 시스템 프롬프트 조합
                    val systemPrompt = "당신은 $name 입니다. $description 취미: $hobby."

                    // ConversationManager 초기화
                    conversationManager = ConversationManager(systemPrompt)

                    // 대화 흐름 시작
                    setupConversationFlow(name)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ConversationActivity,
                        "캐릭터 정보를 불러오지 못했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            })
    }

    private fun setupConversationFlow(characterName: String) {
        // 액션바 제목
        title = "$characterName 와의 대화"

        // 초기 메시지는 표시하지 않음
        lifecycleScope.launch {
            conversationManager.startConversation(
                initialPrompt = "안녕, 나는 너와 대화하고 싶어.",
                onMessage = { /* skip user initial */ },
                onJsonMessage = { /*…*/ }
            )
        }

        // 보내기 버튼 리스너
        binding.sendButton.setOnClickListener {
            val msg = binding.messageInput.text.toString().trim().takeIf { it.isNotEmpty() } ?: return@setOnClickListener

            // Outgoing 추가
            addMessage(msg, incoming = false)
            binding.messageInput.text?.clear()

            // AI 응답 요청
            lifecycleScope.launch {
                conversationManager.sendMessage(
                    prompt = msg,
                    onMessage = { raw ->
                        // 접두어 제거
                        val text = raw.substringAfter(":").trimStart()
                        runOnUiThread { addMessage(text, incoming = true) }
                    },
                    onJsonMessage = { /*…*/ }
                )
            }
        }
    }

    /**
     * 메시지 뷰를 inflate 해서 chat_container에 추가
     * incoming=true → item_incoming_message.xml, false → item_outgoing_message.xml
     */
    private fun addMessage(text: String, incoming: Boolean) {
        val layoutId = if (incoming)
            R.layout.item_incoming_message
        else
            R.layout.item_outgoing_message

        val itemView = LayoutInflater.from(this)
            .inflate(layoutId, binding.chatContainer, false)

        // 메시지 세팅
        itemView.findViewById<TextView>(R.id.tv_message).text = text

        if (incoming) {
            // 인커밍 아바타 세팅
            itemView.findViewById<ImageView>(R.id.iv_avatar)
                .setImageResource(characterResId)
        }

        // 컨테이너에 추가 & 스크롤 아래로
        binding.chatContainer.addView(itemView)
        binding.chatScrollView.post { binding.chatScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
