package pjo.travelapp.data.repo

import pjo.travelapp.data.entity.ChatResponse

interface AiChatRepository {
    suspend fun sendMessage(message: String): Result<ChatResponse>
}