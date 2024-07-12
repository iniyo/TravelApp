package pjo.travelapp.data.entity

import com.google.gson.annotations.SerializedName

data class AutocompletePredictionItem(
    @SerializedName("place_id")
    val placeId: String,
    val primaryText: String,
    val secondaryText: String? = null,
    val fullText: String? = null,
    val description: String? = null,
    val types: List<String> = emptyList()
)
