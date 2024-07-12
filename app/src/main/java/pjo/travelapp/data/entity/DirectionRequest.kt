package pjo.travelapp.data.entity

import com.google.gson.annotations.SerializedName
import java.util.Date

data class DirectionsRequest(
    val origin: directLocation, // LatLng | String | google.maps.Place
    val destination: directLocation, // LatLng | String | google.maps.Place
    val travelMode: TravelMode = TravelMode.DRIVING,
    val languageCode: String = "",
    val transitOptions: TransitOptions? = null,
    val drivingOptions: DrivingOptions? = null,
    val unitSystem: UnitSystem? = null,
    val waypoints: List<Waypoint>? = null,
    val optimizeWaypoints: Boolean = false,
    val provideRouteAlternatives: Boolean = false,
    val avoidFerries: Boolean = false,
    val avoidHighways: Boolean = false,
    val avoidTolls: Boolean = false,
    val region: String? = null
)

data class directLocation (
    @SerializedName("LatLng")
    val latlng: List<directLatlng>
)

data class directLatlng (
    val latitude: Double,
    val longitude: Double
)

enum class TravelMode {
    DRIVING, WALKING, BICYCLING, TRANSIT
}

data class Waypoint(
    val location: Any, // LatLng | String | google.maps.Place
    val stopover: Boolean
)

data class TransitOptions(
    val arrivalTime: Date? = null,
    val departureTime: Date? = null,
    val modes: List<TransitMode>? = null,
    val routingPreference: TransitRoutePreference? = null
)

enum class TransitMode {
    BUS, SUBWAY, TRAIN, TRAM, RAIL
}

enum class TransitRoutePreference {
    LESS_WALKING, FEWER_TRANSFERS
}

data class DrivingOptions(
    val departureTime: String, // Use ISO 8601 format
    val trafficModel: TrafficModel
)

enum class TrafficModel {
    BEST_GUESS, PESSIMISTIC, OPTIMISTIC
}

enum class UnitSystem {
    METRIC, IMPERIAL
}