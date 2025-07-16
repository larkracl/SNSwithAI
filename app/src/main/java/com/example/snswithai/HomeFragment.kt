package com.example.snswithai

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snswithai.data.model.TimelinePost
import com.example.snswithai.data.repository.TimelineCommentRepository
import com.example.snswithai.data.repository.TimelinePostRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter   // 예시 코드로 입력한 임시 변수이니 사용할 어댑터명으로 변경해주세요
    private val postList = mutableListOf<TimelinePost>()

    private val db = FirebaseDatabase.getInstance()
    private val postRepository = TimelinePostRepository(db)
    private val commentRepository = TimelineCommentRepository(db)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        postAdapter = PostAdapter(
            postList,
            // 게시글 클릭 시 삭제 예시
            onDelete = { post ->       // post: 사용자가 클릭한 게시글 객체
                lifecycleScope.launch {
                    postRepository.deleteTimelinePost(post.postId)
                }
            },
            // 게시글 클릭 시 수정 예시
            onEdit = { post ->
                showEditDialog(post)        // 수정 화면 불러오기
            },
            onLikeClick = { post -> updateLike(post) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = postAdapter

        observePosts()

        // 포스트 등록
        (등록버튼변수명).setOnClickListener {
                val content = (포스트작성edittext변수명).text.toString()
                if (content.isNotBlank()) {
                    lifecycleScope.launch {
                        val postId = db.getReference("timelinePosts").push().key ?: return@launch
                        val newPost = TimelinePost(
                            authorId = "(유저id)",
                            authorName = "(유저Name)",
                            content = content,
                            createdAt = System.currentTimeMillis(),
                            likeCount = 0
                        )

                        postRepository.createTimelinePost(newPost)
                        (포스트작성edittext변수명).text.clear()     // 등록 후 포스트 edittext 초기화
                    }
                }
            }

        // 포스트 수정

        return view
    }

    private fun observePosts() {
        val postsRef = db.getReference("timelinePosts")
        postsRef.orderByChild("created_at").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // 포스트 리스트 초기화
                postList.clear()

                // 포스트 리스트에 삽입
                for (child in snapshot.children) {
                    val post = child.getValue(TimelinePost::class.java)
                    post?.let { postList.add(it) }
                }

                postList.reverse()      // 최신순 정렬
                postAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "타임라인 불러오기 실패: ${error.message}")
            }
        })
    }

    // 하나의 예시로 별도의 수정 창을 발견하지 못해 작성, 필요 없다면 지우셔도 괜찮습니다.
    private fun showEditDialog(post: TimelinePost) {
        val editText = EditText(requireContext()).apply {
            setText(post.content)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("게시글 수정")
            .setView(editText)
            .setPositiveButton("수정") { _, _ ->
                val newContent = editText.text.toString()
                if (newContent.isNotBlank()) {
                    lifecycleScope.launch {
                        val updatedPost = post.copy(
                            content = newContent,
                            createdAt = System.currentTimeMillis()
                        )
                        postRepository.updateTimelinePost(updatedPost)
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    suspend fun toggleLike(postId: String, userId: String): Boolean{
        val postRef = db.getReference("timelinePosts").child(postId)
        val snapshot = postRef.get().await()
        val post = snapshot.getValue(TimelinePost::class.java)

        if (post != null) {
            val likedBy = post.likedBy?.toMutableMap() ?: mutableMapOf()
            val alreadyLiked = likedBy.containsKey(userId)

            if (alreadyLiked) {
                likedBy.remove(userId)
                postRepository.decrementLikeCount(postId)
            } else {
                likedBy[userId] = true
                postRepository.incrementLikeCount(postId)
            }

            postRef.child("likedBy").setValue(likedBy).await()

            return !alreadyLiked
        }

        return false
    }

    private fun updateLike(post: TimelinePost) {
        val userId = "(유저id)"

        lifecycleScope.launch {
            val isLiked = toggleLike(post.postId, userId)
        }
    }
}
