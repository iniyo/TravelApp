package pjo.travelapp.presentation.adapter

import android.util.Log
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
        fun bind(itemList: PlaceResult) {
            try {
                if (itemList.name.isNotEmpty()) {
                    binding.tvSearchListItemTitle.text = itemList.name
                    binding.tvRating.text = itemList.rating.toString()
                    binding.rbScore.rating = itemList.rating.toFloat()

                    val photoUrl = itemList.photos.firstOrNull()?.getPhotoUrl()
                    if (photoUrl != null) {
                        Glide.with(binding.root)
                            .load(photoUrl)
                            .error(R.drawable.ic_launcher_foreground)
                            .placeholder(R.drawable.img_bg_title)
                            .into(binding.sivSearchListItem)
                    } else {
                        binding.sivSearchListItem.setImageResource(R.drawable.ic_launcher_foreground) // 기본 이미지 설정
                    }

                    itemView.setOnClickListener { itemClickListener(itemList) }
                } else {
                    // 아이템 이름이 비어있을 경우 처리 (빈 리스트의 경우)
                    binding.tvSearchListItemTitle.text = "No result"
                    binding.tvRating.text = "-"
                    binding.rbScore.rating = 0f
                    binding.sivSearchListItem.setImageResource(R.drawable.ic_launcher_foreground) // 기본 이미지 설정
                    itemView.setOnClickListener(null) // 클릭 이벤트 제거
                }
            } catch (e: NullPointerException) {
                Log.e("error tag", "bind: ${e.printStackTrace()}")
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            RvMapsSearchListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(predictions[position])
    }

    override fun getItemCount(): Int = predictions.size

    fun updateData(newPredictions: List<PlaceResult>) {
        predictions = newPredictions
        notifyDataSetChanged()
    }
}
