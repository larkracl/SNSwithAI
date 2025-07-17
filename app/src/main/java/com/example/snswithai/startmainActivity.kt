package com.example.snswithai

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class StartMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        // Intent에서 UID 받기
        val receivedUid = intent.getStringExtra("USER_UID")

        if (receivedUid != null) {
            Log.d("StartMainActivity", "Received UID: $receivedUid")
            Toast.makeText(this, "User UID: $receivedUid", Toast.LENGTH_LONG).show()
            // 이 UID를 사용하여 사용자 관련 데이터 로드 또는 다른 작업 수행
        } else {
            Log.d("StartMainActivity", "UID not received.")
            // UID가 없는 경우의 처리 (예: 오류 메시지 표시, 로그인 화면으로 다시 보내기 등)
        }

        if (savedInstanceState == null) {
            // HomeFragment.newInstance()를 사용하여 UID 전달
            val homeFragment = HomeFragment.newInstance(receivedUid) // receivedUid는 nullable일 수 있음
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
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
        // 5번째 버튼: 프로필
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileControllerFragment())
                .commit()
        }
    }
}
