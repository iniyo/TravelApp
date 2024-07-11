package pjo.travelapp.data.remote

import pjo.travelapp.BuildConfig
import pjo.travelapp.data.entity.DirectionsResponse
import pjo.travelapp.data.entity.NearbySearchResponse
import pjo.travelapp.data.entity.PlaceDetailsResponse
import pjo.travelapp.data.entity.PlaceIdResponse
import retrofit2.http.GET
import retrofit2.http.Query


interface MapsDirectionsService {

    // 경로 정보 가져오기, null은 선택사항.
    @GET("json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String,
        @Query("transit_routing_preference") transitRoutingPreference: String? = null,
        @Query("departure_time") departureTime: String? = null,
        @Query("arrival_time") arrivalTime: String? = null,
        @Query("traffic_model") trafficModel: String? = null,
        @Query("units") units: String? = null,
        @Query("waypoints") waypoints: String? = null,
        @Query("optimizeWaypoints") optimizeWaypoints: Boolean = false,
        @Query("alternatives") alternatives: Boolean = false,
        @Query("avoid") avoid: String? = null,
        @Query("region") region: String? = null,
        @Query("key") apiKey: String = BuildConfig.maps_api_key
    ): DirectionsResponse

    // 주변 일정 거리 내의 장소 정보 가져오기
    @GET("maps/api/place/nearbysearch/json")
    suspend fun getNearbyPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("key") apiKey: String = BuildConfig.maps_api_key,
        @Query("language") language: String = "ko"
    ): NearbySearchResponse

    // 선택한 장소 id로 장소 세부 정보 가져오기
    @GET("maps/api/place/details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("key") apiKey: String = BuildConfig.maps_api_key,
        @Query("language") language: String = "ko"
    ): PlaceDetailsResponse

    // 장소 id 가져오기
    @GET("maps/api/geocode/json")
    suspend fun getPlaceId(
        @Query("latlng") latLng: String,
        @Query("key") apiKey: String = BuildConfig.maps_api_key,
        @Query("language") language: String = "ko"
    ): PlaceIdResponse
}