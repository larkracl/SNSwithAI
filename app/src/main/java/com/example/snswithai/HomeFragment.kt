package com.example.snswithai

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.snswithai.data.model.TimelineComment
import com.example.snswithai.data.model.TimelinePost
import com.example.snswithai.data.repository.TimelineCommentRepository
import com.example.snswithai.data.repository.TimelinePostRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private val db = FirebaseDatabase.getInstance()
    private val postRepository = TimelinePostRepository(db)
    private val commentRepository = TimelineCommentRepository(db)


    // 각 기능은 데이터 모델 및 레포지토리에 맞춰 진행했으나 불필요할 경우 없애주세요

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // 포스트 실시간 불러오기

        val postsRef = FirebaseDatabase.getInstance().getReference("TimelinePost")
        val postList = mutableListOf<TimelinePost>()
        val adapter = PostAdapter(postList)     // PostAdapter는 리사이클러뷰 어댑터 이름으로 바꿔주세요

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewPosts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        postsRef.orderByChild("timestamp").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // 타임라인 초기화
                postList.clear()

                // 포스트를 리스트로 불러오기
                for(postSnap in snapshot.children) {
                    val post = postSnap.getValue(TimelinePost::class.java)
                    post?.let { postList.add(it) }
                }

                postList.reverse()  // 최신순 정렬
                adapter.notifyDataSetChanged()      // 타임라인 리로드
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebse", "타임라인 불러오기 실패: ${error.message}")
            }
        })

        // 포스트 좋아요 클릭 시 사용
        // 포스트 좋아요 등록
        postRepository.incrementLikeCount(postId)
        //포스트 좋아요 해제
        postRepository.decrementLikeCount(postId)

        // 댓글 좋아요 클릭 시 사용
        // 댓글 좋아요 등록
        commentRepository.incrementLikeCount(commentId)
        // 댓글 좋아요 해제
        commentRepository.decrementLikeCount(commentId)

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    // 포스트 등록
    suspend fun postToTimeline(authorId: String, authorName: String, content: String) {
        val post = TimelinePost(authorId, authorName, content, System.currentTimeMillis(), 0, emptyMap())
        postRepository.createTimelinePost(post)
    }

    // 포스트 수정
    suspend fun updatePost(authorId: String,authorName: String, content: String, likeCount: Int) {
        val post = TimelinePost(authorId, authorName, content, System.currentTimeMillis(), likeCount)
        postRepository.updateTimelinePost(post)
    }

    // 포스트 삭제
    suspend fun deletePost(postId : String) {
        postRepository.deleteTimelinePost(postId)
    }

    // 댓글 등록
    suspend fun commentToPost(authorId: String, authorName: String, content: String, parentCommentId: String, likeCount: Int, postId: String) {
        val comment = TimelineComment(authorId, authorName, content, parentCommentId, System.currentTimeMillis(), likeCount)
        commentRepository.createTimelineComment(comment)
        postRepository.incrementCommentCount(postId)
    }

    // 댓글 수정
    suspend fun updateComment(authorId: String, authorName: String, content: String, parentCommentId: String, likeCount: Int) {
        val comment = TimelineComment(authorId, authorName, content, parentCommentId, System.currentTimeMillis(), likeCount)
        commentRepository.updateTimelineComment(comment)
    }

    // 댓글 삭제
    suspend fun deleteComment (CommentId: String, postId: String) {
        commentRepository.deleteTimelineComment(CommentId)
        postRepository.decrementCommentCount(postId)
    }

    /* // 이하 좋아요 관련 기능은 좋아요 테이블(likes)을 별도로 만들어 사용할 경우 해제

    // 좋아요를 등록 및 해제 함수(포스트 번호와 유저 번호로 등록)
    fun toggleLike(id: Int, authorId: String) {
        val likeRef = FirebaseDatabase.getInstance().getReference("likes").child(id.toString()).child(authorId)

        likeRef.get().addOnSuccessListener { snapshot ->

            if (snapshot.exists()) {        // 이미 좋아요가 등록됐다면 해제
                likeRef.removeValue()
            } else {                        // 좋아요 등록
                likeRef.setValue(true)
            }
        }
    }

    // 좋아요의 갯수를 얻어오는 함수
    fun getLikeCount(id: Int, callback: (Int) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("likes").child(id.toString())
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount.toInt()
                callback(count)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(0)
            }
        })
    }*/
}
