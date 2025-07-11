package com.example.snswithai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

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
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.d("MainActivity", "No users found in Firestore.")
                    return@addOnSuccessListener
                }
                Log.d("MainActivity", "--- Firestore Users ---")
                for (document in result) {
                    Log.d("MainActivity", "User ID: ${document.id}, Data: ${document.data}")
                }
                Log.d("MainActivity", "-----------------------")
            }
            .addOnFailureListener { exception ->
                Log.w("MainActivity", "Error getting documents.", exception)
            }
    }
}