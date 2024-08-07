package pjo.travelapp.data.repo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pjo.travelapp.data.entity.AutoCompleteResponse
import pjo.travelapp.data.entity.HotelCard
import pjo.travelapp.data.entity.HotelResponse
import pjo.travelapp.data.remote.SkyScannerApiService
import javax.inject.Inject


class HotelRepositoryImpl @Inject constructor(
    private val apiService: SkyScannerApiService
) : HotelRepository {

    override suspend fun searchHotels(entityId: String, checkin: String, checkout: String): HotelResponse {
        return withContext(Dispatchers.IO) {
            apiService.searchHotels(entityId = entityId, checkIn = "2024-07-31", checkOut = "2024-08-02")
        }
    }

    override suspend fun autoComplete(query: String): AutoCompleteResponse {
        return withContext(Dispatchers.IO) {
            apiService.autoComplete(query)
        }
    }
}
