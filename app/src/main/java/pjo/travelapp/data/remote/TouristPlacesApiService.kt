package pjo.travelapp.data.remote

import pjo.travelapp.data.entity.PlacesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TouristPlacesApiService {
    @GET("v1/places:searchText")
    suspend fun searchPlaces(
        @Query("query") query: String,
        @Query("key") apiKey: String
    ): PlacesResponse
}
