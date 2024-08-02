package pjo.travelapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pjo.travelapp.data.entity.UserPlan
import pjo.travelapp.databinding.VpUserSchduleBinding
import pjo.travelapp.presentation.util.BitmapUtil
import pjo.travelapp.presentation.util.setRandomGradientBackground

class UserScheduleAdapter(
    private val itemClickList: (UserPlan) -> Unit,
    private val deleteClickList: (UserPlan) -> Unit
) :
    ListAdapter<UserPlan, UserScheduleAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: VpUserSchduleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserPlan, position: Int) {
            try {
                binding.apply {
                    val context = itemView.context
                    val bitmapUtil = BitmapUtil(context)

                    val bitmaps = item.placeAndPhotoPaths.map { pair ->
                        bitmapUtil.loadBitmap(pair.second)
                    }

                    scivImgContainer.setBitmaps(bitmaps)

                    tvTravelPeriod.text = item.placeAndPhotoPaths.joinToString(separator = ",") {
                        it.first
                    }
                    tvTravelDate.text = item.datePeriod

                    ivDelete.setOnClickListener { deleteClickList(item) }
                    itemView.setOnClickListener { itemClickList(item) }

                    if (position > 0) {
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
        private val diffUtil = object : DiffUtil.ItemCallback<UserPlan>() {
            override fun areItemsTheSame(
                oldItem: UserPlan,
                newItem: UserPlan
            ): Boolean {
                // 아이템 고유 ID로 비교 (예: placeId)
                return oldItem === newItem
            }

            override fun areContentsTheSame(
                oldItem: UserPlan,
                newItem: UserPlan
            ): Boolean {
                // 아이템의 내용이 같은지 비교
                return oldItem == newItem
            }
        }
    }
}