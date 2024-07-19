package pjo.travelapp.data.entity

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class DirectionsResponse(
    @SerializedName("geocoded_waypoints") val geocodedWaypoints: List<GeocodedWaypoint>,
    @SerializedName("routes") val routes: List<Route>,
    @SerializedName("status") val status: String
)

data class Route(
    @SerializedName("bounds") val bounds: Bounds,
    @SerializedName("copyrights") val copyrights: String,
    @SerializedName("legs") val legs: List<Leg>,
    @SerializedName("overview_polyline") val overviewPolyline: Polyline,
    @SerializedName("summary") val summary: String,
    @SerializedName("warnings") val warnings: List<String>,
    @SerializedName("waypoint_order") val waypointOrder: List<Int>
)

data class Leg(
    @SerializedName("distance") val distance: Distance,
    @SerializedName("duration") val duration: Duration,
    @SerializedName("end_address") val endAddress: String,
    @SerializedName("end_location") val endLocation: LatLng,
    @SerializedName("start_address") val startAddress: String,
    @SerializedName("start_location") val startLocation: LatLng,
    @SerializedName("steps") val steps: List<Step>,
    @SerializedName("traffic_speed_entry") val trafficSpeedEntry: List<Any>,
    @SerializedName("via_waypoint") val viaWaypoint: List<Any>
)

data class Step(
    @SerializedName("distance") val distance: Distance,
    @SerializedName("duration") val duration: Duration,
    @SerializedName("end_location") val endLocation: LatLng,
    @SerializedName("html_instructions") val htmlInstructions: String,
    @SerializedName("polyline") val polyline: Polyline,
    @SerializedName("start_location") val startLocation: LatLng,
    @SerializedName("travel_mode") val travelMode: String,
    @SerializedName("maneuver") val maneuver: String? = null
)

data class Polyline(
    @SerializedName("points") val points: String
)

data class Duration(
    @SerializedName("text") val text: String,
    @SerializedName("value") val value: Int
)

data class Distance(
    @SerializedName("text") val text: String,
    @SerializedName("value") val value: Int
)

data class Bounds(
    @SerializedName("northeast") val northeast: LatLng,
    @SerializedName("southwest") val southwest: LatLng
)

data class GeocodedWaypoint(
    @SerializedName("geocoder_status") val geocoderStatus: String,
    @SerializedName("place_id") val placeId: String,
    @SerializedName("types") val types: List<String>
)


