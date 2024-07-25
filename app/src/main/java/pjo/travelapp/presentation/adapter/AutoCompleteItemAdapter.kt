package pjo.travelapp.presentation.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pjo.travelapp.R
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.databinding.RvMapsSearchListBinding

class AutoCompleteItemAdapter(
    private val itemClickListener: (PlaceResult) -> Unit
) : ListAdapter<PlaceResult, AutoCompleteItemAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: RvMapsSearchListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PlaceResult?) {
            try {
                binding.apply {
                    if (item != null) {
                        Log.d("TAG", "AutoCompleteItemAdapter bind: $item")
                        tvSearchListItemTitle.text = item.name
                        tvRating.text = item.rating.toString()
                        rbScore.rating = item.rating.toFloat()
                        tvSearchListItemInfo.text = item.vicinity

                        val photoUrl = item.photos?.firstOrNull()?.getPhotoUrl()
                        if (photoUrl != null) {
                            Glide.with(root)
                                .load(photoUrl)
                                .error(R.drawable.img_bg_title)
                                .placeholder(R.drawable.img_bg_title)
                                .into(sivSearchListItem)
                        } else {
                            sivSearchListItem.setImageResource(R.drawable.img_bg_title)
                        }
                        itemView.setOnClickListener { itemClickListener(item) }
                    } else {
                        // 아이템이 null일 경우 기본값 설정
                        tvSearchListItemTitle.text = "정보 없음"
                        tvRating.text = "-"
                        rbScore.rating = 0f
                        tvSearchListItemInfo.text = "정보 없음"
                        sivSearchListItem.setImageResource(R.drawable.ic_launcher_foreground) // 기본 이미지 설정
                        itemView.setOnClickListener(null) // 클릭 이벤트 제거
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RvMapsSearchListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // DiffUtil.ItemCallback 구현
    companion object{
        private val diffUtil = object: DiffUtil.ItemCallback<PlaceResult>() {
            override fun areItemsTheSame(oldItem: PlaceResult, newItem: PlaceResult): Boolean {
                // 아이템 고유 ID로 비교 (예: placeId)
                return oldItem.placeId === newItem.placeId
            }

            override fun areContentsTheSame(oldItem: PlaceResult, newItem: PlaceResult): Boolean {
                // 아이템의 내용이 같은지 비교
                return oldItem == newItem
            }
        }
    }
}
