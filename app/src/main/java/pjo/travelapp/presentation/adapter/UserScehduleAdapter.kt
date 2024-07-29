package pjo.travelapp.presentation.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pjo.travelapp.data.entity.UserSchduleEntity
import pjo.travelapp.databinding.VpUserSchduleBinding
import pjo.travelapp.presentation.util.extension.setRandomGradientBackground

class UserScehduleAdapter(
    private val itemClickList: (UserSchduleEntity) -> Unit,
    private val deleteClickList: (UserSchduleEntity) -> Unit
) :
    ListAdapter<UserSchduleEntity, UserScehduleAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: VpUserSchduleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserSchduleEntity, position: Int) {
            try {
                binding.apply {
                    // UserSchduleEntity의 place 리스트를 순회하면서 이미지뷰를 동적으로 추가합니다.
                    val bitmaps = item.place.map { pair ->
                        BitmapFactory.decodeResource(binding.root.context.resources, pair.second)
                    }
                    scivImgContainer.setBitmaps(bitmaps)

                    tvTravelPeriod.text = item.place.joinToString(separator = ",") {
                        it.first
                    }
                    tvTravelDate.text = item.datePeriod

                    ivDelete.setOnClickListener { deleteClickList(item) }
                    itemView.setOnClickListener { itemClickList(item) }

                    if(position > 0) {
                        setRandomGradientBackground(clMainContainer)
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            VpUserSchduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    // DiffUtil.ItemCallback 구현
    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<UserSchduleEntity>() {
            override fun areItemsTheSame(
                oldItem: UserSchduleEntity,
                newItem: UserSchduleEntity
            ): Boolean {
                // 아이템 고유 ID로 비교 (예: placeId)
                return oldItem === newItem
            }

            override fun areContentsTheSame(
                oldItem: UserSchduleEntity,
                newItem: UserSchduleEntity
            ): Boolean {
                // 아이템의 내용이 같은지 비교
                return oldItem == newItem
            }
        }
    }
}