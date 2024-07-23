package pjo.travelapp.presentation.util

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class AudioListenerToText(private val context: Context, private val tv: TextView) {

    private val _speechState = MutableLiveData<Boolean>()
    val speechState: LiveData<Boolean> get() = _speechState

    private val _speechResult = MutableLiveData<String>()
    val speechResult: LiveData<String> get() = _speechResult

    // 리스너 설정
    private val recognitionListener: RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle) {
            // 음성 녹음을 준비 중일 때는 텍스트를 변경하지 않습니다.
            Log.d("TAG", "onReadyForSpeech")
        }

        override fun onBeginningOfSpeech() {
            // 음성이 들어왔을 때 텍스트를 변경하고 상태를 업데이트합니다.
            tv.text = "음성녹음 중"
            _speechState.value = true
            Log.d("TAG", "onBeginningOfSpeech")
        }

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray) {}

        override fun onEndOfSpeech() {
            tv.text = "끝!"
            _speechState.value = false
            Log.d("TAG", "onEndOfSpeech")
        }

        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
                SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "퍼미션 없음"
                SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트웍 타임아웃"
                SpeechRecognizer.ERROR_NO_MATCH -> "찾을 수 없음"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER 가 바쁨"
                SpeechRecognizer.ERROR_SERVER -> "서버가 이상함"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "말하는 시간초과"
                else -> "알 수 없는 오류임"
            }
            Log.d("TAG", "onVoiceError: $message")
          /*  if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                Toast.makeText(context, "음성인식이 제대로 수행되지 않았어요: $message", Toast.LENGTH_SHORT).show()
            }*/
            _speechState.value = false
        }

        override fun onResults(results: Bundle) {
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                tv.text = matches[0]
                _speechResult.value = matches[0]
            } else {
                tv.text = "결과를 찾을 수 없음"
                _speechResult.value = ""
            }
            Log.d("TAG", "onResults: ${_speechResult.value}")
        }

        override fun onPartialResults(partialResults: Bundle) {}

        override fun onEvent(eventType: Int, params: Bundle) {}
    }

    fun getRecognitionListener(): RecognitionListener {
        return recognitionListener
    }
}
