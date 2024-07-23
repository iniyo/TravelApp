package pjo.travelapp.presentation.ui.fragment

import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import pjo.travelapp.databinding.FragmentVoiceRecognitionBinding
import pjo.travelapp.presentation.ui.viewmodel.MainViewModel
import pjo.travelapp.presentation.util.AudioListenerToText
import pjo.travelapp.presentation.util.navigator.AppNavigator
import javax.inject.Inject

@AndroidEntryPoint
class VoiceRecognitionFragment : BaseFragment<FragmentVoiceRecognitionBinding>() {

    @Inject
    lateinit var navigator: AppNavigator
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var speechRecognizer: SpeechRecognizer

    override fun initView() {
        super.initView()
        bind {
            // RecognizerIntent 생성
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, requireContext().packageName) // 현재 패키지 이름 설정
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR") // 언어 설정
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true) // 부분 결과를 허용
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000) // 최대 침묵 시간 (2초)
            }

            // 새 SpeechRecognizer 를 만드는 팩토리 메서드
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
            val audioListenerToText = AudioListenerToText(requireContext(), tvCurrentSpeechState)
            speechRecognizer.setRecognitionListener(audioListenerToText.getRecognitionListener()) // 리스너 설정
            speechRecognizer.startListening(intent)

            audioListenerToText.speechState.observe(viewLifecycleOwner) {
                if (it) {
                    lavSoundListener.playAnimation()
                } else {
                    lavSoundListener.cancelAnimation()
                }
            }

            audioListenerToText.speechResult.observe(viewLifecycleOwner) { result ->
                if (result.isNotEmpty()) {
                    viewModel.fetchVoiceString(result)
                    navigator.navigateUp() // 음성 녹음이 성공적으로 끝나면 프래그먼트를 닫음
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        speechRecognizer.stopListening()
        navigator.navigateUp()
    }

    override fun onStop() {
        super.onStop()
        speechRecognizer.stopListening()
        navigator.navigateUp()
    }
}
