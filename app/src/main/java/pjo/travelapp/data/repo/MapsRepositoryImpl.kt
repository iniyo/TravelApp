import pjo.travelapp.data.entity.AddressResponse
import pjo.travelapp.data.entity.DirectionsRequest
import pjo.travelapp.data.entity.DirectionsResponse
import pjo.travelapp.data.entity.NearbySearchRequest
import pjo.travelapp.data.entity.NearbySearchResponse
import pjo.travelapp.data.entity.PlaceDetailRequest
import pjo.travelapp.data.entity.PlaceDetailsResponse
import pjo.travelapp.data.entity.PlaceIdRequest
import pjo.travelapp.data.entity.PlaceIdResponse
import pjo.travelapp.data.remote.MapsApiService
import pjo.travelapp.data.repo.MapsRepository
import javax.inject.Inject

class MapsRepositoryImpl @Inject constructor(
    private val service: MapsApiService
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
}
