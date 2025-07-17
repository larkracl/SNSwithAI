package com.example.snswithai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.snswithai.R
import com.example.snswithai.data.model.TimelinePostComment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentAdapter(
    private val comments: List<TimelinePostComment>,
    private val onReplyClick: (TimelinePostComment) -> Unit = {}
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.img_comment_profile)
        val tvUsername: TextView = itemView.findViewById(R.id.tv_comment_username)
        val tvTime: TextView = itemView.findViewById(R.id.tv_comment_time)
        val tvContent: TextView = itemView.findViewById(R.id.tv_comment_content)
        val btnReply: TextView = itemView.findViewById(R.id.btn_reply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        holder.tvUsername.text = comment.authorName
        holder.tvContent.text = comment.text

        val formattedTime = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
            .format(Date(comment.createdAt))
        holder.tvTime.text = formattedTime

        holder.imgProfile.setImageResource(R.drawable.ic_profile_placeholder)

        holder.btnReply.setOnClickListener {
            onReplyClick(comment)
        }
    }

    override fun getItemCount(): Int = comments.size
}
