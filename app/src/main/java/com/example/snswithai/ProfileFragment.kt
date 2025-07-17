package com.example.snswithai

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.snswithai.databinding.FragmentProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel

    // ① arguments에서 USER_UID 꺼내기
    private val userUid: String?
        get() = arguments?.getString("USER_UID")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel 초기화
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // 버튼 클릭 리스너
        binding.btnBack.setOnClickListener { requireActivity().onBackPressed() }
        binding.btnMore.setOnClickListener { /* TODO: 더보기 로직 */ }
        binding.btnFollow.setOnClickListener { viewModel.toggleFollow() }
        binding.btnMessage.setOnClickListener { /* TODO: 메시지 전송 */ }
        binding.btnEditProfile.setOnClickListener { /* TODO: 프로필 편집 */ }

        // ViewModel 관찰
        viewModel.profile.observe(viewLifecycleOwner, Observer { profile ->
            binding.tvUsername.text    = profile.name
                   binding.tvKeywords.text = profile.description   // ← description 으로 변경
            binding.btnFollow.text     = if (profile.isFollowing) "언팔로우" else "팔로우"

            // 프로필 이미지 로딩
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val input = URL(profile.imageUrl).openStream()
                    val bmp   = BitmapFactory.decodeStream(input)
                    withContext(Dispatchers.Main) {
                        binding.imgProfile.setImageBitmap(bmp)
                    }
                } catch (_: Exception) {
                    // placeholder 처리 등
                }
            }
        })

        // ③ userUid가 있으면 그걸, 없으면 기본값 넘기기
        val uidToLoad = userUid ?: "char101"
        viewModel.loadProfile(uidToLoad)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
