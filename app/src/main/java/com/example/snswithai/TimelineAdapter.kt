package com.example.snswithai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

// ① 타임라인용 데이터 모델
data class TimelinePost(
    val authorName: String,
    val authorImageUrl: String,
    val content: String,
    val createdAt: Long,
    val likeCount: Int,
    val commentCount: Int
)

// ② 타임라인 전용 어댑터
class TimelineAdapter(
    private var items: List<TimelinePost>
) : RecyclerView.Adapter<TimelineAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProfile: ImageView = itemView.findViewById(R.id.img_profile)
        private val tvUsername: TextView  = itemView.findViewById(R.id.tv_username)
        private val tvTime: TextView      = itemView.findViewById(R.id.tv_time)
        private val tvContent: TextView   = itemView.findViewById(R.id.tv_content)
        private val tvHeartCount: TextView   = itemView.findViewById(R.id.tv_heart_count)
        private val tvCommentCount: TextView = itemView.findViewById(R.id.tv_comment_count)

        fun bind(post: TimelinePost) {
            // (1) 프로필 이미지 – 필요하면 URL → Bitmap 로딩 추가
            // Glide 같은 라이브러리가 없으면, 생략하거나 직접 BitmapFactory로 로딩...

            tvUsername.text    = post.authorName
            tvContent.text     = post.content
            tvHeartCount.text   = post.likeCount.toString()
            tvCommentCount.text = post.commentCount.toString()

            // 시간 포맷
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            tvTime.text = sdf.format(Date(post.createdAt))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    /** 데이터 갱신 헬퍼 */
    fun setData(newItems: List<TimelinePost>) {
        items = newItems
        notifyDataSetChanged()
    }
}
