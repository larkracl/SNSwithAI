package com.example.snswithai

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.snswithai.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentProfileBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) UID 가져오기 (args or auth)
        val uidFromArgs = arguments?.getString("USER_UID")
        val uidFromAuth = FirebaseAuth.getInstance().uid
        val uidToLoad = uidFromArgs ?: uidFromAuth

        if (uidToLoad.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "사용자 UID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        // 2) ViewModel 초기화 및 프로필 load
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        viewModel.loadProfile(uidToLoad)

        // 3) 프로필 데이터 관찰
        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            binding.tvUsername.text = profile.name
            binding.tvKeywords.text = profile.description
            binding.btnFollow.text = if (profile.isFollowing) "언팔로우" else "팔로우"

            // 프로필 이미지 로딩 (코루틴+BitmapFactory)
            lifecycleScope.launch(Dispatchers.IO) {
                runCatching {
                    val input = URL(profile.imageUrl).openStream()
                    BitmapFactory.decodeStream(input)
                }.onSuccess { bmp ->
                    withContext(Dispatchers.Main) {
                        binding.imgProfile.setImageBitmap(bmp)
                    }
                }
            }

            // 4) 내 타임라인만 필터링해서 RecyclerView로
            val timelineRef = Firebase
                .database("https://snswithai-29d1f-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("user_data")
                .child(uidToLoad)
                .child("timeline")

            timelineRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { node ->
                        node.child("author_id").getValue(String::class.java)
                            .takeIf { it == uidToLoad }
                            ?.let {
                                TimelinePost(
                                    authorName     = profile.name,
                                    authorImageUrl = profile.imageUrl,
                                    content        = node.child("content")
                                        .getValue(String::class.java)
                                        .orEmpty(),
                                    createdAt      = node.child("created_at")
                                        .getValue(Long::class.java)
                                        ?: 0L,
                                    likeCount      = node.child("like_count")
                                        .getValue(Long::class.java)
                                        ?.toInt() ?: 0,
                                    commentCount   = node.child("comment_count")
                                        .getValue(Long::class.java)
                                        ?.toInt() ?: 0
                                )
                            }
                    }

                    binding.recyclerPosts.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        adapter = TimelineAdapter(list)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "타임라인 로드 실패", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // 5) 버튼 리스너
        binding.btnBack.setOnClickListener    { parentFragmentManager.popBackStack() }
        binding.btnMore.setOnClickListener    { /* TODO */ }
        binding.btnFollow.setOnClickListener  { viewModel.toggleFollow() }
        binding.btnMessage.setOnClickListener { /* TODO */ }
        binding.btnEditProfile.setOnClickListener { /* TODO */ }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
