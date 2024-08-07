package pjo.travelapp.data.entity

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.gson.annotations.SerializedName
import pjo.travelapp.BuildConfig

data class PlaceDetailRequest(
    @SerializedName("place_id") val placeId: String,
    @SerializedName("key") val apiKey: String = BuildConfig.maps_api_key,
    @SerializedName("language") val language: String = "ko"
)

data class PlacesResponse(
    val results: List<PlaceResult>
)

data class PlaceDetailsResponse(
    /*    @SerializedName("html_attributions") val htmlAttributions: List<String>,*/
    @SerializedName("result") val result: PlaceResult,
    @SerializedName("status") val status: String
)

data class PlaceResult(
    /* @SerializedName("address_components") val addressComponents: List<AddressComponent>,
     @SerializedName("adr_address") val adrAddress: String,
     @SerializedName("business_status") val businessStatus: String,
     @SerializedName("dine_in") val dineIn: Boolean,
     @SerializedName("icon") val icon: String,
     @SerializedName("icon_background_color") val iconBackgroundColor: String,
     @SerializedName("icon_mask_base_uri") val iconMaskBaseUri: String,
     @SerializedName("international_phone_number") val internationalPhoneNumber: String?,
     @SerializedName("plus_code") val plusCode: PlusCode,
     @SerializedName("reference") val reference: String,
     @SerializedName("reservable") val reservable: Boolean,
     @SerializedName("takeout") val takeout: Boolean,
     @SerializedName("utc_offset") val utcOffset: Int,
     @SerializedName("url") val url: String,
     @SerializedName("current_opening_hours") val currentOpeningHours: CurrentOpeningHours?,
     @SerializedName("user_ratings_total") val userRatingsTotal: Int,
     @SerializedName("serves_dinner") val servesDinner: Boolean,
     @SerializedName("serves_lunch") val servesLunch: Boolean,*/

    @SerializedName("formatted_address") val formattedAddress: String,
    @SerializedName("formatted_phone_number") val formattedPhoneNumber: String?,
    @SerializedName("geometry") val geometry: Geometry,
    @SerializedName("name") val name: String,
    @SerializedName("opening_hours") val openingHours: OpeningHours?,
    @SerializedName("photos") val photos: List<Photo>,
    @SerializedName("place_id") val placeId: String,
    @SerializedName("rating") val rating: Double,
    @SerializedName("types") val types: List<String>,
    @SerializedName("reviews") val reviews: List<Review>,
    @SerializedName("vicinity") val vicinity: String,
    @SerializedName("website") val website: String?
)

data class AddressComponent(
    @SerializedName("long_name") val longName: String,
    @SerializedName("short_name") val shortName: String,
    @SerializedName("types") val types: List<String>
)

// 현재 시간에 따른 운영 상태를 포함한 운영 시간 정보
data class CurrentOpeningHours(
    @SerializedName("open_now") val openNow: Boolean,
 /*   @SerializedName("periods") val periods: List<Period>,*/
    @SerializedName("weekday_text") val weekdayText: List<String>
)

data class Period(
    @SerializedName("close") val close: TimeDetail,
    @SerializedName("open") val open: TimeDetail
)

data class TimeDetail(
    @SerializedName("date") val date: String?,
    @SerializedName("day") val day: Int,
    @SerializedName("time") val time: String,
    @SerializedName("truncated") val truncated: Boolean?
)

data class Geometry(
    @SerializedName("location") val location: Location,
    /*@SerializedName("viewport") val viewport: Viewport? = null*/
)

data class Location(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)

data class Viewport(
    @SerializedName("northeast") val northeast: LatLng,
    @SerializedName("southwest") val southwest: LatLng
)

data class OpeningHours(
    @SerializedName("open_now") val openNow: Boolean,
    /*@SerializedName("periods") val periods: List<Period>,*/
    @SerializedName("weekday_text") val weekdayText: List<String>
)

data class Photo(
    @SerializedName("height") val height: Int,
  /*  @SerializedName("html_attributions") val htmlAttributions: List<String>,*/
    @SerializedName("photo_reference") val photoReference: String,
    @SerializedName("width") val width: Int
) {
    fun getPhotoUrl(maxWidth: Int = 400): String {
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=$maxWidth&photoreference=$photoReference&key=${BuildConfig.maps_api_key}"
    }
}

data class PlusCode(
    @SerializedName("compound_code") val compoundCode: String,
    @SerializedName("global_code") val globalCode: String
)

data class Review(
    @SerializedName("author_name") val authorName: String,
    @SerializedName("author_url") val authorUrl: String,
    @SerializedName("language") val language: String,
    @SerializedName("original_language") val originalLanguage: String?,
    @SerializedName("profile_photo_url") val profilePhotoUrl: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("relative_time_description") val relativeTimeDescription: String,
    @SerializedName("text") val text: String,
    @SerializedName("time") val time: Long,
    @SerializedName("translated") val translated: Boolean
)

data class PlaceDetail(
    val place: Place,
    val bitmap: List<Bitmap?>?
)