package pjo.travelapp.presentation.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.Place
import pjo.travelapp.R
import pjo.travelapp.databinding.RvMorePlacesItemBinding
import pjo.travelapp.databinding.RvScheduleItemBinding

class ScheduleDefaultAdapter :
    RecyclerView.Adapter<ScheduleDefaultAdapter.ViewHolder>() {

    private val placesWithPhotos = mutableListOf<Pair<Place, Bitmap?>>()

    inner class ViewHolder(private val binding: RvScheduleItemBinding) :
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvScheduleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(placesWithPhotos[position])
    }

    override fun getItemCount(): Int = placesWithPhotos.size

    fun addPlace(placeWithPhoto: Pair<Place, Bitmap?>) {
        placesWithPhotos.add(placeWithPhoto)

        notifyItemInserted(placesWithPhotos.size - 1)
    }
}
