package pjo.travelapp.presentation.util

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel
import pjo.travelapp.R

class FlexboxItemManager(private val context: Context, private val flexboxLayout: FlexboxLayout) {

    private val idMap = mutableMapOf<String, Int>()

    fun addItem(
        layoutId: String,
        imageViewId: String,
        textViewId: String,
        iconResId: Int,
        textResId: Int,
        clickListener: ((View) -> Unit)? = null
    ) {
        val generatedLayoutId = idMap[layoutId] ?: View.generateViewId().also { idMap[layoutId] = it }
        val generatedImageViewId = idMap[imageViewId] ?: View.generateViewId().also { idMap[imageViewId] = it }
        val generatedTextViewId = idMap[textViewId] ?: View.generateViewId().also { idMap[textViewId] = it }

        val newItem = LinearLayout(context).apply {
            id = generatedLayoutId // 레이아웃 ID 설정
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(5.dpToPx(), 0, 0, 10.dpToPx())
            }
            orientation = LinearLayout.HORIZONTAL
            setPadding(6.dpToPx(), 6.dpToPx(), 6.dpToPx(), 6.dpToPx())
            background = ContextCompat.getDrawable(context, R.drawable.btn_effect_user_detail_white_corner)
            gravity = Gravity.CENTER_VERTICAL
            clickListener?.let { setOnClickListener(it) } // 클릭 리스너 설정
        }

        val imageView = ImageView(context).apply {
            id = generatedImageViewId // ImageView ID 설정
            layoutParams = LinearLayout.LayoutParams(18.dpToPx(), 18.dpToPx()).apply {
                setMargins(5.dpToPx(), 0, 12.dpToPx(), 0)
            }
            setImageResource(iconResId)
            contentDescription = context.getString(textResId)
        }

        val textView = TextView(context).apply {
            id = generatedTextViewId // TextView ID 설정
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 5.dpToPx(), 0)
            }
            setText(textResId)
            textSize = 13f
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            setTypeface(typeface, android.graphics.Typeface.NORMAL)
        }

        newItem.addView(imageView)
        newItem.addView(textView)

        flexboxLayout.addView(newItem)
    }

    fun addDeletableItem(
        layoutId: String? = null,
        imageViewId: String? = null,
        textViewId: String? = null,
        deleteButtonId: String? = null,
        imageResource: Int,
        textResId: String,
        clickListener: ((String) -> Unit)? = null
    ) {

/*
        // 아이템 추가
        addedItems.add(textResId)*/

        val generatedLayoutId = layoutId?.let { idMap[it] } ?: View.generateViewId().also { idMap[layoutId ?: "layout"] = it }
        val generatedImageViewId = imageViewId?.let { idMap[it] } ?: View.generateViewId().also { idMap[imageViewId ?: "imageView"] = it }
        val generatedTextViewId = textViewId?.let { idMap[it] } ?: View.generateViewId().also { idMap[textViewId ?: "textView"] = it }
        val generatedDeleteButtonId = deleteButtonId?.let { idMap[it] } ?: View.generateViewId().also { idMap[deleteButtonId ?: "deleteButton"] = it }

        val newItem = ConstraintLayout(context).apply {
            id = generatedLayoutId // 레이아웃 ID 설정
            layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 5.dpToPx(), 0, 5.dpToPx())
            }
            setBackgroundColor(Color.TRANSPARENT)
        }

        val imageView = ShapeableImageView(context).apply {
            id = generatedImageViewId // ImageView ID 설정
            layoutParams = ConstraintLayout.LayoutParams(40.dpToPx(), 40.dpToPx()).apply {
                topMargin = 8.dpToPx()
                marginStart = 8.dpToPx()
                marginEnd = 8.dpToPx()
            }
            setImageResource(imageResource)
            scaleType = ImageView.ScaleType.CENTER_CROP
            shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setAllCornerSizes(ShapeAppearanceModel.PILL)
                .build()
            // 클릭 이벤트 무시
            isClickable = false
        }

        val deleteButton = ImageView(context).apply {
            id = generatedDeleteButtonId // 삭제 버튼 ID 설정
            layoutParams = ConstraintLayout.LayoutParams(
                25.dpToPx(),
                25.dpToPx()
            )
            setImageResource(android.R.drawable.ic_delete)
            setOnClickListener {
                // 삭제 버튼을 클릭하면 부모 레이아웃에서 해당 아이템을 제거
                flexboxLayout.removeView(newItem)
                clickListener?.invoke(textResId) // 전달된 클릭 리스너 호출
            }
        }

        val textView = TextView(context).apply {
            id = generatedTextViewId // TextView ID 설정
            layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 2.dpToPx()
                marginEnd = 5.dpToPx()
            }
            text = textResId
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, R.color.dark_blue))
            setTypeface(typeface, android.graphics.Typeface.NORMAL)
            // 클릭 이벤트 무시
            isClickable = false
        }

        newItem.addView(imageView)
        newItem.addView(deleteButton)
        newItem.addView(textView)

        // Constraint 설정
        ConstraintSet().apply {
            clone(newItem)
            // 아이템들을 중앙에 배치
            connect(imageView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(imageView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(imageView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

            connect(deleteButton.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(deleteButton.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

            connect(textView.id, ConstraintSet.TOP, imageView.id, ConstraintSet.BOTTOM)
            connect(textView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(textView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

            applyTo(newItem)
        }

        flexboxLayout.addView(newItem)
    }

    private fun Int.dpToPx(): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}
