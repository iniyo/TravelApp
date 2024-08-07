package pjo.travelapp.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pjo.travelapp.databinding.RvSearchPlaceItemBinding

class PlaceSelectAdapter(
    private val itemClickListener: (Pair<String, Int>) -> Unit
) : RecyclerView.Adapter<PlaceSelectAdapter.ViewHolder>() {

    private var imgList: List<Int> = listOf()
    private var titleList: List<String> = listOf()
    private var subTitleList: List<String> = listOf()

    inner class ViewHolder(private val binding: RvSearchPlaceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(img: Int, title: String, position: Int) {
            binding.apply {
                sivLittlePlacePic.setImageResource(img)
                tvPlaceName.text = title
                tvPlaceType.text = subTitleList[position]
                itemView.setOnClickListener { itemClickListener(Pair(title, img)) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvSearchPlaceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            imgList[position % imgList.size],
            titleList[position % titleList.size],
            position
        )
    }

    override fun getItemCount(): Int = imgList.size

    @SuppressLint("NotifyDataSetChanged")
    fun setData(imgList: List<Int>, titleList: List<String>, subTitleList: List<String>) {
        this.imgList = imgList
        this.titleList = titleList
        this.subTitleList = subTitleList
        notifyDataSetChanged()
    }
}
