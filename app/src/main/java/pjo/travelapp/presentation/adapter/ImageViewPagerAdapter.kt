package pjo.travelapp.presentation.adapter

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pjo.travelapp.databinding.VpItemSlideImgBinding
import kotlin.math.log

class ImageViewPagerAdapter : RecyclerView.Adapter<ImageViewPagerAdapter.ViewHolder>() {

    private val bitmaps = mutableListOf<Bitmap>()

    inner class ViewHolder(private val binding: VpItemSlideImgBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Bitmap) {
            binding.apply {
                try {
                    Log.d("TAG", "bind: $item")
                    ivImgContainer.setImageBitmap(item)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            VpItemSlideImgBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(bitmaps[position])
    }

    override fun getItemCount(): Int = bitmaps.size

    fun addBitmap(bitmap: Bitmap) {
        bitmaps.add(bitmap)
        notifyItemInserted(bitmaps.size - 1)
    }

    fun setBitmaps(newBitmaps: List<Bitmap>) {
        bitmaps.clear()
        bitmaps.addAll(newBitmaps)
        notifyDataSetChanged()
    }
}
