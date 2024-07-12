package pjo.travelapp.presentation.util.bindadapter

import android.content.res.ColorStateList
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import pjo.travelapp.R
import pjo.travelapp.data.entity.Photo

object BindAdapter {
    @JvmStatic
    @BindingAdapter("android:text")
    fun setText(view: TextView, rating: Double?) {
        view.text = rating?.toString() ?: ""
    }

    @JvmStatic
    @BindingAdapter("android:rating")
    fun setRating(ratingBar: RatingBar, rating: Double) {
        ratingBar.rating = rating.toFloat()
    }

    @JvmStatic
    @BindingAdapter("openCloseStatus")
    fun TextView.setOpenCloseStatusAndColor(openNow: Boolean?) {
        openNow?.let {
            if (it) {
                text = context.getString(R.string.opening)
                val color = ContextCompat.getColor(context, R.color.selected_icon_color)
                setTextColor(ColorStateList.valueOf(color))
            } else {
                text = context.getString(R.string.closed)
                val color = ContextCompat.getColor(context, R.color.dark_light_gray)
                setTextColor(ColorStateList.valueOf(color))
            }
        }
    }

    @JvmStatic
    @BindingAdapter("imageUrl")
    fun loadImage(view: ImageView, photo: Photo?) {
        photo?.getPhotoUrl()?.let {
            Glide.with(view.context)
                .load(it)
                .into(view)
        }
    }
}
