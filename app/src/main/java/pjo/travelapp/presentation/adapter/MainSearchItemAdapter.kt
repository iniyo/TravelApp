package pjo.travelapp.presentation.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pjo.travelapp.R
import pjo.travelapp.data.entity.PlaceDetail
import pjo.travelapp.databinding.RvSearchPlaceItemBinding

class MainSearchItemAdapter(
    private val itemClickListener: (PlaceDetail) -> Unit
) : ListAdapter<PlaceDetail, MainSearchItemAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: RvSearchPlaceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PlaceDetail?) {
            try {
                binding.apply {
                    if (item != null) {
                        Log.d("TAG", "AutoCompleteItemAdapter bind: $item")
                        item.bitmap?.let {
                            sivLittlePlacePic.setImageBitmap(it.first())
                        } ?: run {
                            sivLittlePlacePic.setImageResource(R.drawable.ic_launcher_foreground)
                        }
                        tvPlaceName.text = item.place.name
                        tvSpot.text = item.place.address

                        itemView.setOnClickListener { itemClickListener(item) }
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RvSearchPlaceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // DiffUtil.ItemCallback 구현
    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<PlaceDetail>() {
            override fun areItemsTheSame(
                oldItem: PlaceDetail,
                newItem: PlaceDetail
            ): Boolean {
                // 아이템 고유 ID로 비교 (예: placeId)
                return oldItem.place.id == newItem.place.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: PlaceDetail,
                newItem: PlaceDetail
            ): Boolean {
                // 아이템의 내용이 같은지 비교
                return oldItem === newItem
            }
        }
    }
}
