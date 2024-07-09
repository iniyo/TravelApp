package pjo.travelapp.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsService {
    @GET("json")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String
    ): Call<DirectionsResponse>
}