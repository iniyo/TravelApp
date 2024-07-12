package pjo.travelapp.data.entity

import com.google.gson.annotations.SerializedName
import pjo.travelapp.BuildConfig

data class PlaceIdRequest(
    @SerializedName("latlng") val latLng: String,
    @SerializedName("key") val apiKey: String = BuildConfig.maps_api_key,
    @SerializedName("language") val language: String = "ko"
) {
    fun toQueryMap(): Map<String, String> {
        return mapOf(
            "latlng" to latLng,
            "key" to apiKey,
            "language" to language
        )
    }
}

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

