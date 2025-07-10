package com.example.snswithai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.snswithai.stt.STTManager

class VoiceTestActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_RECORD_AUDIO = 100
    }

    private lateinit var btnStartListen: Button
    private lateinit var pbLevel: ProgressBar
    private lateinit var tvRecognized: TextView
    private lateinit var sttManager: STTManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_voice_test)

        btnStartListen = findViewById(R.id.btn_start_listen)
        pbLevel        = findViewById(R.id.pb_level)
        tvRecognized   = findViewById(R.id.tv_recognized)

        btnStartListen.isEnabled = false
        tvRecognized.text = "권한 상태: 체크 중…"

        // 1) RECORD_AUDIO 런타임 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO
            )
        } else {
            onAudioPermissionGranted()
        }

        // 2) STTManager 초기화 (인식 결과 + RMS 레벨)
        sttManager = STTManager(
            this,
            onResult = { recognized ->
                runOnUiThread { tvRecognized.text = "인식 결과: $recognized" }
            },
            onRms = { rms ->
                // rmsdB는 보통 0~12 사이, 0~100으로 매핑
                val level = ((rms + 12f) * (100f / 12f)).toInt().coerceIn(0, 100)
                runOnUiThread { pbLevel.progress = level }
            }
        )

        // 3) 버튼 클릭으로 듣기 시작
        btnStartListen.setOnClickListener {
            pbLevel.progress = 0
            tvRecognized.text = "음성 인식 중..."
            sttManager.startListening()
        }
    }

    private fun onAudioPermissionGranted() {
        btnStartListen.isEnabled = true
        tvRecognized.text = "권한 상태: 허용됨\n버튼을 눌러 음성 인식을 시작하세요."
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onAudioPermissionGranted()
            } else {
                btnStartListen.isEnabled = false
                tvRecognized.text = "권한 상태: 거부됨\n녹음 권한이 필요합니다."
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sttManager.destroy()
    }
}
