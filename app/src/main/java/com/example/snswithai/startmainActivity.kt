package com.example.snswithai

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class StartMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // "채팅" 탭 클릭 리스너만 남겨둠
        findViewById<LinearLayout>(R.id.chat).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DbTestFragment())
                .commit()
        }
        // 2번째 버튼(navAI): ChatListFragment 예시
        findViewById<LinearLayout>(R.id.navAI).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChatListFragment())
                .commit()
        }
    }
}
