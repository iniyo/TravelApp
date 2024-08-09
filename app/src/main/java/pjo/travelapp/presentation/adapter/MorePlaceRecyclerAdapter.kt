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
    private val hotelItemClickListener: (HotelCard) -> Unit,
    private val placeResultClickListener: (PlaceResult) -> Unit // PlaceResult 클릭 리스너 추가
) : ListAdapter<Any, RecyclerView.ViewHolder>(DiffCallback()) {

    enum class DataType {
        PLACE, HOTEL, PLACERESULT // PLACERESULT 타입 추가
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
                    ivMainContent.setImageResource(R.drawable.intro_pic)
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

    // 새로운 ViewHolder 추가
    inner class PlaceResultViewHolder(private val binding: RvMorePlacesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PlaceResult) {
            binding.apply {
                try {
                    ivMainContent.setImageResource(R.drawable.intro_pic) // 기본 이미지
                    tvTitle.text = item.name // PlaceResult의 제목
                    tvRating.text = item.rating.toString() // PlaceResult의 평점
                    rbScore.rating = item.rating.toFloat()
                    tvReviews.text = item.reviews?.joinToString("\n")

                    itemView.setOnClickListener { placeResultClickListener(item) }
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
            DataType.PLACERESULT -> PlaceResultViewHolder(binding) // 새로운 ViewHolder 생성
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PlaceViewHolder -> holder.bind(getItem(position) as PlaceDetail)
            is HotelViewHolder -> holder.bind(getItem(position) as HotelCard)
            is PlaceResultViewHolder -> holder.bind(getItem(position) as PlaceResult) // 새로운 바인딩 로직 추가
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

    // 새로운 submit 함수 추가
    fun submitPlaceResults(placeResults: List<PlaceResult>) {
        currentDataType = DataType.PLACERESULT
        submitList(placeResults)
    }
}
class DiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            oldItem is PlaceDetail && newItem is PlaceDetail -> oldItem.place.id == newItem.place.id
            oldItem is HotelCard && newItem is HotelCard -> oldItem.id == newItem.id
            oldItem is PlaceResult && newItem is PlaceResult -> oldItem.placeId == newItem.placeId
            else -> false
        }
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return oldItem == newItem
    }
}
