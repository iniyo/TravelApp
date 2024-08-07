package pjo.travelapp.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pjo.travelapp.data.entity.IsMessage
import pjo.travelapp.data.repo.AiChatRepository
import pjo.travelapp.presentation.util.LatestUiState
import javax.inject.Inject

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val aiChatRepository: AiChatRepository
) : ViewModel() {

    private val _response = MutableStateFlow<LatestUiState<IsMessage>>(LatestUiState.Loading)
    val response: StateFlow<LatestUiState<IsMessage>> get() = _response

    init {
       /* initializeAssistantAndThread()*/
    }

    private fun setUserInstructions(userInstructions: String) {
        viewModelScope.launch {
            aiChatRepository.setInstructions(userInstructions)
        }
        initializeAssistantAndThread()
    }

    private fun initializeAssistantAndThread() {
        viewModelScope.launch {
            try {
                _response.value = LatestUiState.Loading  // 로딩 상태로 업데이트
                val assistant = aiChatRepository.createApiAssistant()
                val thread = aiChatRepository.createThread()
                if (assistant != null && thread != null) {
                    startRun(thread.id)  // Run 시작
                } else {
                    _response.value =
                        LatestUiState.Error(Exception("Failed to initialize assistant or thread."))
                }
            } catch (e: Exception) {
                _response.value = LatestUiState.Error(e)
            }
        }
    }

    private fun startRun(threadId: String) {
        viewModelScope.launch {
            try {
                _response.value = LatestUiState.Loading  // 로딩 상태로 업데이트
                val runResponse = aiChatRepository.startRun(threadId)
                if (runResponse != null) {
                    fetchSystemResponse(threadId)  // Run 후 시스템 메시지 폴링 시작
                } else {
                    _response.value = LatestUiState.Error(Exception("Failed to start run."))
                }
            } catch (e: Exception) {
                _response.value = LatestUiState.Error(e)
            }
        }
    }

    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            try {
                _response.value = LatestUiState.Loading  // 로딩 상태로 업데이트
                val systemResponse = aiChatRepository.sendMessage(userMessage)
                _response.value = LatestUiState.Success(IsMessage(systemResponse, false))
            } catch (e: Exception) {
                _response.value = LatestUiState.Error(e)
            }
        }
    }

    private fun fetchSystemResponse(threadId: String) {
        viewModelScope.launch {
            try {
                val systemResponse = aiChatRepository.pollForSystemResponse(threadId)
                if (systemResponse != null) {
                    _response.value = LatestUiState.Success(IsMessage(systemResponse, false))
                } else {
                    _response.value = LatestUiState.Error(Exception("No response from AI"))
                }
            } catch (e: Exception) {
                _response.value = LatestUiState.Error(e)
            }
        }
    }
}
