package pjo.travelapp.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.databinding.RvPlanItemBinding

class PlanAdapter(
    private val itemClickListener: (View) -> Unit // click 시 전달할 아이템 설정.
) : ListAdapter<Pair<Int, Int>, PlanAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: RvPlanItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Pair<Int, Int>, position: Int) {
            try {
                binding.apply {
                    tvDate.text = position.inc().toString() + "일차 " + item.first.toString() + "월 " + item.second.toString() + "일"
                    itemView.setOnClickListener(null) // 클릭 이벤트 제거

                    ivDelete.setOnClickListener {

                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int {

        return super.getItemCount()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RvPlanItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    // DiffUtil.ItemCallback 구현
    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<Pair<Int, Int>>() {
            override fun areItemsTheSame(oldItem: Pair<Int, Int>, newItem: Pair<Int, Int>): Boolean {
                // 아이템 고유 ID로 비교 (예: placeId)
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Pair<Int, Int>, newItem: Pair<Int, Int>): Boolean {
                // 아이템의 내용이 같은지 비교
                return oldItem.first == newItem.first
            }
        }
    }
}