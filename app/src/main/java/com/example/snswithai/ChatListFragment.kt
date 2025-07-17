package com.example.snswithai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

        // TODO: 실제 로그인된 userId로 대체하세요
        val currentUserId = "ZCtJmhJKr7RklOwkOjJa2OvunbA3"

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
                            val roomKey = roomKeys[position]
                            holder.tvName.text = roomNames[position]

                            // 기본 플레이스홀더 세팅
                            holder.ivAvatar.setImageResource(R.drawable.ic_profile_placeholder)

                            // 4) 방 멤버 중 첫 번째 char 키 찾기
                            val membersNode = snapshot
                                .child("chat_room_members")
                                .child(roomKey)
                            val characterKey = membersNode.children
                                .mapNotNull { it.key }
                                .firstOrNull { it.startsWith("char") }
                                .orEmpty()

                            // 5) 캐릭터 정보에서 로컬 drawable 경로(imageURL) 가져와 세팅
                            if (characterKey.isNotEmpty()) {
                                FirebaseDatabase.getInstance()
                                    .reference
                                    .child("characters")
                                    .child(characterKey)
                                    .child("imageURL")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(ds: DataSnapshot) {
                                            val path = ds.getValue(String::class.java)
                                            if (!path.isNullOrEmpty()) {
                                                // "@drawable/your_image_name"에서 "your_image_name" 추출
                                                val resName = path.substringAfter("/")

                                                // 리소스 ID 얻기
                                                val resId = holder.ivAvatar.context.resources
                                                    .getIdentifier(resName, "drawable", holder.ivAvatar.context.packageName)

                                                if (resId != 0) {
                                                    holder.ivAvatar.setImageResource(resId)
                                                }
                                            }
                                        }
                                        override fun onCancelled(error: DatabaseError) {
                                            // 로드 실패 시 기본 이미지 유지
                                        }
                                    })
                            }

                            // 6) 클릭 시 ConversationActivity로 이동
                            holder.itemView.setOnClickListener {
                                Intent(requireContext(), ConversationActivity::class.java).also { intent ->
                                    intent.putExtra("ROOM_KEY", roomKey)
                                    intent.putExtra("CHARACTER_KEY", characterKey)
                                    startActivity(intent)
                                }
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
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        val tvName:  TextView   = itemView.findViewById(R.id.tv_name)
    }
}
