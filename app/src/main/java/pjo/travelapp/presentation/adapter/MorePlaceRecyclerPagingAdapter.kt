package pjo.travelapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import pjo.travelapp.R
import pjo.travelapp.data.entity.PlaceDetail
import pjo.travelapp.databinding.RvMorePlacesItemBinding

class MorePlaceRecyclerPagingAdapter :
    PagingDataAdapter<PlaceDetail, MorePlaceRecyclerPagingAdapter.PlaceViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<PlaceDetail>()
    {
        override fun areItemsTheSame(
            oldItem: PlaceDetail,
            newItem: PlaceDetail
        ): Boolean {
            return oldItem.place.id == newItem.place.id
        }

        override fun areContentsTheSame(
            oldItem: PlaceDetail,
            newItem: PlaceDetail
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding =
            RvMorePlacesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val placeWithPhoto = getItem(position)
        placeWithPhoto?.let {
            holder.bind(it)
        }
    }

    class PlaceViewHolder(private val binding: RvMorePlacesItemBinding) :
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

                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }
}
