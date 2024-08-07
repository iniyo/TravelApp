package pjo.travelapp.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pjo.travelapp.R
import pjo.travelapp.data.entity.IsMessage
import pjo.travelapp.databinding.AiChatItemBinding
import pjo.travelapp.databinding.ItemLoadingBinding

class AiChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val messages = mutableListOf<IsMessage>()
    private var isLoading = false

    companion object {
        const val VIEW_TYPE_MESSAGE = 0
        const val VIEW_TYPE_LOADING = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_MESSAGE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.ai_chat_item, parent, false)
            MessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MessageViewHolder) {
            holder.bind(messages[position])
        } else if (holder is LoadingViewHolder) {
            holder.bind()
        }
    }

    override fun getItemViewType(position: Int): Int {
        // messages 리스트 끝에 로딩 항목을 추가
        return if (isLoading && position == messages.size) VIEW_TYPE_LOADING else VIEW_TYPE_MESSAGE
    }


    override fun getItemCount(): Int {
        return messages.size + if (isLoading) 1 else 0
    }

    fun addMessage(message: IsMessage) {
        messages.add(message)
        setLoading(false)  // 메시지 추가 후 로딩 상태를 false로 설정하고 전체 갱신
    }

    fun setLoading(loading: Boolean) {
        if (loading) {
            if (!isLoading) {
                isLoading = true
                notifyItemInserted(messages.size) // 로딩 항목 추가
            }
        } else {
            if (isLoading) {
                isLoading = false
                notifyItemRemoved(messages.size) // 로딩 항목 제거
            }
        }
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = AiChatItemBinding.bind(itemView)
        fun bind(item: IsMessage) {
            if (item.isUser) {
                binding.tvUser.text = item.message
                binding.llLeftChat.visibility = View.GONE
                binding.llRightChat.visibility = View.VISIBLE
            } else {
                binding.tvAssistant.text = item.message
                binding.llLeftChat.visibility = View.VISIBLE
                binding.llRightChat.visibility = View.GONE
            }
        }
    }

    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemLoadingBinding.bind(itemView)
        fun bind() {
            binding.ltvLoading.playAnimation()
        }
    }
}
