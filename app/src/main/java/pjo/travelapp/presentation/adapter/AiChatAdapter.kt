package pjo.travelapp.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pjo.travelapp.R
import pjo.travelapp.data.entity.IsMessage
import pjo.travelapp.databinding.AiChatItemBinding
import pjo.travelapp.databinding.ItemLoadingBinding

class AiChatAdapter(
    private val itemClickListener: (IsMessage) -> Unit
) : ListAdapter<IsMessage, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    var isLoading = false
        set(value) {
            field = value
            // notify the adapter to update when loading state changes
            if (value) {
                notifyItemInserted(itemCount)
            } else {
                notifyItemRemoved(itemCount)
            }
        }

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
            holder.bind(getItem(position))
        } else if (holder is LoadingViewHolder) {
            holder.bind()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading && position == itemCount - 1) VIEW_TYPE_LOADING else VIEW_TYPE_MESSAGE
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (isLoading) 1 else 0
    }

    fun addMessage(message: IsMessage) {
        val currentList = currentList.toMutableList()
        currentList.add(message)
        submitList(currentList)
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
            itemView.setOnLongClickListener {
                itemClickListener(item)
                true
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

class MessageDiffCallback : DiffUtil.ItemCallback<IsMessage>() {
    override fun areItemsTheSame(oldItem: IsMessage, newItem: IsMessage): Boolean {
        return oldItem.message == newItem.message
    }

    override fun areContentsTheSame(oldItem: IsMessage, newItem: IsMessage): Boolean {
        return oldItem == newItem
    }
}
