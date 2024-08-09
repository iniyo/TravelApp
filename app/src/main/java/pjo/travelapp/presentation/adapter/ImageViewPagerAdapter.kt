package pjo.travelapp.presentation.adapter

import android.graphics.Bitmap
import android.provider.ContactsContract.Data
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pjo.travelapp.R
import pjo.travelapp.data.entity.Photo
import pjo.travelapp.databinding.VpItemSlideImgBinding
import pjo.travelapp.presentation.ui.consts.DataType

class ImageViewPagerAdapter(
    private var currentDataType: DataType = DataType.PLACE_DETAIL
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var bitmaps: List<Bitmap> = emptyList()
    private var photos: List<Photo> = emptyList()
    private var dummys: List<Int> = emptyList()

    inner class PlaceDetailViewHolder(private val binding: VpItemSlideImgBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Bitmap) {
            binding.apply {
                try {
                    Log.d("TAG", "bind: Bitmap $item")
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
                    Log.d("TAG", "bind: Photo $item")
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

    inner class DummyViewHolder(private val binding: VpItemSlideImgBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Int) {
            binding.apply {
                try {
                    Log.d("TAG", "bind: Photo $item")
                    ivImgContainer.setImageResource(item)
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
            DataType.DUMMY -> DummyViewHolder(binding)
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
            DataType.DUMMY -> dummys.size
        }
    }

    fun submitBitmaps(newBitmaps: List<Bitmap>) {
        currentDataType = DataType.PLACE_DETAIL
        bitmaps = newBitmaps
        notifyDataSetChanged()
    }

    fun submitPhotos(newPhotos: List<Photo>) {
        currentDataType = DataType.PLACE_RESULT
        photos = newPhotos
        notifyDataSetChanged()
    }

    fun submitDummy(newDummy: List<Int>) {
        currentDataType = DataType.DUMMY
        dummys = newDummy
        notifyDataSetChanged()
    }
}
