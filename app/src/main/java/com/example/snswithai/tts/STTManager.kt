package com.example.snswithai.stt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

class STTManager(
    context: Context,
    private val onResult: (String) -> Unit,
    private val onRms: (Float) -> Unit
) : RecognitionListener {

    private val recognizer: SpeechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(this@STTManager)
        }

    private val intent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        putExtra(RecognizerIntent.EXTRA_PROMPT, "음성을 입력하세요")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    /** 호출하면 음성 듣기를 시작합니다 */
    fun startListening() = recognizer.startListening(intent)

    /** 필요 시 청취를 중단합니다 */
    fun stopListening() = recognizer.stopListening()

    /** 사용 후 반드시 호출해 리소스를 해제합니다 */
    fun destroy() = recognizer.destroy()

    // RecognitionListener 구현 (시그니처 정확히 맞춰야 합니다)

    override fun onReadyForSpeech(params: Bundle) {
        Log.d("STTManager", "준비 완료")
    }

    override fun onBeginningOfSpeech() {
        // 사용자가 말하기 시작
    }

    override fun onRmsChanged(rmsdB: Float) {
        // 실시간 RMS 레벨 콜백
        Log.d("STTManager", "RMS: $rmsdB")
        onRms(rmsdB)
    }

    override fun onBufferReceived(buffer: ByteArray) {
        // 버퍼 수신
    }

    override fun onEndOfSpeech() {
        Log.d("STTManager", "말하기 끝")
    }

    override fun onError(error: Int) {
        Log.e("STTManager", "STT 오류 코드: $error")
    }

    override fun onResults(results: Bundle) {
        val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        matches?.firstOrNull()?.let {
            Log.d("STTManager", "최종 인식 결과: $it")
            onResult(it)
        }
    }

    override fun onPartialResults(partialResults: Bundle) {
        val matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        matches?.firstOrNull()?.let {
            Log.d("STTManager", "중간 인식 결과: $it")
            onResult(it)
        }
    }

    override fun onEvent(eventType: Int, params: Bundle) {
        // 기타 이벤트
    }
}
