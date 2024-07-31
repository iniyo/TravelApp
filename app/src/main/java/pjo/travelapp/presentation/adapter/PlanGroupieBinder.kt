package pjo.travelapp.presentation.adapter

import android.view.View
import com.bumptech.glide.Glide
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.viewbinding.BindableItem
import pjo.travelapp.R
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.databinding.ItemChildPlaceBinding
import pjo.travelapp.databinding.RvPlanItemBinding

class ParentCommentItem(
    val item: Pair<Int, Int>,
    private val noteClickListener: () -> Unit,
    private val placeClickListener: (Int) -> Unit
): BindableItem <RvPlanItemBinding>(),
    ExpandableItem {

    // 자식 그룹
    private lateinit var childCommentGroup: ExpandableGroup

    // set layout
    override fun getLayout(): Int = R.layout.rv_plan_item

    // viewholder
    override fun bind(viewBinding: RvPlanItemBinding, position: Int) {
        try {
            viewBinding.apply {
                val stringBuilder = StringBuilder()
                stringBuilder.apply {
                    append(position.inc().toString())
                    append("일차 ")
                    append(" / ")
                    append(item.first.toString())
                    append("월 ")
                    append(item.second.toString())
                    append("일")
                }
                tvDate.text = stringBuilder

                btnNote.setOnClickListener { noteClickListener() }
                btnSelectPlace.setOnClickListener { placeClickListener(position) }
                root.setOnClickListener { childCommentGroup.onToggleExpanded() }
                childCommentGroup.onToggleExpanded()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // View Binding 객체 초기화
    override fun initializeViewBinding(view: View) = RvPlanItemBinding.bind(view)

    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        this.childCommentGroup = onToggleListener
    }
}

class ChildCommentItem(
    val item: PlaceResult
): BindableItem<ItemChildPlaceBinding>() {
    override fun getLayout(): Int = R.layout.item_child_place

    override fun bind(viewBinding: ItemChildPlaceBinding, position: Int) {
        viewBinding.apply {
            val photoUrl = item.photos?.firstOrNull()?.getPhotoUrl()
            if (photoUrl != null) {
                Glide.with(root)
                    .load(photoUrl)
                    .error(R.drawable.img_bg_title)
                    .placeholder(R.drawable.img_bg_title)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.img_bg_title)
            }
            tvTitle.text = item.name
            item.reviews?.let {
                tvReviews.text = item.reviews.joinToString { "\n" }
            }
        }
    }

    override fun initializeViewBinding(view: View): ItemChildPlaceBinding =
        ItemChildPlaceBinding.bind(view)

}
