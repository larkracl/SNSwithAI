package com.example.snswithai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class IntermediateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intermediate)

        val goToMainActivityButton: Button = findViewById(R.id.goToMainActivityButton)
        val goToNewActivitySmButton: Button = findViewById(R.id.goToNewActivitySmButton)

        goToMainActivityButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Optional: finish IntermediateActivity so user can't go back to it
        }

        goToNewActivitySmButton.setOnClickListener {
            val intent = Intent(this, NewActivity_sm::class.java)
            startActivity(intent)
            finish() // Optional: finish IntermediateActivity so user can't go back to it
        }
    }
}
