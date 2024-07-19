package pjo.travelapp.data.entity

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class DirectionsRequest(
    @SerializedName("origin") val origin: DirectLocation, // LatLng | String | google.maps.Place
    @SerializedName("destination") val destination: DirectLocation, // LatLng | String | google.maps.Place
    @SerializedName("mode") val travelMode: String = "transit, walking, bicycling, driving",
   /* @SerializedName("language") val language: String = "en",*/
    @SerializedName("units") val units: UnitSystem? = null,
    @SerializedName("waypoints") val waypoints: List<Waypoint>? = null,
    @SerializedName("optimizeWaypoints") val optimizeWaypoints: Boolean = false,
    @SerializedName("alternatives") val alternatives: Boolean = false,
    @SerializedName("avoid") val avoid: List<AvoidType>? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("departure_time") val departureTime: String? = null, // Use ISO 8601 format or "now"
    @SerializedName("arrival_time") val arrivalTime: String? = null // Use ISO 8601 format
)

sealed class DirectLocation {
    data class Address(@SerializedName("address") val address: String) : DirectLocation() {
        override fun toString() = address
    }
    data class Coordinates(@SerializedName("latLng") val latLng: LatLng?) : DirectLocation() {
        override fun toString() = "${latLng?.latitude},${latLng?.longitude}"
    }
    data class Place(@SerializedName("place_id") val placeId: String?) : DirectLocation() {
        override fun toString() = "place_id:${placeId}"
    }
    data class PlaceName(@SerializedName("place_name") val placeName: String) : DirectLocation() {
        override fun toString() = placeName
    }
}

enum class TravelMode {
    DRIVING, WALKING, BICYCLING, TRANSIT
}

enum class TransitRoutingPreference {
    LESS_WALKING
}

data class Waypoint(
    @SerializedName("location") val location: DirectLocation, // LatLng | String | google.maps.Place
    @SerializedName("stopover") val stopover: Boolean
)

enum class UnitSystem {
    METRIC, IMPERIAL
}

enum class AvoidType {
    TOLLS, HIGHWAYS, FERRIES
}
