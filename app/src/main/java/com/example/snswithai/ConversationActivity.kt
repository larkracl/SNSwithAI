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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class ConversationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatroomBinding
    private lateinit var conversationManager: ConversationManager

    private lateinit var roomKey: String
    private lateinit var charKey: String

    // 캐릭터 이미지 리소스 ID
    private var characterResId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1) ViewBinding 초기화
        binding = ActivityChatroomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2) Intent에서 CHARACTER_KEY와 ROOM_KEY 수신
        charKey = intent.getStringExtra("CHARACTER_KEY") ?: run {
            Toast.makeText(this, "캐릭터 정보를 받지 못했습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        roomKey = intent.getStringExtra("ROOM_KEY") ?: run {
            Toast.makeText(this, "채팅방 정보를 받지 못했습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 3) Firebase에서 캐릭터 정보(name, description, hobby, imageURL) 로딩
        FirebaseDatabase.getInstance()
            .reference
            .child("characters")
            .child(charKey)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // 캐릭터 메타 데이터 파싱
                    val name        = snapshot.child("name").getValue(String::class.java)       ?: "AI"
                    val description = snapshot.child("description").getValue(String::class.java)?: ""
                    val hobby       = snapshot.child("hobby").getValue(String::class.java)     ?: ""
                    val imageUrl    = snapshot.child("imageURL").getValue(String::class.java)  ?: "@drawable/ic_profile_placeholder"

                    // 4) 이미지 리소스 ID 계산
                    val resName = imageUrl.substringAfter("/")
                    characterResId = resources.getIdentifier(resName, "drawable", packageName)
                        .takeIf { it != 0 }
                        ?: R.drawable.ic_profile_placeholder

                    // 5) 과거 메시지 로드 (characterResId 준비된 상태)
                    loadChatHistory()

                    // 6) 시스템 프롬프트 조합 및 ConversationManager 초기화
                    val systemPrompt = "당신은 $name 입니다. $description 취미: $hobby."
                    conversationManager = ConversationManager(systemPrompt)

                    // 7) 대화 흐름 시작
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
        title = "$characterName 와의 대화"

        // 초기 메시지는 화면에 표시하지 않음
        lifecycleScope.launch {
            conversationManager.startConversation(
                initialPrompt = "안녕, 나는 너와 대화하고 싶어.",
                onMessage = { /* skip user initial */ },
                onJsonMessage = { /*…*/ }
            )
        }

        binding.sendButton.setOnClickListener {
            val msg = binding.messageInput.text.toString().trim().takeIf { it.isNotEmpty() }
                ?: return@setOnClickListener

            // 1) DB에 내 메시지 저장
            val currentUserId = FirebaseAuth.getInstance().uid ?: "unknown_user"
            FirebaseDatabase.getInstance()
                .reference
                .child("chat_messages")
                .child(roomKey)
                .push()
                .setValue(mapOf(
                    "message"   to msg,
                    "sender_id" to currentUserId,
                    "sent_at"   to ServerValue.TIMESTAMP
                ))

            // 2) 화면에 내 메시지 표시
            addMessage(msg, incoming = false)
            binding.messageInput.text?.clear()

            // 3) AI에게 요청 → 응답 받으면 DB 저장 및 화면 표시
            lifecycleScope.launch {
                conversationManager.sendMessage(
                    prompt = msg,
                    onMessage = { raw ->
                        val text = raw.substringAfter(":").trimStart()

                        // 3-a) DB에 AI 응답 저장
                        FirebaseDatabase.getInstance()
                            .reference
                            .child("chat_messages")
                            .child(roomKey)
                            .push()
                            .setValue(mapOf(
                                "message"   to text,
                                "sender_id" to charKey,
                                "sent_at"   to ServerValue.TIMESTAMP
                            ))

                        // 3-b) 화면에 AI 메시지 표시
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
    private fun loadChatHistory() {
        val messagesRef = FirebaseDatabase.getInstance()
            .reference
            .child("chat_messages")
            .child(roomKey)

        // 한 번만 읽어오기
        messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { child ->
                    val text     = child.child("message").getValue(String::class.java) ?: return@forEach
                    val senderId = child.child("sender_id").getValue(String::class.java) ?: ""
                    val incoming = senderId.startsWith("char")
                    addMessage(text, incoming)
                }
                // 화면을 맨 아래로 스크롤
                binding.chatScrollView.post {
                    binding.chatScrollView.fullScroll(ScrollView.FOCUS_DOWN)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ConversationActivity, "메시지 로드 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
