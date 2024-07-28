package pjo.travelapp.presentation.ui.fragment

import android.util.Log
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentVoiceRecognitionBinding
import pjo.travelapp.presentation.ui.viewmodel.MainViewModel
import pjo.travelapp.presentation.ui.viewmodel.SpeechRecognitionViewModel
import pjo.travelapp.presentation.util.navigator.AppNavigator
import javax.inject.Inject

@AndroidEntryPoint
class VoiceRecognitionFragment : BaseFragment<FragmentVoiceRecognitionBinding>() {

    @Inject
    lateinit var navigator: AppNavigator
    private val mainViewModel: MainViewModel by activityViewModels()
    private val speechViewModel: SpeechRecognitionViewModel by activityViewModels()

    override fun initListener() {
        bind {
            btnSpeech.setOnClickListener {
                Log.d("TAG", "Button clicked")
                speechViewModel.startListening()
            }
            toolbar.ivSignDisplayBackButton.setOnClickListener {
                navigator.navigateUp()
            }
        }
    }

    override fun initViewModel() {
        bind {
            launchWhenStarted {
                launch {
                    speechViewModel.recognitionResults.observe(viewLifecycleOwner) { results ->
                        results?.let {
                            speechViewModel.fetchVoiceString(it.joinToString(" "))
                        }
                    }
                }

                launch {
                    speechViewModel.recognitionStatus.observe(viewLifecycleOwner) { status ->
                        status?.let {
                            btnSpeech.isEnabled =
                                it != SpeechRecognitionViewModel.RecognitionStatus.LISTENING
                            btnSpeech.text = it.name
                            when (status) {
                                SpeechRecognitionViewModel.RecognitionStatus.END -> lavSoundListener.cancelAnimation()
                                SpeechRecognitionViewModel.RecognitionStatus.READY -> {}
                                SpeechRecognitionViewModel.RecognitionStatus.BEGINNING -> lavSoundListener.playAnimation()
                                SpeechRecognitionViewModel.RecognitionStatus.LISTENING -> {}
                                SpeechRecognitionViewModel.RecognitionStatus.SUCCESS -> navigator.navigateUp()
                                SpeechRecognitionViewModel.RecognitionStatus.ERROR -> {  }
                                SpeechRecognitionViewModel.RecognitionStatus.STOPPED -> {
                                    btnSpeech.text = getString(R.string.voice_fetch_failed)
                                    lavSoundListener.cancelAnimation()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        speechViewModel.stopListeningRecognizer()
    }

    override fun onResume() {
        super.onResume()
        speechViewModel.startListening()
    }
}