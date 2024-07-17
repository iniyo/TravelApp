import com.google.android.gms.maps.model.LatLng
import pjo.travelapp.data.entity.AddressResponse
import pjo.travelapp.data.entity.DirectionsRequest
import pjo.travelapp.data.entity.DirectionsResponse
import pjo.travelapp.data.entity.Location
import pjo.travelapp.data.entity.NearbySearchRequest
import pjo.travelapp.data.entity.NearbySearchResponse
import pjo.travelapp.data.entity.OriginOrDestination
import pjo.travelapp.data.entity.PlaceDetailRequest
import pjo.travelapp.data.entity.PlaceDetailsResponse
import pjo.travelapp.data.entity.PlaceIdRequest
import pjo.travelapp.data.entity.PlaceIdResponse
import pjo.travelapp.data.entity.RoutesRequestBody
import pjo.travelapp.data.entity.RoutesResponse
import pjo.travelapp.data.remote.MapsApiService
import pjo.travelapp.data.remote.RoutesApiService
import pjo.travelapp.data.repo.MapsRepository
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class MapsRepositoryImpl @Inject constructor(
    private val service: MapsApiService,
    private val routeService: RoutesApiService
) : MapsRepository {
    override suspend fun getDirections(request: DirectionsRequest): DirectionsResponse {
        return service.getDirections(
            origin = request.origin,
            destination = request.destination,
        )
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

    override suspend fun getRoute(origin: String, destination: String): RoutesResponse {
        val requestBody = RoutesRequestBody(
            origin = OriginOrDestination(Location(LatLng(37.7749, -122.4194))), // 예시 좌표
            destination = OriginOrDestination(Location(LatLng(34.0522, -118.2437))) // 예시 좌표
        )
        return routeService.getRoutes( requestBody = requestBody)
    }
}
