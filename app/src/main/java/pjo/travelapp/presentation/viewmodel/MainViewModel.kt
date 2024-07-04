package pjo.travelapp.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    var isReady: Boolean = false
        private set // 외부에서 값 변경 못함. readOnly -> setter private

    fun setReady(status: Boolean) {
        isReady = status
    }



}