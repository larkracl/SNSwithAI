package com.example.snswithai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class ChatListFragment : Fragment(R.layout.fragment_chatlist) {

    private lateinit var rvChatRooms: RecyclerView
    private val roomKeys  = mutableListOf<String>()
    private val roomNames = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvChatRooms = view.findViewById(R.id.rv_chat_rooms)
        rvChatRooms.layoutManager = LinearLayoutManager(requireContext())

        // TODO: 실제 로그인된 userId 로 바꾸세요
        val currentUserId = "user_alice_123"

        FirebaseDatabase.getInstance()
            .reference
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // 1) 내가 속한 채팅방 키들 가져오기
                    val chatRoomsNode = snapshot
                        .child("user_data")
                        .child(currentUserId)
                        .child("chat_rooms")
                    roomKeys.clear()
                    roomKeys.addAll(chatRoomsNode.children.mapNotNull { it.key })

                    // 2) 채팅방 메타에서 이름 가져오기
                    val metaNode = snapshot.child("chat_rooms_meta")
                    roomNames.clear()
                    roomKeys.forEach { key ->
                        metaNode.child(key).child("room_name")
                            .getValue(String::class.java)
                            ?.let { roomNames.add(it) }
                    }

                    // 3) RecyclerView 어댑터 설정
                    rvChatRooms.adapter = object : RecyclerView.Adapter<RoomViewHolder>() {
                        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
                            val itemView = LayoutInflater.from(parent.context)
                                .inflate(R.layout.item_chat_room, parent, false)
                            return RoomViewHolder(itemView)
                        }

                        override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
                            holder.tvName.text = roomNames[position]

                            holder.itemView.setOnClickListener {
                                val roomKey = roomKeys[position]

                                // 4) 방 멤버 중 첫 번째 키 가져오기
                                val membersNode = snapshot
                                    .child("chat_room_members")
                                    .child(roomKey)
                                val firstKey = membersNode.children
                                    .mapNotNull { it.key }
                                    .firstOrNull()
                                    ?: return@setOnClickListener

                                // 5) 캐릭터 키 결정 (char로 시작하지 않으면 다음 찾기)
                                val characterKey = if (firstKey.startsWith("char")) {
                                    firstKey
                                } else {
                                    membersNode.children
                                        .mapNotNull { it.key }
                                        .firstOrNull { it.startsWith("char") }
                                        ?: return@setOnClickListener
                                }

                                                               // 6) ConversationActivity로 키 전달

                                                               // 6) ConversationActivity로 roomKey와 characterKey 함께 전달
                                                               val intent = Intent(requireContext(), ConversationActivity::class.java)
                                                                   .apply {
                                                                           putExtra("ROOM_KEY", roomKey)
                                                                           putExtra("CHARACTER_KEY", characterKey)
                                                                       }
                                                               startActivity(intent)
                            }
                        }

                        override fun getItemCount(): Int = roomNames.size
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("ChatListFragment", "Firebase load cancelled: ${error.message}")
                }
            })
    }

    private class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
    }
}
