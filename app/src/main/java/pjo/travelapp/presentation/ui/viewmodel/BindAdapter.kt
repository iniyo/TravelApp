package pjo.travelapp.presentation.ui.viewmodel

import android.widget.RatingBar
import android.widget.TextView
import androidx.databinding.BindingAdapter


@BindingAdapter("ratingTextBinding")
fun TextView.setPlaceDetailsRatingText(rating: Double?) {
    text = rating?.toString() ?: ""
}

@BindingAdapter("ratingBarBinding")
fun RatingBar.setPlaceDetailsRatingFloat(ratingScore: Double?) {
    rating = ratingScore?.toFloat() ?: 0f
}