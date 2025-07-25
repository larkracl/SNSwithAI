package com.example.snswithai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snswithai.data.model.TimelinePost
import com.example.snswithai.data.repository.TimelinePostRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await



class HomeFragment : Fragment() {

    private var currentUid: String = "" // UID를 저장할 변수
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<TimelinePost>()

    private val db = FirebaseDatabase.getInstance()
    private val postRepository = TimelinePostRepository(db)

    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val ARG_UID = "user_uid" // Bundle 키 정의

        fun newInstance(uid: String?): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            args.putString(ARG_UID, uid) // Bundle에 UID 저장
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentUid = it.getString(ARG_UID).toString() // Bundle에서 UID 읽기
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewPosts)

        postAdapter = PostAdapter(
            postList,
            onDelete = { post ->
                lifecycleScope.launch {
                    postRepository.deleteTimelinePost(post.postId)
                }
            },
            onEdit = { post ->
                showEditDialog(post)
            },
            onLikeClick = { post -> updatePostLike(post) },
            onPostClick = { post ->
                val intent = Intent(requireContext(), PostDetailActivity::class.java).apply {
                    putExtra("POST_ID", post.postId)
                }
                startActivity(intent)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = postAdapter

        observePosts()

        val fabAddPost = view.findViewById<View>(R.id.fab_add_post)
        fabAddPost.setOnClickListener {
            val intent = Intent(requireContext(), PostCreateActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun observePosts() {
        val postsRef = db.getReference("user_data").child("ZCtJmhJKr7RklOwkOjJa2OvunbA3").child("timeline")
        //ZCtJmhJKr7RklOwkOjJa2OvunbA3 : 이주영 or currentUid
        Log.d("DBTEST", "Firebase Data: $postsRef")
        postsRef.orderByChild("created_at").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // 포스트 리스트 초기화
                postList.clear()

                // 포스트 리스트에 삽입
                for (child in snapshot.children) {
                    val post = child.getValue(TimelinePost::class.java)
                    post?.let {
                        // postId를 채워서 리스트에 추가
                        val postWithId = it.copy(postId = child.key!!)
                        postList.add(postWithId)
                    }
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
                            created_at = System.currentTimeMillis()
                        )
                        postRepository.updateTimelinePost(updatedPost)
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 기존에 좋아요 된 글인지 확인 후 DB 및 화면 반영
    suspend fun togglePostLike(postId: String, userId: String): Boolean{
        val postRef = db.getReference("user_data").child("ZCtJmhJKr7RklOwkOjJa2OvunbA3").child("timeline").child(postId)
        val snapshot = postRef.get().await()
        val post = snapshot.getValue(TimelinePost::class.java)

        if (post != null) {
            val likedBy = post.liked_by?.toMutableMap() ?: mutableMapOf()
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

    private fun updatePostLike(post: TimelinePost) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val isLiked = togglePostLike(post.postId, userId)
        }
    }
}

