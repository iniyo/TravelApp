package pjo.travelapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pjo.travelapp.R
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.databinding.RvMapsSearchListBinding

class AutoCompleteItemAdapter(
    private var predictions: List<PlaceResult>,
    private val itemClickListener: (PlaceResult) -> Unit
) : RecyclerView.Adapter<AutoCompleteItemAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: RvMapsSearchListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PlaceResult?) {
            try {
                binding.apply {
                    if (item != null) {
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
                        }else {
                            sivSearchListItem.setImageResource(R.drawable.img_bg_title) //
                        }
                        itemView.setOnClickListener { itemClickListener(item) }
                    }
                    else {
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
        holder.bind(predictions.getOrNull(position))
    }

    override fun getItemCount(): Int = predictions.size

    fun updateData(newPredictions: List<PlaceResult>?) {
        predictions = newPredictions ?: emptyList()
        notifyDataSetChanged()
    }
}
