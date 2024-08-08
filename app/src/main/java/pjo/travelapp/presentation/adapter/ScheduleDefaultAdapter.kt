package pjo.travelapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pjo.travelapp.R
import pjo.travelapp.data.entity.PlaceDetail
import pjo.travelapp.databinding.RvScheduleItemBinding

class ScheduleDefaultAdapter(
    private val itemClickListener: (PlaceDetail) -> Unit,
) : ListAdapter<PlaceDetail, ScheduleDefaultAdapter.ViewHolder>(PlaceDetailDiffCallback()) {

    inner class ViewHolder(private val binding: RvScheduleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PlaceDetail) {
            binding.apply {
                try {
                    item.bitmap?.let {
                        ivMainContent.setImageBitmap(it.first())
                    } ?: ivMainContent.setImageResource(R.drawable.intro_pic)

                    tvTitle.text = item.place.name ?: "Unknown Place"
                    tvRating.text = item.place.rating?.toString() ?: "No Rating"
                    rbScore.rating = item.place.rating?.toFloat() ?: 0f
                    val reviews = item.place.reviews
                    if (reviews != null && reviews.isNotEmpty()) {
                        // 첫 번째 리뷰의 텍스트를 가져옴
                        tvReviews.text = reviews[0].text ?: "No Reviews"
                    } else {
                        tvReviews.text = "No Reviews"
                    }

                    itemView.setOnClickListener {
                        itemClickListener(item)
                    }

                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RvScheduleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // DiffUtil.Callback을 사용하여 데이터 변경을 효율적으로 처리
    class PlaceDetailDiffCallback : DiffUtil.ItemCallback<PlaceDetail>() {
        override fun areItemsTheSame(oldItem: PlaceDetail, newItem: PlaceDetail): Boolean {
            return oldItem.place.id == newItem.place.id // 고유 식별자 비교
        }

        override fun areContentsTheSame(oldItem: PlaceDetail, newItem: PlaceDetail): Boolean {
            return oldItem == newItem // 데이터 클래스의 equals 메서드 사용
        }
    }
}

