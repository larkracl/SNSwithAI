package com.example.snswithai

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private lateinit var imgHome: ImageView
    private lateinit var imgChat: ImageView
    private lateinit var imgAI: ImageView
    private lateinit var imgFriends: ImageView
    private lateinit var imgProfile: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ImageView 초기화
        imgHome = findViewById(R.id.img_home)
        imgChat = findViewById(R.id.img_chat)
        imgAI = findViewById(R.id.img_ai)
        imgFriends = findViewById(R.id.img_friends)
        imgProfile = findViewById(R.id.img_profile)

        // 기본 화면 및 선택 이미지 세팅
        switchFragment(HomeFragment())
        updateNavIcons(R.id.home)

        findViewById<LinearLayout>(R.id.home).setOnClickListener {
            switchFragment(HomeFragment())
            updateNavIcons(R.id.home)
        }
        /*findViewById<LinearLayout>(R.id.chat).setOnClickListener {
            switchFragment(ChatFragment())
            updateNavIcons(R.id.chat)
        }
        findViewById<LinearLayout>(R.id.navAI).setOnClickListener {
            switchFragment(AiFragment())
            updateNavIcons(R.id.navAI)
        }
        findViewById<LinearLayout>(R.id.navFriends).setOnClickListener {
            switchFragment(FriendsFragment())
            updateNavIcons(R.id.navFriends)
        }*/
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            switchFragment(ProfileFragment())
            updateNavIcons(R.id.navProfile)
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun updateNavIcons(selectedId: Int) {
        imgHome.setImageResource(if (selectedId == R.id.home) R.drawable.ic_home else R.drawable.ic_home_gray)
        imgChat.setImageResource(if (selectedId == R.id.chat) R.drawable.ic_chat else R.drawable.ic_chat_gray)
        imgAI.setImageResource(if (selectedId == R.id.navAI) R.drawable.ic_ai else R.drawable.ic_ai_gray)
        imgFriends.setImageResource(if (selectedId == R.id.navFriends) R.drawable.ic_friends else R.drawable.ic_friends_gray)
        imgProfile.setImageResource(if (selectedId == R.id.navProfile) R.drawable.ic_profile else R.drawable.ic_profile_gray)
    }
}
