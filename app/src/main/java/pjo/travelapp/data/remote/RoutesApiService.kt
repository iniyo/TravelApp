package pjo.travelapp.data.remote

import pjo.travelapp.BuildConfig
import pjo.travelapp.data.entity.RoutesRequestBody
import pjo.travelapp.data.entity.RoutesResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface RoutesApiService {
    @POST("v2:computeRoutes")
    suspend fun getRoutes(
        @Query("key") apiKey: String = BuildConfig.maps_api_key,
        @Body requestBody: RoutesRequestBody
    ): RoutesResponse
}