package pjo.travelapp.data.repo

import pjo.travelapp.data.entity.AssistantResponse
import pjo.travelapp.data.entity.IsMessage
import pjo.travelapp.data.entity.RunResponse
import pjo.travelapp.data.entity.ThreadResponse

interface AiChatRepository {
    suspend fun createApiAssistant(): AssistantResponse?
    suspend fun createThread(): ThreadResponse?
    suspend fun startRun(threadId: String): RunResponse?
    suspend fun pollForSystemResponse(threadId: String): String?
    suspend fun sendMessage(userMessage: String): String
    suspend fun setInstructions(userInstruction: String)
}