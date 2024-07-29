package pjo.travelapp.data.repo

import pjo.travelapp.data.entity.ChatRequest
import pjo.travelapp.data.entity.ChatResponse
import pjo.travelapp.data.remote.AiChatService
import java.io.IOException
import javax.inject.Inject

class AiChatRepositoryImpl @Inject constructor(
    private val api: AiChatService
): AiChatRepository {
    override suspend fun sendMessage(message: String): Result<ChatResponse> {
        return try {
            val response = api.sendMessage(ChatRequest(message))
            Result.success(response)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
}