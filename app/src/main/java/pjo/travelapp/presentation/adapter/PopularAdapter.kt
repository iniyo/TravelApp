package pjo.travelapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pjo.travelapp.databinding.RecycleCategoryItemBinding
import pjo.travelapp.databinding.RecyclePopularItemBinding

class PopularAdapter(
    private val imgList: List<Int>
) : RecyclerView.Adapter<PopularAdapter.ViewHolder>() {

    class ViewHolder(private val binding: RecyclePopularItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imgUrl: Int) {

            binding.apply {

                sivPopularItemMainImg.setImageResource(imgUrl)
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
        val binding =
            RecyclePopularItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imgList[position % imgList.size])
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun getItemCount(): Int = imgList.size

}