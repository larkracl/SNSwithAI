package com.example.snswithai

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class StartMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        if (savedInstanceState == null) { // Activity가 처음 생성될 때만 Fragment를 추가 (화면 회전 등으로 재생성될 때 중복 추가 방지)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()) // R.id.fragment_container를 HomeFragment로 교체
                .commit()
        }
        // "채팅" 탭 클릭 리스너만 남겨둠
        findViewById<LinearLayout>(R.id.chat).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChatListFragment())
                .commit()
        }
        // 2번째 버튼(navAI): ChatListFragment 예시
        findViewById<LinearLayout>(R.id.navAI).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DbTestFragment())
                .commit()
        }
        // 3번째 버튼 (Home)
        findViewById<LinearLayout>(R.id.home).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
        //5번째 버튼
        findViewById<LinearLayout>(R.id.home).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
    }
}
