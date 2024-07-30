package pjo.travelapp.data.entity

import com.google.gson.annotations.SerializedName

data class MessageRequest(
    val role: String,  // "user" or "assistant"
    val content: String
)

data class RunRequest(
    @SerializedName("assistant_id") val assistantId: String,
    val instructions: String? = null // 임시 추가
)


data class RunResponse(
    val id: String,
    @SerializedName("object") val objectType: String,
    val createdAt: Int,
    val status: String
)

data class MessageResponse(
    val id: String,
    @SerializedName("object") val objectType: String,
    val role: String,
    val content: List<ContentItem>
)

data class ContentItem(
    val type: String,
    val text: TextItem
)

data class TextItem(
    val value: String,
    val annotations: List<Any>
)

data class AssistantRequest(
    val instructions: String,
    val name: String,
    val tools: List<Tool>,
    val model: String
)

data class Tool(
    val type: String
)

data class AssistantResponse(
    val id: String,
    val objectType: String,
    val created: Int,
    @SerializedName("assistant_id") val assistantId: String
)

data class ThreadResponse(
    val id: String  // thread ID
)

data class MessagesListResponse(
    @SerializedName("object") val objectType: String,
    val data: List<MessageResponse>  // 메시지 목록을 저장
)

data class IsMessage(
    val message: String,
    val isUser: Boolean
)