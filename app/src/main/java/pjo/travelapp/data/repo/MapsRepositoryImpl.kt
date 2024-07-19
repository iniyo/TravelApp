import pjo.travelapp.data.entity.AddressResponse
import pjo.travelapp.data.entity.DirectionsRequest
import pjo.travelapp.data.entity.DirectionsResponse
import pjo.travelapp.data.entity.NearbySearchRequest
import pjo.travelapp.data.entity.NearbySearchResponse
import pjo.travelapp.data.entity.PlaceDetailRequest
import pjo.travelapp.data.entity.PlaceDetailsResponse
import pjo.travelapp.data.entity.PlaceIdRequest
import pjo.travelapp.data.entity.PlaceIdResponse
import pjo.travelapp.data.entity.TransitRoutingPreference
import pjo.travelapp.data.entity.Waypoint
import pjo.travelapp.data.remote.MapsApiService
import pjo.travelapp.data.repo.MapsRepository
import javax.inject.Inject

class MapsRepositoryImpl @Inject constructor(
    private val service: MapsApiService
) : MapsRepository {
    override suspend fun getDirections(request: DirectionsRequest): DirectionsResponse {
        val waypointsString =
            request.waypoints?.let { buildWaypointsString(it, request.optimizeWaypoints) }
        val avoidString = request.avoid?.joinToString("|") { it.name.lowercase() }

        return service.getDirections(
            origin = request.origin.toString(),
            destination = request.destination.toString(),
            travelMode = "transit, walking, bicycling, driving",
            units = request.units?.name?.lowercase(),
            transitRoutingPreference = TransitRoutingPreference.LESS_WALKING.toString().lowercase(),
            waypoints = waypointsString,
            optimizeWaypoints = request.optimizeWaypoints,
            alternatives = request.alternatives,
            avoid = avoidString,
            region = request.region,
            departureTime = request.departureTime,
            arrivalTime = request.arrivalTime,
            trafficModel = null
        )
    }

    private fun buildWaypointsString(waypoints: List<Waypoint>, optimize: Boolean): String {
        val waypointsStr = waypoints.joinToString("|") { it.location.toString() }
        return if (optimize) "optimize:true|$waypointsStr" else waypointsStr
    }

    override suspend fun getPlaceDetail(request: PlaceDetailRequest): PlaceDetailsResponse {
        return service.getPlaceDetails(
            placeId = request.placeId
        )
    }

    override suspend fun getPlaceId(request: PlaceIdRequest): PlaceIdResponse {
        return service.getPlaceId(
            latLng = request.latLng
        )
    }

    override suspend fun getNearbyPlaces(request: NearbySearchRequest): NearbySearchResponse {
        return service.getNearbyPlaces(
            location = request.location,
            radius = request.radius,
            type = request.type
        )
    }

    override suspend fun getPlaceAddress(placeName: String): AddressResponse {
        return service.getPlaceAddress(placeName)
    }

}
