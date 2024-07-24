package pjo.travelapp.presentation.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.Place
import pjo.travelapp.R
import pjo.travelapp.databinding.RvMorePlacesItemBinding

class MorePlaceRecyclerPagingAdapter :
    PagingDataAdapter<Pair<Place, Bitmap?>, MorePlaceRecyclerPagingAdapter.PlaceViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Pair<Place, Bitmap?>>() {
        override fun areItemsTheSame(
            oldItem: Pair<Place, Bitmap?>,
            newItem: Pair<Place, Bitmap?>
        ): Boolean {
            return oldItem.first.id == newItem.first.id
        }

        override fun areContentsTheSame(
            oldItem: Pair<Place, Bitmap?>,
            newItem: Pair<Place, Bitmap?>
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = RvMorePlacesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

        fun bind(item: Pair<Place, Bitmap?>) {
            binding.apply {
                try {
                    item.second?.let {
                        ivMainContent.setImageBitmap(it)
                    } ?: ivMainContent.setImageResource(R.drawable.intro_pic)

                    tvTitle.text = item.first.name ?: "Unknown Place"
                    tvRating.text = item.first.rating?.toString() ?: "No Rating"
                    rbScore.rating = item.first.rating?.toFloat() ?: 0f
                    val reviews = item.first.reviews
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
