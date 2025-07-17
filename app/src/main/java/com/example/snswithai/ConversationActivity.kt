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
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content

class ConversationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatroomBinding
    private lateinit var conversationManager: ConversationManager

    private lateinit var roomKey: String
    private lateinit var charKey: String

    // 캐릭터 이미지 리소스 ID
    private var characterResId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatroomBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        FirebaseDatabase.getInstance()
            .reference
            .child("characters")
            .child(charKey)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: "AI"
                    val description = snapshot.child("description").getValue(String::class.java) ?: ""
                    val hobby = snapshot.child("hobby").getValue(String::class.java) ?: ""
                    val imageUrl = snapshot.child("imageURL").getValue(String::class.java) ?: "@drawable/ic_profile_placeholder"

                    val resName = imageUrl.substringAfter("/")
                    characterResId = resources.getIdentifier(resName, "drawable", packageName)
                        .takeIf { it != 0 }
                        ?: R.drawable.ic_profile_placeholder

                    // 시스템 프롬프트 조합
                    val systemPrompt = "당신은 $name 입니다. $description 취미: $hobby."

                    // 과거 메시지 로드 및 ConversationManager 초기화
                    loadChatHistoryAndInitManager(systemPrompt, name)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ConversationActivity, "캐릭터 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
    }

    private fun setupConversationFlow(characterName: String) {
        title = "$characterName 와의 대화"

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

            addMessage(msg, incoming = false)
            binding.messageInput.text?.clear()

            lifecycleScope.launch {
                conversationManager.sendMessage(
                    prompt = msg,
                    onMessage = { raw ->
                        val text = raw.substringAfter(":").trimStart()
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
                        runOnUiThread { addMessage(text, incoming = true) }
                    },
                    onJsonMessage = { /*…*/ }
                )
            }
        }
    }

    private fun addMessage(text: String, incoming: Boolean) {
        val layoutId = if (incoming) R.layout.item_incoming_message else R.layout.item_outgoing_message
        val itemView = LayoutInflater.from(this).inflate(layoutId, binding.chatContainer, false)
        itemView.findViewById<TextView>(R.id.tv_message).text = text
        if (incoming) {
            itemView.findViewById<ImageView>(R.id.iv_avatar).setImageResource(characterResId)
        }
        binding.chatContainer.addView(itemView)
        binding.chatScrollView.post { binding.chatScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    /**
     * Firebase에서 가져온 DataSnapshot을 `List<Content>`로 변환합니다.
     */
    private fun createChatHistoryContents(snapshot: DataSnapshot): List<Content> {
        val history = mutableListOf<Content>()
        val currentUserId = FirebaseAuth.getInstance().uid ?: "unknown_user"

        snapshot.children.forEach { child ->
            val message = child.child("message").getValue(String::class.java)
            val senderId = child.child("sender_id").getValue(String::class.java)

            if (message != null && senderId != null) {
                // senderId를 기반으로 'user' 또는 'model' 역할을 할당
                val role = if (senderId == currentUserId) "user" else "model"
                history.add(content(role) { text(message) })
            }
        }
        return history
    }

    /**
     * 채팅 기록을 불러와 UI에 표시하고, ConversationManager를 초기화합니다.
     */
    private fun loadChatHistoryAndInitManager(systemPrompt: String, characterName: String) {
        val messagesRef = FirebaseDatabase.getInstance()
            .reference
            .child("chat_messages")
            .child(roomKey)

        messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 1. DB 데이터 -> List<Content> 가공
                val history = createChatHistoryContents(snapshot)

                // 2. ConversationManager 초기화
                conversationManager = ConversationManager(systemPrompt, history)

                // 3. 대화 흐름 시작
                setupConversationFlow(characterName)

                // 4. 화면에 과거 메시지 표시
                val currentUserId = FirebaseAuth.getInstance().uid ?: "unknown_user"
                snapshot.children.forEach { child ->
                    val text = child.child("message").getValue(String::class.java) ?: return@forEach
                    val senderId = child.child("sender_id").getValue(String::class.java) ?: ""
                    val incoming = senderId != currentUserId
                    addMessage(text, incoming)
                }
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
