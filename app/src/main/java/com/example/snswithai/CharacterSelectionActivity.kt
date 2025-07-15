package com.example.snswithai

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.snswithai.databinding.ActivityCharacterSelectionBinding

class CharacterSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCharacterSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCharacterSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val buttons = mapOf(
            binding.character1Button to 1,
            binding.character2Button to 2,
            binding.character3Button to 3,
            binding.character4Button to 4,
            binding.character5Button to 5
        )

        for ((button, characterId) in buttons) {
            button.setOnClickListener {
                val intent = Intent(this, ConversationActivity::class.java).apply {
                    putExtra("CHARACTER_ID", characterId)
                }
                startActivity(intent)
            }
        }
    }
}