package pjo.travelapp.data.remote

import pjo.travelapp.BuildConfig
import pjo.travelapp.data.entity.AutoCompleteResponse
import pjo.travelapp.data.entity.HotelResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface SkyScannerApiService {

    @GET("hotels/search")
    suspend fun searchHotels(
        @Header("x-rapidapi-key") apiKey: String = BuildConfig.skyscanner_api_key,
        @Query("entityId") entityId: String,
        @Query("checkin") checkIn: String,
        @Query("checkout") checkOut: String,
        @Query("locale") locale: String = "ko-KR"
    ): HotelResponse


    @GET("hotels/auto-complete")
    @Headers("x-rapidapi-key: cb7d582a6fmsha4930464485b69cp1f6290jsn7ba269110935")
    suspend fun autoComplete(
        @Query("query") query: String,
    ): AutoCompleteResponse
}