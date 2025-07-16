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
import com.example.snswithai.data.model.TimelineComment
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
    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<com.example.snswithai.data.local.db.entity.TimelineComment>()


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
            onLikeClick = { post -> updatePostLike(post) }
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


        /* 여기부터 하단은 디테일 화면 */

        loadDetailPost(postId)  // 해당 포스트 출력
        loadCommentsForPost(postId) // 댓글 리스트 출력

        commentAdapter = CommentAdapter(
            commentList,
            onEdit = {  comment ->
                showEditCommentDialog(comment)
            },
            onDelete = {    comment ->
                lifecycleScope.launch {
                    commentRepository.deleteTimelineComment(comment.commentId)
                    postRepository.decrementCommentCount(postId)
                }
            },
            onLikeClick = { comment -> updateCommentLike(comment) }
        )

        // 댓글 등록
        (등록버튼변수명).setOnClickListener {
            val content = (댓글작성edittext변수명).text.toString()
            if (content.isNotBlank()) {
                lifecycleScope.launch {
                    val newComment = TimelineComment(
                        authorId = "(유저id)",
                        authorName = "(유저Name)",
                        content = content,
                        createdAt = System.currentTimeMillis(),
                        parentCommentId = null,
                        likeCount = 0
                    )

                    commentRepository.createTimelineComment(newComment)
                    postRepository.incrementCommentCount(postId)
                    loadCommentsForPost(postId)     // 등록 후 댓글 리로드
                }
            }
        }

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

    // 기존에 좋아요 된 글인지 확인 후 DB 및 화면 반영
    suspend fun togglePostLike(postId: String, userId: String): Boolean{
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

    private fun updatePostLike(post: TimelinePost) {
        val userId = "(유저id)"

        lifecycleScope.launch {
            val isLiked = togglePostLike(post.postId, userId)
        }
    }


    /* 하단부터 디테일 화면 */
    // 선택한 포스트 출력
    private fun loadDetailPost(postId: String) {
        lifecycleScope.launch {
            val post = postRepository.getTimelinePost(postId)
            if (post != null) {
                Log.d("DetailPost", "조회 성공: ${post.content}")
                // 받아온 post의 내용을 받아 화면에 배치
            } else {
                Log.e("DetailPost", "해당 postId의 게시물이 없습니다.")
            }
        }
    }

    // 전체 댓글 보기
    private fun loadCommentsForPost(postId: String) {
        lifecycleScope.launch {
            val comments = commentRepository.getCommentsForPost(postId)
            commentList.clear()
            commentList.addAll(comments.sortedByDescending { it.createdAt })
            commentAdapter.notifyDataSetChanged()
        }
    }

    private fun showEditCommentDialog(comment: TimelineComment) {
        val input = EditText(requireContext()).apply {
            setText(comment.content)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("댓글 수정")
            .setView(input)
            .setPositiveButton("수정") { _, _ ->
                val newContent = input.text.toString()
                if (newContent.isNotBlank()) {
                    lifecycleScope.launch {
                        val updatedComment = comment.copy(content = newContent)
                        commentRepository.updateTimelineComment(updatedComment)
                        loadCommentsForPost(comment.parentCommentId ?: "") // 해당 포스트의 댓글 다시 불러오기
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 기존에 좋아요 된 댓글인지 확인 후 DB 및 화면 반영
    suspend fun toggleCommentLike(commentId: String, userId: String): Boolean{
        val commentRef = db.getReference("timelineComments").child(commentId)
        val snapshot = commentRef.get().await()
        val comment = snapshot.getValue(TimelineComment::class.java)

        if (comment != null) {
            val likedBy = comment.likedBy?.toMutableMap() ?: mutableMapOf()
            val alreadyLiked = likedBy.containsKey(userId)

            if (alreadyLiked) {
                likedBy.remove(userId)
                commentRepository.decrementLikeCount(postId)
            } else {
                likedBy[userId] = true
                commentRepository.incrementLikeCount(postId)
            }

            commentRef.child("likedBy").setValue(likedBy).await()

            return !alreadyLiked
        }

        return false
    }

    private fun updateCommentLike(comment: TimelineComment) {
        val userId = "(유저id)"

        lifecycleScope.launch {
            val isLiked = toggleCommentLike(comment.commentId, userId)
        }
    }

}
