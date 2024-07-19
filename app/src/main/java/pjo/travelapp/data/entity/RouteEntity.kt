package pjo.travelapp.data.entity

import com.google.android.gms.maps.model.LatLng
import java.util.Date

// Routes API 요청을 나타내는 데이터 클래스
data class RoutesRequest(
    val origin: DLocation,
    val destination: DLocation,
    val travelMode: String = "DRIVE",
    val routingPreference: String? = null,
    val departureTime: String? = "2023-10-15T15:01:23.045123456Z",
    val computeAlternativeRoutes: Boolean = false,
    val routeModifiers: RouteModifiers = RouteModifiers(),
    val languageCode: String = "kr",
    val units: String = "IMPERIAL"
)
data class DLocation(
    val location: LatLngObject
)

data class LatLngObject(
    val latLng: LatLng?
)

data class RouteModifiers(
    val avoidTolls: Boolean = false,
    val avoidHighways: Boolean = false,
    val avoidFerries: Boolean = false
)


sealed class RouteDirectLocation {
    data class Address(val address: String) : RouteDirectLocation()
    data class Coordinates(val latLng: LatLng) : RouteDirectLocation()
    data class Place(val placeId: String) : RouteDirectLocation()

    override fun toString(): String {
        return when (this) {
            is Address -> address
            is Coordinates -> "${latLng.latitude},${latLng.longitude}"
            is Place -> "place_id:${placeId}"
        }
    }
}

data class DirectLatLng(
    val latitude: Double,
    val longitude: Double
)

enum class RouteTravelMode {
    DRIVING, WALKING, BICYCLING, TRANSIT, TWO_WHEELER
}

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
    val departureTime: String,
    val trafficModel: TrafficModel
)

enum class TrafficModel {
    BEST_GUESS, PESSIMISTIC, OPTIMISTIC
}

// Routes API 응답을 나타내는 데이터 클래스
data class RoutesResponse(
    val routes: List<Routes>
)

data class Routes(
    val distanceMeters: Int,
    val duration: String,
    val polyline: DPolyline?
)

data class DPolyline(
    val encodedPolyline: String
)

data class RouteLeg(
    val distanceMeters: Int,
    val duration: String,
    val staticDuration: String,
    val polyline: Polyline,
    val startLocation: Location,
    val endLocation: Location,
    val steps: List<Step>,
    val travelAdvisory: TravelAdvisory
)

data class RouteStep(
    val distanceMeters: Int,
    val staticDuration: String,
    val polyline: Polyline,
    val startLocation: Location,
    val endLocation: Location,
    val navigationInstruction: NavigationInstruction
)

data class RoutePolyline(
    val encodedPolyline: String
)

data class RouteLocation(
    val latLng: LatLng?
)

data class NavigationInstruction(
    val maneuver: String,
    val instructions: String
)

data class RouteViewport(
    val high: Location,
    val low: Location
)

data class TravelAdvisory(
    val tollInfo: TollInfo
)

data class TollInfo(
    val estimatedPrice: List<Money>
)

data class Money(
    val currencyCode: String,
    val units: String,
    val nanos: Int
)


