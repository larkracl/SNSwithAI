package com.example.snswithai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Firestore 데이터베이스에서 사용자 목록을 가져와 로그에 출력
        logAllUsers()

        val nextButton: Button = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            val intent = Intent(this, NewActivity::class.java)
            startActivity(intent)
        }
    }

    private fun logAllUsers() {
        val db = FirebaseDatabase.getInstance()
        db.getReference("users")
            .get()
            .addOnSuccessListener { result ->
                if (!result.exists()) {
                    Log.d("MainActivity", "No users found in Realtime Database.")
                    return@addOnSuccessListener
                }
                Log.d("MainActivity", "--- Realtime Database Users ---")
                for (child in result.children) {
                    Log.d("MainActivity", "User ID: ${child.key}, Data: ${child.value}")
                }
                Log.d("MainActivity", "-----------------------")
            }
            .addOnFailureListener { exception ->
                Log.w("MainActivity", "Error getting documents.", exception)
            }
    }
}