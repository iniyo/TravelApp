package pjo.travelapp.data

data class DirectionsResponse(
    val routes: List<Route>
)

data class Route(
    val legs: List<Leg>
)

data class Leg(
    val steps: List<Step>
)

data class Step(
    val start_location: Location,
    val end_location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)
