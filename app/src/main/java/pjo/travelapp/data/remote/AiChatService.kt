package pjo.travelapp.data.remote

import pjo.travelapp.data.entity.ChatRequest
import pjo.travelapp.data.entity.ChatResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AiChatService {
    @Headers("Content-Type: application/json")
    @POST("/chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse
}