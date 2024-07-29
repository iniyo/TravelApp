package pjo.travelapp.data.entity

import android.graphics.Bitmap
import com.google.android.libraries.places.api.model.Place

data class RecommendedEntity (
    val title: String,
    val location: String,
    val rating: Double,
    val imgUrl: String
)
data class PlaceItem(
    val place: Place,
    val photoBitmap: Bitmap?
)data class PlaceWithPhoto(
    val place: Place,
    val photo: Bitmap?
)