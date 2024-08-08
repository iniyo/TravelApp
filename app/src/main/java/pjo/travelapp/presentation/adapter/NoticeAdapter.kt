package pjo.travelapp.presentation.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pjo.travelapp.data.entity.FireStoreNotice
import pjo.travelapp.databinding.RvItemNoticeBinding

class NoticeAdapter(
    val itemClickListener: (FireStoreNotice) -> Unit,
) : ListAdapter<FireStoreNotice, NoticeAdapter.ViewHolder>(NoticeDiffCallback()) {

    inner class ViewHolder(private val binding: RvItemNoticeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(noticeItem: FireStoreNotice) {
            binding.apply {
                Log.d("TAG", "bind: ${noticeItem.title}")
                Log.d("TAG", "bind: ${noticeItem.date}")
                tvTitle.text = noticeItem.title
                tvContent.text = noticeItem.content
                tvDate.text = noticeItem.date

                ivNewNotice.visibility = if (noticeItem.isNew) View.VISIBLE else View.GONE

                itemView.setOnClickListener {
                    if (tvContent.visibility == View.VISIBLE) {
                        tvContent.visibility = View.GONE
                    } else {
                        tvContent.visibility = View.VISIBLE
                    }
                    if (noticeItem.isNew) {
                        noticeItem.isNew = false
                        ivNewNotice.visibility = View.GONE
                        itemClickListener(noticeItem)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RvItemNoticeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class NoticeDiffCallback : DiffUtil.ItemCallback<FireStoreNotice>() {
    override fun areItemsTheSame(oldItem: FireStoreNotice, newItem: FireStoreNotice): Boolean {
        Log.d("TAG", "areItemsTheSame: ${newItem.title}")
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: FireStoreNotice, newItem: FireStoreNotice): Boolean {
        Log.d("TAG", "areContentsTheSame: ${newItem.title}")
        return oldItem == newItem
    }
}