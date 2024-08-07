package pjo.travelapp.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pjo.travelapp.R
import pjo.travelapp.data.entity.HotelCard
import pjo.travelapp.data.entity.PlaceDetail
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.databinding.RvMorePlacesItemBinding

class MorePlaceRecyclerAdapter(
    private val placeItemClickListener: (PlaceDetail) -> Unit,
    private val hotelItemClickListener: (HotelCard) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(DiffCallback()) {

    enum class DataType {
        PLACE, HOTEL
    }

    private var currentDataType: DataType = DataType.PLACE

    inner class PlaceViewHolder(private val binding: RvMorePlacesItemBinding) :
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
                        tvReviews.text = reviews[0].text ?: "No Reviews"
                    } else {
                        tvReviews.text = "No Reviews"
                    }

                    itemView.setOnClickListener { placeItemClickListener(item) }

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

                    itemView.setOnClickListener { hotelItemClickListener(item) }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RvMorePlacesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return when (currentDataType) {
            DataType.PLACE -> PlaceViewHolder(binding)
            DataType.HOTEL -> HotelViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PlaceViewHolder -> holder.bind(getItem(position) as PlaceDetail)
            is HotelViewHolder -> holder.bind(getItem(position) as HotelCard)
        }
    }

    fun submitPlaces(places: List<PlaceDetail>) {
        currentDataType = DataType.PLACE
        submitList(places)
    }

    fun submitHotels(hotels: List<HotelCard>) {
        currentDataType = DataType.HOTEL
        submitList(hotels)
    }
}
class DiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return if (oldItem is PlaceDetail && newItem is PlaceDetail) {
            oldItem.place.id == newItem.place.id
        } else if (oldItem is HotelCard && newItem is HotelCard) {
            oldItem.id == newItem.id
        } else {
            false
        }
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return oldItem == newItem
    }
}