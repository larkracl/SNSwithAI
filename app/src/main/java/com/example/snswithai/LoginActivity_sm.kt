package com.example.snswithai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LoginActivity_sm : AppCompatActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val googleSignInButton = findViewById<SignInButton>(R.id.googleSignInButton)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // For demonstration purposes, a simple hardcoded check
            if (email == "test@example.com" && password == "password123") {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, StartMainActivity::class.java)
                startActivity(intent)
                finish() // Close LoginActivity so user can't go back to it
            } else {
                Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
            }
        }

        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Google Sign-In Failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let { firebaseUser ->
                        val uid = firebaseUser.uid // UID 가져오기
                        checkUserExistsAndSave(firebaseUser) // 기존 데이터베이스 로직

                        Toast.makeText(this, "Authentication successful.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, StartMainActivity::class.java)
                        intent.putExtra("USER_UID", uid) // Intent에 UID 추가
                        startActivity(intent)
                        finish()
                    } ?: run {
                        // user가 null인 경우 (이론적으로는 task.isSuccessful이면 null이 아니어야 함)
                        Toast.makeText(this, "Failed to get user information.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserExistsAndSave(firebaseUser: com.google.firebase.auth.FirebaseUser) {
        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.uid)
        dbRef.get().addOnSuccessListener { dataSnapshot ->
            if (!dataSnapshot.exists()) {
                // User does not exist, save them to the database
                saveUserToDatabase(firebaseUser)
            }
            // If user exists, do nothing.
        }.addOnFailureListener{
            // Handle the error
            Toast.makeText(this, "Failed to check user existence: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveUserToDatabase(firebaseUser: com.google.firebase.auth.FirebaseUser) {
        val db = FirebaseDatabase.getInstance()
        val user = hashMapOf(
            "email" to firebaseUser.email,
            "name" to firebaseUser.displayName,
            "profileImageUrl" to firebaseUser.photoUrl.toString()
        )

        db.getReference("users").child(firebaseUser.uid)
            .setValue(user)
            .addOnSuccessListener {
                // Optional: Handle success, e.g., log or show a toast
            }
            .addOnFailureListener { e ->
                // Optional: Handle failure
                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}