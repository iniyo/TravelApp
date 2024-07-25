package pjo.travelapp.presentation.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.libraries.places.api.model.Place
import pjo.travelapp.R
import pjo.travelapp.data.entity.Category
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.databinding.RvCategoryItemBinding

class CategoryRecyclerAdapter(
    private val itemClickListener: (LinearLayout) -> Unit
) : RecyclerView.Adapter<CategoryRecyclerAdapter.ViewHolder>() {

    private val category = Category()
    private val imgList: List<Int> = category.getImgList()
    private val titleList: List<String> = category.getTitleList()

    inner class ViewHolder(private val binding: RvCategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imgUrl: Int, title: String) {

            binding.apply {
                ivPic.setImageResource(imgUrl)
                tvTitle.text = title
                itemView.setOnClickListener { itemClickListener(llCategoryMainContainer) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RvCategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imgList[position % imgList.size], titleList[position % titleList.size])
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun getItemCount(): Int = imgList.size

}