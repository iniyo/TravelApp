package pjo.travelapp.presentation.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.libraries.places.api.model.Place
import pjo.travelapp.R
import pjo.travelapp.data.entity.HotelCard
import pjo.travelapp.databinding.RvMorePlacesItemBinding

class MorePlaceRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val placesWithPhotos = mutableListOf<Pair<Place, Bitmap?>>()
    private val hotels = mutableListOf<HotelCard>()
    private var currentDataType: DataType = DataType.PLACE

    enum class DataType {
        PLACE, HOTEL
    }

    inner class PlaceViewHolder(private val binding: RvMorePlacesItemBinding) :
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

    inner class HotelViewHolder(private val binding: RvMorePlacesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HotelCard) {
            binding.apply {
                try {
                    tvSatisfaction.visibility = View.VISIBLE
                    ivMainContent.setImageResource(R.drawable.intro_pic) // 기본 이미지
                    tvTitle.text = item.name
                    tvRating.text = item.stars
                    Glide.with(root)
                        .load(item.images.first())
                        .error(R.drawable.svg_hotel)
                        .into(ivMainContent)
                    rbScore.rating = item.stars.toFloat()
                    tvReviews.text = item.reviewsSummary.scoreDesc

                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            RvMorePlacesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return when (currentDataType) {
            DataType.PLACE -> PlaceViewHolder(binding)
            DataType.HOTEL -> HotelViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PlaceViewHolder -> holder.bind(placesWithPhotos[position])
            is HotelViewHolder -> holder.bind(hotels[position])
        }
    }

    override fun getItemCount(): Int {
        return when (currentDataType) {
            DataType.PLACE -> placesWithPhotos.size
            DataType.HOTEL -> hotels.size
        }
    }

    fun addPlace(placeWithPhoto: Pair<Place, Bitmap?>) {
        currentDataType = DataType.PLACE
        placesWithPhotos.add(placeWithPhoto)
        notifyItemInserted(placesWithPhotos.size - 1)
    }

    fun addHotel(hotel: HotelCard) {
        currentDataType = DataType.HOTEL
        hotels.add(hotel)
        notifyItemInserted(hotels.size - 1)
    }
}
