package pjo.travelapp.data.remote


import pjo.travelapp.BuildConfig
import pjo.travelapp.data.entity.AssistantRequest
import pjo.travelapp.data.entity.AssistantResponse
import pjo.travelapp.data.entity.MessageRequest
import pjo.travelapp.data.entity.MessageResponse
import pjo.travelapp.data.entity.MessagesListResponse
import pjo.travelapp.data.entity.RunRequest
import pjo.travelapp.data.entity.RunResponse
import pjo.travelapp.data.entity.ThreadResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface AiChatService {
    @Headers(
        "Authorization: Bearer ${BuildConfig.open_api_key}",
        "Content-Type: application/json",
        "OpenAI-Beta: assistants=v2"
    )
    @POST("/v1/threads")
    suspend fun createThread(): Response<ThreadResponse>

    @Headers(
        "Authorization: Bearer ${BuildConfig.open_api_key}",
        "Content-Type: application/json",
        "OpenAI-Beta: assistants=v2"
    )
    @POST("/v1/threads/{thread_id}/messages")
    suspend fun postUserMessage(
        @Path("thread_id") threadId: String,
        @Body messageRequest: MessageRequest
    ): Response<MessageResponse>

    @Headers(
        "Authorization: Bearer ${BuildConfig.open_api_key}",
        "Content-Type: application/json",
        "OpenAI-Beta: assistants=v2"
    )
    @POST("/v1/assistants")
    suspend fun createAssistant(@Body assistantRequest: AssistantRequest): Response<AssistantResponse>


    @Headers(
        "Authorization: Bearer ${BuildConfig.open_api_key}",
        "Content-Type: application/json",
        "OpenAI-Beta: assistants=v2"
    )
    @POST("/v1/threads/{thread_id}/runs")
    suspend fun createRun(
        @Path("thread_id") threadId: String,
        @Body runRequest: RunRequest
    ): Response<RunResponse>

    @Headers(
        "Authorization: Bearer ${BuildConfig.open_api_key}",
        "Content-Type: application/json",
        "OpenAI-Beta: assistants=v2"
    )
    @GET("/v1/threads/{thread_id}/messages")
    suspend fun listMessages(@Path("thread_id") threadId: String): Response<MessagesListResponse>
}