package pjo.travelapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import pjo.travelapp.data.entity.Category
import pjo.travelapp.databinding.RvCategoryItemBinding

class CategoryRecyclerAdapter(
    private val itemClickListener: (String) -> Unit
) : ListAdapter<CategoryRecyclerAdapter.CategoryItem, CategoryRecyclerAdapter.ViewHolder>(CategoryDiffCallback()) {

    private val category = Category()
    private val imgList: List<Int> = category.getImgList()
    private val titleList: List<String> = category.getTitleList()

    init {
        val categoryItems = imgList.mapIndexed { index, imgResId ->
            CategoryItem(imgResId, titleList[index % titleList.size])
        }
        submitList(categoryItems)
    }

    inner class ViewHolder(private val binding: RvCategoryItemBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryItem) {
            binding.apply {
                ivPic.setImageResource(item.imgResId)
                tvTitle.text = item.title
                itemView.setOnClickListener { itemClickListener(item.title) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RvCategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    data class CategoryItem(val imgResId: Int, val title: String)

    class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryItem>() {
        override fun areItemsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
            // 각각의 아이템들이 같은지 여부를 비교
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
            // 아이템의 내용이 같은지 여부를 비교
            return oldItem == newItem
        }
    }
}
