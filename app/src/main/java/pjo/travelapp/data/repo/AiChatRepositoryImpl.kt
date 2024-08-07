package pjo.travelapp.data.repo


import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.delay
import pjo.travelapp.R
import pjo.travelapp.data.entity.AssistantRequest
import pjo.travelapp.data.entity.AssistantResponse
import pjo.travelapp.data.entity.IsMessage
import pjo.travelapp.data.entity.MessageRequest
import pjo.travelapp.data.entity.RunRequest
import pjo.travelapp.data.entity.RunResponse
import pjo.travelapp.data.entity.ThreadResponse
import pjo.travelapp.data.entity.Tool
import pjo.travelapp.data.remote.AiChatService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiChatRepositoryImpl @Inject constructor(
    private val apiService: AiChatService,
    private val context: Context
) : AiChatRepository {
    private var currentThreadId: String? = null
    private var currentAssistantId: String? = null
    private var userInstruction: String = context.getString(R.string.instruction)


    override suspend fun createThread(): ThreadResponse? {
        val response = apiService.createThread()
        if (response.isSuccessful) {
            currentThreadId = response.body()?.id
            return response.body()
        } else {
            println("Failed to create thread: ${response.errorBody()?.string()}")
            return null
        }
    }

    override suspend fun createApiAssistant(): AssistantResponse? {
        if(userInstruction != ""){
            val assistantRequest = AssistantRequest(
                instructions = userInstruction,
                name = "custom assistant",
                tools = listOf(Tool(type = "code_interpreter")),
                model = "gpt-4o"
            )
            val response = apiService.createAssistant(assistantRequest)
            if (response.isSuccessful) {
                currentAssistantId = response.body()?.id
                return response.body()
            } else {
                println("Failed to get response: ${response.errorBody()?.string()}")
                return null
            }
        }
        else {
            println("not set instruction")
            return null
        }
    }

    override suspend fun setInstructions(userInstruction: String) {
        this.userInstruction = userInstruction
    }

    override suspend fun startRun(threadId: String): RunResponse? {
        val runRequest = RunRequest(assistantId = currentAssistantId ?: return null)
        val response = apiService.createRun(threadId, runRequest)
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun sendMessage(userMessage: String): String {
        val threadId = currentThreadId ?: return "Thread ID not set or thread creation failed."
        val messageRequest = MessageRequest(role = "user", content = userMessage)
        val response = apiService.postUserMessage(threadId, messageRequest)
        if (response.isSuccessful) {
            println("Message sent successfully: ${response.body()?.content}")
            val assistantId = currentAssistantId ?: return "Assistant ID not set or assistant creation failed."
            val runRequest = RunRequest(assistantId = assistantId)
            val runResponse = apiService.createRun(threadId, runRequest)
            if (runResponse.isSuccessful) {
                println("Run started successfully")
                return pollForSystemResponse(threadId)
            } else {
                return "Failed to start run: ${runResponse.errorBody()?.string()}"
            }
        } else {
            return "Failed to send message: ${response.errorBody()?.string()}"
        }
    }
    override suspend fun pollForSystemResponse(threadId: String): String {
        var attempts = 0
        val maxAttempts = 30
        while (attempts < maxAttempts) {
            delay(1000)
            val response = apiService.listMessages(threadId)
            if (response.isSuccessful) {
                val messagesResponse = response.body()
                val data = messagesResponse?.data
                if (!data.isNullOrEmpty() && data[0].content.isNotEmpty()) {
                    println("API response poll: ${Gson().toJson(data[0])}")
                    println("API response received: ${Gson().toJson(messagesResponse)}")
                    data.find { it.role == "assistant" }?.let {
                        return it.content.joinToString("\n") { content -> content.text.value }
                    }
                }
            } else {
                println("Failed to fetch messages: ${response.errorBody()?.string()}")
            }
            attempts++
        }
        return "No system response received after $maxAttempts attempts."
    }

}