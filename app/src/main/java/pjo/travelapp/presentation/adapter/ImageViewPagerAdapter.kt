package pjo.travelapp.presentation.adapter

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pjo.travelapp.R
import pjo.travelapp.data.entity.Photo
import pjo.travelapp.databinding.VpItemSlideImgBinding
import kotlin.math.log

class ImageViewPagerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val bitmaps = mutableListOf<Bitmap>()
    private val photos = mutableListOf<Photo>()
    private var currentDataType: DataType = DataType.PLACE_DETAIL

    enum class DataType {
        PLACE_RESULT, PLACE_DETAIL
    }

    inner class PlaceDetailViewHolder(private val binding: VpItemSlideImgBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Bitmap) {
            binding.apply {
                try {
                    ivImgContainer.setImageBitmap(item)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

    inner class PlaceResultViewHolder(private val binding: VpItemSlideImgBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Photo) {
            binding.apply {
                try {
                    Glide.with(root)
                        .load(item.getPhotoUrl())
                        .error(com.android.car.ui.R.drawable.car_ui_icon_error)
                        .placeholder(R.drawable.img_bg_title)
                        .into(ivImgContainer)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            VpItemSlideImgBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return when(currentDataType) {
            DataType.PLACE_DETAIL -> PlaceDetailViewHolder(binding)
            DataType.PLACE_RESULT -> PlaceResultViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is PlaceDetailViewHolder -> holder.bind(bitmaps[position])
            is PlaceResultViewHolder -> holder.bind(photos[position])
        }
    }

    override fun getItemCount(): Int {
        return when(currentDataType) {
            DataType.PLACE_DETAIL -> bitmaps.size
            DataType.PLACE_RESULT -> photos.size
        }

    }

    fun setBitmaps(newBitmaps: List<Bitmap>) {
        bitmaps.clear()
        bitmaps.addAll(newBitmaps)
        notifyDataSetChanged()
    }

    fun setPhotos(newPhoto: List<Photo>) {
        photos.clear()
        photos.addAll(newPhoto)
        notifyDataSetChanged()
    }
}
