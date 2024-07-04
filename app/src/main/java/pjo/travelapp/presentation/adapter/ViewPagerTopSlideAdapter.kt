package pjo.travelapp.presentation.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pjo.travelapp.R
import pjo.travelapp.databinding.VpMainTopSlideItemBinding

class ViewPagerTopSlideAdapter(
    private val imgList: List<String>
) : RecyclerView.Adapter<ViewPagerTopSlideAdapter.ViewHolder>() {


    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            VpMainTopSlideItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imgList[position % imgList.size])
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    class ViewHolder(private val binding: VpMainTopSlideItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imgUrl: String) {

            binding.apply {

                Log.d("TAG", "bind: $imgUrl")
                Glide.with(root.context)
                    .load(imgUrl)
                    .skipMemoryCache(false)
                    .placeholder(R.drawable.intro_pic)
                    .into(ivTopSlide)

            }
        }
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        Glide.with(holder.itemView.context).clear(holder.itemView)
    }

    override fun getItemCount(): Int = imgList.size
}
