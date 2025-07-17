// ProfileControllerFragment.kt
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

class ProfileControllerFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
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

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        binding.btnBack.setOnClickListener { requireActivity().onBackPressed() }
        binding.btnMore.setOnClickListener { /* TODO: More action */ }
        binding.btnFollow.setOnClickListener { viewModel.toggleFollow() }
        binding.btnMessage.setOnClickListener { /* TODO: Send Message */ }
        binding.btnEditProfile.setOnClickListener { /* TODO: Edit Profile */ }

        viewModel.profile.observe(viewLifecycleOwner, Observer { profile ->
            binding.tvUsername.text = profile.name
            binding.tvKeywords.text = profile.keywords.joinToString(", ")
            binding.btnFollow.text = if (profile.isFollowing) "언팔로우" else "팔로우"

            // 의존성 없이 URL 이미지를 로드
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val connection = URL(profile.imageUrl).openConnection()
                    connection.connect()
                    val input = connection.getInputStream()
                    val bitmap = BitmapFactory.decodeStream(input)
                    withContext(Dispatchers.Main) {
                        binding.imgProfile.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    // 에러 처리 (placeholder 등)
                }
            }
        })

        viewModel.loadProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}