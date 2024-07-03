package pjo.travelapp.presentation.util

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.flexbox.FlexboxLayout
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
            background = ContextCompat.getDrawable(context, R.drawable.bg_white_corner)
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

    private fun Int.dpToPx(): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}

