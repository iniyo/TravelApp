package pjo.travelapp.presentation.ui.viewmodel

import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okio.IOException
import pjo.travelapp.data.entity.AutocompletePredictionItem
import pjo.travelapp.data.entity.DirectionsRequest
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
    /**
     * 변수 세팅
     * @param direction: 경로 검색 결과
     * @param placeDetails: 장소 세부 정보
     * @param currentLatLng: 현재 위치
     * @param selectedMarker: 현재 선택된 마커 정보
     * @param query: 검색 텍스트 ex) 부산, 레스토랑 등
     * @param predictionList: 검색 결과
     * @param placeId: 검색된 장소의 id값
     */

    // mutableStateFlow:
    // state flow: 현재 상태 관찰, ※ 내부적으로 값을 conflate하기 때문에 동일한 값은 2번 배출하지 않음
    // 즉, MutableList에 값만 추가하여 다시 emit 해도 해당 emit은 이전 emit과 동일 인스턴스이기 때문에 방출되지 않는다.
    // 따라서 List값 변경을 방출해주기 위해선 immutable list에 + 하여 인스턴스 자체를 바꿔서 emit하는 식으로 해야 한다.
    private val _directions = MutableStateFlow<DirectionsResponse?>(null)
    val directions: StateFlow<DirectionsResponse?> get() = _directions

    private val _placeDetails = MutableStateFlow<LatestUiState<PlaceDetailsResponse?>>(LatestUiState.Loading)
    val placeDetails: StateFlow<LatestUiState<PlaceDetailsResponse?>> get() = _placeDetails

    private val _placeDetailsResult = MutableStateFlow<PlaceResult?>(null)
    val placeDetailsResult: StateFlow<PlaceResult?> get() = _placeDetailsResult

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

    /**
     * 변수 세팅 끝
     */

    init {
        fetchPlaceDetails()
    }

    fun fetchDirections(request: DirectionsRequest) {
        viewModelScope.launch {
            getDirectionsUseCase(request).collectLatest { response: DirectionsResponse ->
                _directions.value = response
            }
        }
    }

    fun fetchPlaceLoc(placeName: String) {
        viewModelScope.launch {
            getDirectionsUseCase.getPlaceLocation(placeName).collectLatest { res ->
                _placeLoc.value = res.results.toString()
            }
        }
    }

    fun fetchSelectedMarker() {

    }

    fun fetchLocation( location: LatLng) {
        viewModelScope.launch {
            _lat.value = location
        }
    }

    // 검색 결과 저장
    fun fetchPredictions(itemList: List<AutocompletePredictionItem>) {
        Log.d("TAG", "fetchPredictions: $itemList")
        viewModelScope.launch {
            _predictionList.value = itemList
        }

    }

    // 장소 세부정보 갱신
    fun fetchPlaceDetails() {
        viewModelScope.launch {
            _placeId.collectLatest {
                _placeId.value?.let { it1 ->
                    getPlaceDetailUseCase(it1)
                        .onStart { _placeDetails.value = LatestUiState.Loading }
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
        }
    }

    fun fetchPlaceDetails(placeId: String) {
        viewModelScope.launch {
            getPlaceDetailUseCase(placeId)
                .catch { e ->
                    e.printStackTrace()
                }
                .collectLatest {
                    _placeDetailsResult.emit(it.result)
                }
        }
    }
    fun clearPlaceDetails() {
        viewModelScope.launch {
            _placeDetailsResult.emit(null)
        }
    }
    // 위치 정보 가져오기
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

    // 장소 검색 후 위치 반환
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

    fun performSearch(query: String, currentLatLng: LatLng? = null) {
        val token = AutocompleteSessionToken.newInstance()
        val request = if(currentLatLng != null) {
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
        } else{
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