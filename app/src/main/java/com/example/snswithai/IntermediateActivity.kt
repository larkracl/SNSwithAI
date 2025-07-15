package com.example.snswithai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.util.Log

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

        // Firebase Database logging
        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.value
                Log.d("DBTEST", "Firebase Data: $value")
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("DBTEST", "Failed to read value.", error.toException())
            }
        })
    }
}
