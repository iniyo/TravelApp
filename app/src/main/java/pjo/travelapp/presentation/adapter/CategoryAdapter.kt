package pjo.travelapp.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pjo.travelapp.R
import pjo.travelapp.data.CategoryItem
import pjo.travelapp.databinding.CategoryItemBinding

class CategoryAdapter(
    private val imgList: List<Int>
): RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var selectedItem: Int = -1

    class ViewHolder(private val binding: CategoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imgUrl: Int, position: Int, selectedItem:Int) {

            binding.apply {

                ivPic.setImageResource(imgUrl)
                /*Glide.with(root.context)
                    .load(imgUrl)
                    .skipMemoryCache(false)
                    .placeholder(R.drawable.intro_pic)
                    .into(ivPic)

                // 선택된 아이템인 경우 배경 변경
                if (position == selectedItem) {
                    llCategoryMainContainer.isSelected = true
                    ivPic.setImageResource(0)
                    tvTitle.visibility = View.VISIBLE
                } else {
                    llCategoryMainContainer.isSelected = false
                    ivPic.setImageResource(R.drawable.bg_gray_corner)
                    tvTitle.visibility = View.GONE
                }*/
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imgList[position % imgList.size], position, selectedItem)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun getItemCount(): Int = imgList.size

}