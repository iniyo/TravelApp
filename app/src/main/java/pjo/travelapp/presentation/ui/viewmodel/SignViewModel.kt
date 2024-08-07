package pjo.travelapp.presentation.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pjo.travelapp.presentation.util.signmanager.KakaoSignManager
import pjo.travelapp.presentation.util.signmanager.NaverSignManager
import javax.inject.Inject


@HiltViewModel
class SignViewModel @Inject constructor(
    private val kakaoSignManager: KakaoSignManager,
    private val naverSignManager: NaverSignManager
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> get() = _isLoggedIn.asStateFlow()

    fun checkLoginStatus() {
        viewModelScope.launch {
            val naverLoggedIn = naverSignManager.isLoggedIn()
            if (naverLoggedIn) {
                _isLoggedIn.value = true
            } else {
                kakaoSignManager.isLoggedIn { kakaoLoggedIn ->
                    _isLoggedIn.value = kakaoLoggedIn
                }
            }
        }
    }
}