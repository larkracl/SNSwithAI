package com.example.snswithai

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    private lateinit var recyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<TimelinePostComment>()
    private var postId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail) // 새 XML 파일 이름과 동일해야 함

        // 바인딩
        val tvUsername = findViewById<TextView>(R.id.tv_username)
        val tvLocation = findViewById<TextView>(R.id.tv_location)
        val tvPostContent = findViewById<TextView>(R.id.tv_post_content)
        recyclerView = findViewById(R.id.recyclerViewComments)

        commentAdapter = CommentAdapter(commentList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = commentAdapter

        postId = intent.getStringExtra("POST_ID")
        if (postId == null) {
            finish()
            return
        }

        loadPostData(tvUsername, tvLocation, tvPostContent)
        loadCommentsFromDB()
    }

    private fun loadPostData(tvUsername: TextView, tvLocation: TextView, tvPostContent: TextView) {
        val postRef = FirebaseDatabase.getInstance().getReference("user_data")
            .child("ZCtJmhJKr7RklOwkOjJa2OvunbA3")
            .child("timeline").child(postId!!)

        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue(TimelinePost::class.java)
                post?.let {
                    tvUsername.text = it.author_name
                    tvPostContent.text = it.content
                    tvLocation.text = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(Date(it.created_at))
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadCommentsFromDB() {
        val commentsRef = FirebaseDatabase.getInstance().getReference("timeline_comments").child(postId!!)
        commentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentList.clear()
                for (commentSnapshot in snapshot.children) {
                    val comment = commentSnapshot.getValue(TimelinePostComment::class.java)
                    comment?.let { commentList.add(it) }
                }
                commentList.sortBy { it.createdAt }
                commentAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}

