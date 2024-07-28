package pjo.travelapp.data.repo

import pjo.travelapp.data.entity.AutoCompleteResponse
import pjo.travelapp.data.entity.HotelCard
import pjo.travelapp.data.entity.HotelResponse

interface HotelRepository {
    suspend fun searchHotels(entityId: String, checkin: String, checkout: String): HotelResponse
    suspend fun autoComplete(query: String): AutoCompleteResponse
}