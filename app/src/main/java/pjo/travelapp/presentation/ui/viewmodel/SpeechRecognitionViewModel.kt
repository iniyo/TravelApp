package pjo.travelapp.presentation.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SpeechRecognitionViewModel(application: Application) : AndroidViewModel(application) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val _recognitionResults = MutableLiveData<List<String>?>()
    private val _recognitionStatus = MutableLiveData<RecognitionStatus>()

    val recognitionResults: LiveData<List<String>?> get() = _recognitionResults
    val recognitionStatus: LiveData<RecognitionStatus> get() = _recognitionStatus

    init {
        initializeSpeechRecognizer()
    }

    private fun initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplication<Application>()).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _recognitionStatus.value = RecognitionStatus.READY
                }

                override fun onBeginningOfSpeech() {
                    _recognitionStatus.value = RecognitionStatus.BEGINNING
                }

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _recognitionStatus.value = RecognitionStatus.END
                    // 음성 인식이 종료될 때 상태를 변경하지 않도록 수정
                    if (_recognitionStatus.value != RecognitionStatus.LISTENING) {
                        _recognitionStatus.value = RecognitionStatus.END
                    }
                }

                override fun onError(error: Int) {
                    _recognitionStatus.value = RecognitionStatus.ERROR
                    stopListeningRecognizer()
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null) {
                        _recognitionResults.value = matches
                        _recognitionStatus.value = RecognitionStatus.SUCCESS
                    }
                    // 여기서 stopListeningRecognizer()를 호출하지 않도록 수정
                }

                override fun onPartialResults(partialResults: Bundle?) {}

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    fun startListening() {
        if (speechRecognizer == null) {
            initializeSpeechRecognizer()
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplication<Application>().packageName)
        }
        speechRecognizer?.startListening(intent)
        _recognitionStatus.value = RecognitionStatus.LISTENING
    }

    fun stopListeningRecognizer() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        _recognitionStatus.value = RecognitionStatus.STOPPED
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
    }

    enum class RecognitionStatus {
        READY, BEGINNING, LISTENING, END, SUCCESS, ERROR, STOPPED
    }
}
