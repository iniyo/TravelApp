package pjo.travelapp.data.entity

import com.google.gson.annotations.SerializedName

data class PlaceIdResponse(
    val results: List<GeocodeResult>,
    val status: String
)

data class GeocodeResult(
    @SerializedName("place_id")
    val placeId: String,
    @SerializedName("formatted_address")
    val formattedAddress: String?,
    val geometry: Geometry?,
    val types: List<String>?
)

