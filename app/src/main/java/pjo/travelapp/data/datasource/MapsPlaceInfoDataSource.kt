package pjo.travelapp.data.datasource


import pjo.travelapp.data.remote.MapsDirectionsService
import javax.inject.Inject

class MapsPlaceInfoDataSource @Inject constructor(
    private val  service: MapsDirectionsService
){
    /*suspend fun getNearbyPlaces(location: String, radius: Int, type: String, apiKey: String): PlacesResponse? {
        return try {
            service.getNearbyPlaces(location, radius, type, apiKey)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }*/
}