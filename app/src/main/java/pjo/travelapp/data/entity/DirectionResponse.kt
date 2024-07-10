package pjo.travelapp.data.entity

import com.google.gson.annotations.SerializedName

data class DirectionsResponse(
    val routes: List<Route>
)

data class Route(
    val bounds: Bounds,
    val legs: List<Leg>,
    @SerializedName("overview_polyline")
    val overviewPolyline: Polyline,
    val summary: String,
    val warnings: List<String>,
    @SerializedName("waypoint_order")
    val waypointOrder: List<Int>
)

data class Bounds(
    val northeast: LatLng,
    val southwest: LatLng
)

data class Leg(
    val distance: Distance,
    val duration: Duration,
    @SerializedName("end_address")
    val endAddress: String,
    @SerializedName("end_location")
    val endLocation: LatLng,
    @SerializedName("start_address")
    val startAddress: String,
    @SerializedName("start_location")
    val startLocation: LatLng,
    val steps: List<Step>,
    @SerializedName("traffic_speed_entry")
    val trafficSpeedEntry: List<Any>,
    @SerializedName("via_waypoint")
    val viaWaypoint: List<Any>
)

data class Step(
    val distance: Distance,
    val duration: Duration,
    @SerializedName("end_location")
    val endLocation: LatLng,
    @SerializedName("html_instructions")
    val htmlInstructions: String,
    val polyline: Polyline,
    @SerializedName("start_location")
    val startLocation: LatLng,
    @SerializedName("travel_mode")
    val travelMode: String
)

data class Distance(
    val text: String,
    val value: Int
)

data class Duration(
    val text: String,
    val value: Int
)

data class Polyline(
    val points: String
)

data class LatLng(
    val lat: Double,
    val lng: Double
)
