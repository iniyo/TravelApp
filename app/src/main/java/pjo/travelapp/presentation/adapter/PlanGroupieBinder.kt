package pjo.travelapp.presentation.adapter

import android.view.View
import com.bumptech.glide.Glide
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.viewbinding.BindableItem
import pjo.travelapp.R
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.data.entity.UserNote
import pjo.travelapp.databinding.ItemChildPlaceBinding
import pjo.travelapp.databinding.RvPlanItemBinding
import pjo.travelapp.presentation.ui.consts.TAKE_REVIEWS

class ParentPlanItem(
    val item: Pair<Int, Int>,
    var note: UserNote?,
    private val noteClickListener: (Int) -> Unit,
    private val placeClickListener: (Int) -> Unit,
    private val parentIndex: Int // 고유 인덱스 추가
) : BindableItem<RvPlanItemBinding>(), ExpandableItem {

    private lateinit var childCommentGroup: ExpandableGroup

    override fun getLayout(): Int = R.layout.rv_plan_item

    override fun getId(): Long = item.hashCode().toLong()

    override fun bind(viewBinding: RvPlanItemBinding, position: Int) {
        viewBinding.apply {
            item.let {
                val stringBuilder = StringBuilder()
                stringBuilder.apply {
                    append(parentIndex.inc().toString())
                    append("일차 ")
                    append(" / ")
                    append(item.first.toString())
                    append("월 ")
                    append(item.second.toString())
                    append("일")
                }
                tvDate.text = stringBuilder

                btnNote.setOnClickListener { noteClickListener(parentIndex) }
                btnSelectPlace.setOnClickListener { placeClickListener(parentIndex) } // position 대신 parentIndex 사용
            }
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
            tvIconPosition.text = position.toString()
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

