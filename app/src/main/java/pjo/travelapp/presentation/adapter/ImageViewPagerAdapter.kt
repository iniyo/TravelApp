package pjo.travelapp.presentation.adapter

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pjo.travelapp.R
import pjo.travelapp.data.entity.Photo
import pjo.travelapp.databinding.VpItemSlideImgBinding
import pjo.travelapp.presentation.ui.consts.DataType
import kotlin.math.log

class ImageViewPagerAdapter(
    private var currentDataType: DataType = DataType.PLACE_DETAIL
) : ListAdapter<Any, RecyclerView.ViewHolder>(diffCallback(currentDataType)) {

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
            is PlaceDetailViewHolder -> holder.bind(getItem(position) as Bitmap)
            is PlaceResultViewHolder -> holder.bind(getItem(position) as Photo)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return currentDataType.ordinal
    }

    fun submitBitmaps(newBitmaps: List<Bitmap>) {
        currentDataType = DataType.PLACE_DETAIL
        submitList(newBitmaps)
    }

    fun submitPhotos(newPhotos: List<Photo>) {
        currentDataType = DataType.PLACE_RESULT
        submitList(newPhotos)
    }

    companion object {
        fun diffCallback(dataType: DataType): DiffUtil.ItemCallback<Any> {
            return when (dataType) {
                DataType.PLACE_DETAIL -> BitmapDiffCallback() as DiffUtil.ItemCallback<Any>
                DataType.PLACE_RESULT -> PhotoDiffCallback() as DiffUtil.ItemCallback<Any>
            }
        }
    }
}
class BitmapDiffCallback : DiffUtil.ItemCallback<Bitmap>() {
    override fun areItemsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
        return oldItem.sameAs(newItem)
    }
}

class PhotoDiffCallback : DiffUtil.ItemCallback<Photo>() {
    override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
        return oldItem.photoReference == newItem.photoReference // assuming Photo has an id field
    }

    override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
        return oldItem == newItem
    }
}
