package pjo.travelapp.data.remote

import pjo.travelapp.BuildConfig
import pjo.travelapp.data.entity.DirectionsResponse
import pjo.travelapp.data.entity.RoutesRequest
import pjo.travelapp.data.entity.RoutesResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

// Maps API 서비스 인터페이스
interface RoutesApiService {

    // 경로 정보를 가져오는 함수
    @POST("directions/v2:computeRoutes")
    suspend fun computeRoutes(
        @Body request: RoutesRequest,
        @Query("key") apiKey: String = BuildConfig.maps_api_key,
        @Header("X-Goog-FieldMask") fieldMask: String = "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline,routes.legs.startLocation,routes.legs.endLocation,routes.legs.distanceMeters,routes.legs.duration,routes.legs.steps.startLocation,routes.legs.steps.endLocation,routes.legs.steps.polyline.encodedPolyline,routes.legs.steps.distanceMeters,routes.legs.steps.travelMode"
    ): RoutesResponse
}