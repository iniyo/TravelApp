package pjo.travelapp.presentation.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.google.android.libraries.places.api.model.Place
import pjo.travelapp.R
import pjo.travelapp.databinding.RvSearchPlaceItemBinding

class MainSearchItemAdapter(
    private val itemClickListener: (Place) -> Unit
) : ListAdapter<Pair<Place, Bitmap?>, MainSearchItemAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: RvSearchPlaceItemBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Pair<Place, Bitmap?>) {
            try {
                binding.apply {
                    if (item != null) {
                        Log.d("TAG", "AutoCompleteItemAdapter bind: $item")
                        item.second?.let {
                            ivLittlePlacePic.setImageBitmap(it)
                        } ?: run {
                            ivLittlePlacePic.setImageResource(R.drawable.ic_launcher_foreground)
                        }

                        tvPlaceName.text = item.first.name
                        tvSpot.text = item.first.address


                        itemView.setOnClickListener { itemClickListener(item.first) }
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
        private val diffUtil = object : DiffUtil.ItemCallback<Pair<Place, Bitmap?>>() {
            override fun areItemsTheSame(
                oldItem: Pair<Place, Bitmap?>,
                newItem: Pair<Place, Bitmap?>
            ): Boolean {
                // 아이템 고유 ID로 비교 (예: placeId)
                return oldItem.first.id == newItem.first.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: Pair<Place, Bitmap?>,
                newItem: Pair<Place, Bitmap?>
            ): Boolean {
                // 아이템의 내용이 같은지 비교
                return oldItem === newItem
            }
        }
    }
}
