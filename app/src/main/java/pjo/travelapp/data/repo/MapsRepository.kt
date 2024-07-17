package pjo.travelapp.data.repo

import com.google.android.gms.maps.model.LatLng
import pjo.travelapp.data.entity.AddressResponse
import pjo.travelapp.data.entity.DirectionsRequest
import pjo.travelapp.data.entity.DirectionsResponse
import pjo.travelapp.data.entity.NearbySearchRequest
import pjo.travelapp.data.entity.NearbySearchResponse
import pjo.travelapp.data.entity.PlaceDetailRequest
import pjo.travelapp.data.entity.PlaceDetailsResponse
import pjo.travelapp.data.entity.PlaceIdRequest
import pjo.travelapp.data.entity.PlaceIdResponse
import pjo.travelapp.data.entity.RoutesResponse

interface MapsRepository {
    suspend fun getDirections(request: DirectionsRequest): DirectionsResponse
    suspend fun getNearbyPlaces(request: NearbySearchRequest): NearbySearchResponse?
    suspend fun getPlaceDetail(request: PlaceDetailRequest): PlaceDetailsResponse
    suspend fun getPlaceId(request: PlaceIdRequest): PlaceIdResponse
    suspend fun getPlaceAddress(placeName: String): AddressResponse
    suspend fun getRoute(origin: String, destination: String ): RoutesResponse
}
