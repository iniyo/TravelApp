package pjo.travelapp.presentation.ui.viewmodel

import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okio.IOException
import pjo.travelapp.data.entity.DirectionsRequest
import pjo.travelapp.data.entity.DirectionsResponse
import pjo.travelapp.data.entity.PlaceDetailsResponse
import pjo.travelapp.data.remote.MapsDirectionsService
import pjo.travelapp.domain.usecase.GetDirectionsUseCase
import javax.inject.Inject


@HiltViewModel
class MapsViewModel @Inject constructor(
    private val getDirectionsUseCase: GetDirectionsUseCase,
    private val mapsDirectionsService: MapsDirectionsService,
    private val geocoder: Geocoder
) : ViewModel() {
    private val _directions = MutableStateFlow<DirectionsResponse?>(null)
    val directions: StateFlow<DirectionsResponse?> get() = _directions

    fun fetchDirections(request: DirectionsRequest) {
        viewModelScope.launch {
            getDirectionsUseCase(request).collect { response: DirectionsResponse ->
                _directions.value = response
            }
        }
    }

    fun fetchPlaceDetails(placeId: String, onResult: (PlaceDetailsResponse?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = mapsDirectionsService.getPlaceDetails(placeId)
                onResult(response)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }

    fun fetchPlaceId(latLng: LatLng, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val latLngString = "${latLng.latitude},${latLng.longitude}"
                val response = mapsDirectionsService.getPlaceId(latLngString)
                val placeId = response.results.firstOrNull()?.placeId
                onResult(placeId)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }

        }
    }

    @Suppress("DEPRECATION")
    fun searchLocation(location: String, callback: (LatLng?) -> Unit) {
        viewModelScope.launch {
            try {
                val addressList = geocoder.getFromLocationName(location, 1)
                if (!addressList.isNullOrEmpty()) {
                    val address = addressList[0]
                    val latLng = LatLng(address.latitude, address.longitude)
                    callback(latLng)
                } else {
                    callback(null)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                callback(null)
            }
        }
    }
}