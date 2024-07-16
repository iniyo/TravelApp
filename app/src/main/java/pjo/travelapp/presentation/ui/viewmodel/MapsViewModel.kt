// MapsViewModel.kt
package pjo.travelapp.presentation.ui.viewmodel

import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okio.IOException
import pjo.travelapp.data.entity.AutocompletePredictionItem
import pjo.travelapp.data.entity.DirectionsResponse
import pjo.travelapp.data.entity.PlaceDetailsResponse
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.domain.usecase.GetDirectionsUseCase
import pjo.travelapp.domain.usecase.GetNearbyPlaceUseCase
import pjo.travelapp.domain.usecase.GetPlaceDetailUseCase
import pjo.travelapp.domain.usecase.GetPlaceIdUseCase
import pjo.travelapp.presentation.util.LatestUiState
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(
    private val getDirectionsUseCase: GetDirectionsUseCase,
    private val getPlaceIdUseCase: GetPlaceIdUseCase,
    private val getPlaceDetailUseCase: GetPlaceDetailUseCase,
    private val getNearbyPlaceUseCase: GetNearbyPlaceUseCase,
    private val geocoder: Geocoder,
    private var placeCli: PlacesClient
) : ViewModel() {

    // 기존 코드 유지
    private val _directions = MutableStateFlow<DirectionsResponse?>(null)
    val directions: StateFlow<DirectionsResponse?> get() = _directions

    private val _placeDetails =
        MutableStateFlow<LatestUiState<PlaceDetailsResponse?>>(LatestUiState.Loading)
    val placeDetails: StateFlow<LatestUiState<PlaceDetailsResponse?>> get() = _placeDetails

    private val _placeDetailsResult = MutableStateFlow<PlaceResult?>(null)
    val placeDetailsResult: StateFlow<PlaceResult?> get() = _placeDetailsResult

    private val _placeDetailsList = MutableStateFlow<List<PlaceResult>>(emptyList())
    val placeDetailsList: StateFlow<List<PlaceResult>> get() = _placeDetailsList

    private val _lat = MutableStateFlow<LatLng?>(null)
    val lat: StateFlow<LatLng?> get() = _lat

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> get() = _query

    private val _predictionList = MutableStateFlow<List<AutocompletePredictionItem>>(emptyList())
    val predictionList: StateFlow<List<AutocompletePredictionItem>> get() = _predictionList

    private var _placeId = MutableStateFlow<String?>("")
    val placeId: StateFlow<String?> get() = _placeId

    private val _placeLoc = MutableStateFlow("")
    val placeLoc: StateFlow<String> get() = _placeLoc

    private var searchJob: Job? = null

    init {
        fetchPlaceDetailsList()
        fetchPlaceResultList()
    }

    // 검색 요청을 실행하고 이전 작업을 취소하는 메서드
    fun performSearch(query: String, currentLatLng: LatLng? = null) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _predictionList.value = emptyList() // 새로운 검색 시작 시 리스트 초기화
            val token = AutocompleteSessionToken.newInstance()
            val request = if (currentLatLng != null) {
                val bounds = RectangularBounds.newInstance(
                    LatLng(currentLatLng.latitude - 1.7, currentLatLng.longitude - 1.7),
                    LatLng(currentLatLng.latitude + 1.7, currentLatLng.longitude + 1.7)
                )

                FindAutocompletePredictionsRequest.builder()
                    .setLocationBias(bounds)
                    .setOrigin(currentLatLng)
                    .setSessionToken(token)
                    .setQuery(query)
                    .build()
            } else {
                FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(token)
                    .setQuery(query)
                    .build()
            }

            placeCli.findAutocompletePredictions(request)
                .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                    val predictions = response.autocompletePredictions.map { prediction ->
                        AutocompletePredictionItem(
                            prediction.placeId,
                            prediction.getPrimaryText(null).toString()
                        )
                    }
                    fetchPredictions(predictions)
                }
                .addOnFailureListener { exception: Exception? ->
                    if (exception is ApiException) {
                        Log.e("MapsFragment", "Place not found: ${exception.statusCode}")
                    }
                }
        }
    }

    // 검색 결과 저장 메서드
    private fun fetchPredictions(itemList: List<AutocompletePredictionItem>) {
        Log.d("TAG", "fetchPredictions: $itemList")
        viewModelScope.launch {
            _predictionList.value = emptyList() // 새로운 검색 시작 시 리스트 초기화
            _predictionList.value = itemList // 새로운 값 할당
        }
    }

    // 기존 코드 유지
    private fun fetchPlaceDetailsList() {
        viewModelScope.launch {
            _placeDetailsResult.collectLatest { placeResult ->
                placeResult?.let {
                    val currentList = _placeDetailsList.value.toMutableList()
                    if (currentList.none { it.name == placeResult.name }) {
                        currentList.add(placeResult)
                        _placeDetailsList.value = currentList
                    }
                }
            }
        }
    }

    fun fetchPlaceDetails(placeId: String) {
        viewModelScope.launch {
            getPlaceDetailUseCase(placeId)
                .onStart {
                    _placeDetails.value = LatestUiState.Loading
                }
                .catch { e ->
                    e.printStackTrace()
                    _placeDetails.value = LatestUiState.Error(e)
                }
                .collectLatest {
                    _placeDetails.value = LatestUiState.Success(it)
                    _placeDetailsResult.emit(it.result)
                }
        }
    }

    fun fetchPlaceResultList() {
        viewModelScope.launch {
            _placeDetailsResult.collectLatest { placeResult ->
                placeResult?.let {
                    val currentList = _placeDetailsList.value.toMutableList()
                    if (currentList.none { it.name == placeResult.name }) {
                        currentList.add(placeResult)
                        _placeDetailsList.value = currentList
                    }
                }
            }
        }
    }

    fun clearPlaceDetails() {
        _placeDetailsList.value = emptyList()
        _placeDetailsResult.value = null
        _predictionList.value = emptyList()
    }

    fun fetchLatLngToPlaceId(latLng: LatLng? = null, getPlaceId: String = "") {
        viewModelScope.launch {
            if (latLng != null) {
                val latLngString = "${latLng.latitude},${latLng.longitude}"
                getPlaceIdUseCase(latLngString).collect {
                    val placeId = it.results.firstOrNull()?.placeId
                    if (placeId != null) {
                        Log.d("TAG", "fetchLatLngToPlaceId: latLng")
                        _placeId.value = placeId
                    }
                }
            } else if (getPlaceId.isNotEmpty()) {
                Log.d("TAG", "fetchLatLngToPlaceId: placeid")
                _placeId.value = getPlaceId
            } else {
                Log.d("TAG", "fetchLatLngToPlaceId: empty twice ")
            }
        }
    }

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
