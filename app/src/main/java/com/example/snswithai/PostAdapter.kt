package com.example.snswithai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.snswithai.data.model.TimelinePost
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private val postList: List<TimelinePost>,
    private val onEdit: (TimelinePost) -> Unit,
    private val onDelete: (TimelinePost) -> Unit,
    private val onLikeClick: (TimelinePost) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(postList[position])
    }

    override fun getItemCount(): Int = postList.size

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.tv_username)
        private val timeTextView: TextView = itemView.findViewById(R.id.tv_time)
        private val contentTextView: TextView = itemView.findViewById(R.id.tv_content)
        private val heartCountTextView: TextView = itemView.findViewById(R.id.tv_heart_count)
        private val commentCountTextView: TextView = itemView.findViewById(R.id.tv_comment_count)
        private val moreButton: ImageView = itemView.findViewById(R.id.btn_more)
        private val heartIcon: ImageView = itemView.findViewById(R.id.icon_heart)

        fun bind(post: TimelinePost) {
            usernameTextView.text = post.author_name
            contentTextView.text = post.content
            heartCountTextView.text = post.like_count.toString()

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            timeTextView.text = sdf.format(Date(post.created_at))

            moreButton.setOnClickListener {
                showPopupMenu(it, post)
            }

            heartIcon.setOnClickListener {
                onLikeClick(post)
            }
        }

        private fun showPopupMenu(view: View, post: TimelinePost) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_post, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_edit -> {
                        onEdit(post)
                        true
                    }
                    R.id.menu_delete -> {
                        onDelete(post)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }
}
