package pjo.travelapp.data.remote

import pjo.travelapp.data.entity.AutoCompleteResponse
import pjo.travelapp.data.entity.HotelResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface SkyScannerApiService {
    @Headers(
        "x-rapidapi-host: sky-scanner3.p.rapidapi.com",
        "x-rapidapi-key: cb7d582a6fmsha4930464485b69cp1f6290jsn7ba269110935"
    )
    @GET("hotels/search")
    suspend fun searchHotels(
        @Query("entityId") entityId: String,
        @Query("checkin") checkin: String,
        @Query("checkout") checkout: String
    ): HotelResponse

    @Headers(
        "x-rapidapi-host: sky-scanner3.p.rapidapi.com",
        "x-rapidapi-key: cb7d582a6fmsha4930464485b69cp1f6290jsn7ba269110935"
    )
    @GET("hotels/auto-complete")
    suspend fun autoComplete(
        @Query("query") query: String,
    ): AutoCompleteResponse
}
