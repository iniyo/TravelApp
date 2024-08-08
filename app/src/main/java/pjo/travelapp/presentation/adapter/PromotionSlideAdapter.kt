package pjo.travelapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pjo.travelapp.databinding.VpMainTopSlideItemBinding

class PromotionSlideAdapter : ListAdapter<Int, PromotionSlideAdapter.ViewHolder>(DiffCallback()) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            VpMainTopSlideItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: VpMainTopSlideItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imgUrl: Int) {
            binding.apply {
                ivTopSlide.setImageResource(imgUrl)
                /* Glide.with(root.context)
                     .load(imgUrl)
                     .skipMemoryCache(false)
                     .placeholder(R.drawable.intro_pic)
                     .into(ivTopSlide)*/
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        Glide.with(holder.itemView.context).clear(holder.itemView)
    }

    // DiffUtil.ItemCallback 구현
    class DiffCallback : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }
    }

    // 아이템 추가 메서드 변경
    fun addAd(item: Int) {
        val currentList = currentList.toMutableList()
        currentList.add(item)
        submitList(currentList)
    }
}
