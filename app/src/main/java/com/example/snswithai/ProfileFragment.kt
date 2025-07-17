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

    // ViewModelProvider 사용
    private lateinit var viewModel: ProfileViewModel

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

        // 버튼 클릭 리스너 (기존 로직 그대로)
        binding.btnBack.setOnClickListener { requireActivity().onBackPressed() }
        binding.btnMore.setOnClickListener { /* TODO: 더보기 로직 */ }
        binding.btnFollow.setOnClickListener { viewModel.toggleFollow() }
        binding.btnMessage.setOnClickListener { /* TODO: 메시지 전송 */ }
        binding.btnEditProfile.setOnClickListener { /* TODO: 프로필 편집 */ }

        // ViewModel 관찰: 기존 프로필 세팅 로직 이동
        viewModel.profile.observe(viewLifecycleOwner, Observer { profile ->
            binding.tvUsername.text = profile.name
            binding.tvKeywords.text = profile.keywords.joinToString(", ")
            binding.btnFollow.text = if (profile.isFollowing) "언팔로우" else "팔로우"

            // Glide 대신 순수 코루틴+BitmapFactory 로딩 (의존성 없이)
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val input = URL(profile.imageUrl).openStream()
                    val bmp = BitmapFactory.decodeStream(input)
                    withContext(Dispatchers.Main) {
                        binding.imgProfile.setImageBitmap(bmp)
                    }
                } catch (_: Exception) {
                    // placeholder 처리 등
                }
            }
        })

        // 기존 프로필 로드 호출
        viewModel.loadProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
