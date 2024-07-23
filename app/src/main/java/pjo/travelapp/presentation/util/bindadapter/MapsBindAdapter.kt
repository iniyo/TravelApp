package pjo.travelapp.presentation.util.bindadapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pjo.travelapp.R
import pjo.travelapp.data.entity.Photo

object MapsBindAdapter {
    /**
     * info bottom sheet databinding
     */
    @JvmStatic
    @BindingAdapter("android:text")
    fun setRatingText(view: TextView, rating: Double?) {
        view.text = rating?.toString() ?: ""
    }

    @BindingAdapter("hideViewForTextNullOrEmpty")
    @JvmStatic
    fun setViewVisibility(view: View, text: String?) {
        if (text.isNullOrEmpty()) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    @JvmStatic
    @BindingAdapter("getPhotosSize")
    fun getPhotoSize(view: TextView, photos: List<Photo>?) {
        if (photos != null) {
            view.text = photos.size.toString() + "장"
        } else {
            view.text = "0장"
        }
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
        val ph: String?
        var img: Int = R.drawable.img_bg_title
        if (photo != null) {
            ph = photo.getPhotoUrl()
            Glide.with(view.context)
                .load(ph)
                .error(img)
                .placeholder(img)
                .into(view)
        } else {
            img = R.drawable.img_bg_title
            Glide.with(view.context)
                .load(img)
                .error(img)
                .placeholder(img)
                .into(view)
        }
    }

    @JvmStatic
    @BindingAdapter("allWeekdayInfo")
    fun loadWeekday(view: TextView, weekDay: List<String>?) {
        view.text = if (!weekDay.isNullOrEmpty()) {
            weekDay.joinToString("\n")
        } else {
            ""
        }
    }
    /**
     * info bottom sheet databinding end
     */

    /**
     * edit text search list databinding
     */
    @InverseBindingAdapter(attribute = "query")
    @JvmStatic
    fun getQuery(view: AppCompatEditText): String {
        return view.text.toString()
    }

    @BindingAdapter("queryAttrChanged")
    @JvmStatic
    fun setQueryListener(view: AppCompatEditText, listener: InverseBindingListener?) {
        if (listener != null) {
            view.addTextChangedListener(object : SimpleTextWatcher() {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    listener.onChange()
                    Log.d("TAG", "onTextChanged queryAttrChanged: $listener")
                }
            })
        }
    }

    @BindingAdapter("query")
    @JvmStatic
    fun setQuery(view: AppCompatEditText, query: StateFlow<String>?) {
        val lifecycleOwner = view.context as? LifecycleOwner
        lifecycleOwner?.lifecycleScope?.launch {
            query?.collect { value ->
                if (view.text.toString() != value) {
                    view.setText(value)
                }
            }
        }
    }
    /**
     * edit text search list databinding end
     */
}

abstract class SimpleTextWatcher : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun afterTextChanged(s: Editable?) {}
}
