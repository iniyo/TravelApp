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
import pjo.travelapp.presentation.ui.consts.TAKE_REVIEWS


class ParentPlanItem(
    val item: Pair<Int, Int>,
    private val noteClickListener: () -> Unit,
    private val placeClickListener: (Int) -> Unit
) : BindableItem<RvPlanItemBinding>(), ExpandableItem {

    private lateinit var childCommentGroup: ExpandableGroup
    override fun getLayout(): Int = R.layout.rv_plan_item

    override fun getId(): Long = item.hashCode().toLong()

    override fun bind(viewBinding: RvPlanItemBinding, position: Int) {
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
        }
    }

    override fun initializeViewBinding(view: View) = RvPlanItemBinding.bind(view)

    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        this.childCommentGroup = onToggleListener
    }
}

class ChildPlanItem(
    val item: PlaceResult,
    val itemClickListener: (PlaceResult) -> Unit,
    private val parentGroup: ExpandableGroup
) : BindableItem<ItemChildPlaceBinding>() {

    override fun getLayout(): Int = R.layout.item_child_place

    override fun bind(viewBinding: ItemChildPlaceBinding, position: Int) {
        viewBinding.apply {
            val stringBuilder = StringBuilder()
            val photoUrl = item.photos?.firstOrNull()?.getPhotoUrl()
            if (photoUrl != null) {
                Glide.with(root)
                    .load(photoUrl)
                    .error(R.drawable.img_bg_title)
                    .placeholder(R.drawable.img_bg_title)
                    .into(sivMainItem)
            } else {
                sivMainItem.setImageResource(R.drawable.img_bg_title)
            }
            tvTitle.text = item.name
            tvRatingScore.text = item.rating.toString()
            rbScore.rating = item.rating.toFloat()
            item.reviews?.take(TAKE_REVIEWS)?.forEach { review ->
                tvReviews.text = stringBuilder.append("${review.authorName}: ${review.text}\n\n")
            }
            root.setOnClickListener {
                itemClickListener(item)
            }

            ivDelete.setOnClickListener {
                parentGroup.remove(this@ChildPlanItem)
            }
        }
    }

    override fun initializeViewBinding(view: View): ItemChildPlaceBinding =
        ItemChildPlaceBinding.bind(view)
}

