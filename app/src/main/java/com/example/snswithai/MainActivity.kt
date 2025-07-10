package com.example.snswithai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.example.snswithai.data.local.db.entity.User
import com.example.snswithai.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Firebase 초기화
        FirebaseApp.initializeApp(this)
        val db = FirebaseFirestore.getInstance()
        userRepository = UserRepository(db)
        firebaseAuth = FirebaseAuth.getInstance()

        // Google Sign-In 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 로그인 버튼 클릭 리스너 설정
        findViewById<Button>(R.id.signInButton).setOnClickListener {
            signIn()
        }

        // 이미 로그인되어 있는지 확인
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            updateUIForLoggedInUser(currentUser.uid)
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d("MainActivity", "firebaseAuthWithGoogle: " + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("MainActivity", "Google sign in failed", e)
                // 로그인 실패 처리
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    Log.d("MainActivity", "Firebase 인증 성공: ${firebaseUser.uid}")
                    // 사용자 정보 Firestore에 저장 또는 업데이트
                    val user = User(
                        userId = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "",
                        email = firebaseUser.email ?: "",
                        profileImageUrl = firebaseUser.photoUrl?.toString()
                    )
                    userRepository.createUser(user) // 또는 updateUser
                    withContext(Dispatchers.Main) {
                        updateUIForLoggedInUser(firebaseUser.uid)
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Firebase 인증 실패", e)
                // Firebase 인증 실패 처리
            }
        }
    }

    private fun updateUIForLoggedInUser(userId: String) {
        findViewById<Button>(R.id.signInButton).visibility = View.GONE
        findViewById<TextView>(R.id.helloAiWorldText).visibility = View.VISIBLE
        Log.d("MainActivity", "사용자 ${userId} 로그인 성공, UI 업데이트.")
    }
}