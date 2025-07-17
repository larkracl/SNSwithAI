package com.example.snswithai

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.snswithai.data.model.TimelinePost
import com.example.snswithai.data.model.TimelinePostComment
import com.example.snswithai.databinding.ActivityPostDetailBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private var postId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = intent.getStringExtra("POST_ID")

        if (postId == null) {
            // postId가 없으면 액티비티 종료
            finish()
            return
        }

        loadPostData()
        loadCommentsFromDB()
    }

    private fun loadPostData() {
        val postRef = FirebaseDatabase.getInstance().getReference("user_data")
            .child("ZCtJmhJKr7RklOwkOjJa2OvunbA3").child("timeline").child(postId!!)

        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(TimelinePost::class.java)
                post?.let {
                    binding.tvPostAuthor.text = it.author_name
                    binding.tvPostContent.text = it.content
                    binding.tvPostDate.text = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(
                        Date(it.created_at)
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 데이터 로드 실패 처리
            }
        })
    }

    private fun loadCommentsFromDB() {
        val commentsRef = FirebaseDatabase.getInstance().getReference("timeline_comments").child(postId!!)
        commentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val comments = mutableMapOf<String, TimelinePostComment>()
                for (commentSnapshot in snapshot.children) {
                    val comment = commentSnapshot.getValue(TimelinePostComment::class.java)
                    comment?.let {
                        comments[commentSnapshot.key!!] = it
                    }
                }
                if (comments.isNotEmpty()) {
                    loadComments(comments)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 데이터 로드 실패 처리
            }
        })
    }

    private fun loadComments(comments: Map<String, TimelinePostComment>) {
        val inflater = LayoutInflater.from(this@PostDetailActivity)
        binding.commentsContainer.removeAllViews() // 기존 댓글 제거

        val sortedComments = comments.values.sortedBy { it.createdAt }

        for (comment in sortedComments) {
            try {
                // 생성된 댓글을 UI에 추가
                val commentView = inflater.inflate(R.layout.item_comment, binding.commentsContainer, false)
                val authorTextView = commentView.findViewById<TextView>(R.id.tvCommentAuthor)
                val commentTextView = commentView.findViewById<TextView>(R.id.tvComment)

                authorTextView.text = comment.authorName
                commentTextView.text = comment.text
                binding.commentsContainer.addView(commentView)

            } catch (e: Exception) {
                // 오류 처리
            }
        }
    }
}
