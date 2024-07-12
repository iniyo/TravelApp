package pjo.travelapp.presentation.ui.viewmodel

import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val geocoder: Geocoder
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

    private val _currentLatLng = MutableStateFlow<LatLng?>(null)
    val currentLatLng: StateFlow<LatLng?> get() = _currentLatLng

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> get() = _query

    private val _predictionList = MutableStateFlow<List<AutocompletePredictionItem>?>(null)
    val predictionList: StateFlow<List<AutocompletePredictionItem>?> get() = _predictionList

    private val _placeId = MutableStateFlow("")
    val placeId: StateFlow<String> get() = _placeId
    /**
     * 변수 세팅 끝
     */

    fun fetchDirections(request: DirectionsRequest) {
        viewModelScope.launch {
            getDirectionsUseCase(request).collectLatest { response: DirectionsResponse ->
                _directions.value = response
            }
        }
    }

    fun fetchSelectedMarker() {

    }

    // 검색 결과 저장
    fun fetchPredictions(itemList: List<AutocompletePredictionItem>) {
        viewModelScope.launch {
            try {
                _predictionList.value = itemList
            }catch (e: NullPointerException){
                e.printStackTrace()
            }

        }
    }

    // 현재 위치 저장
    fun fetchCurrentLatLng(latLng: LatLng) {
        viewModelScope.launch {
            try {
                _currentLatLng.value = latLng
            }catch (e: NullPointerException){
                e.printStackTrace()
            }
        }
    }

    // 장소 세부정보 갱신
    fun fetchPlaceDetails() {
        viewModelScope.launch {
            try {
                _placeId.collectLatest {
                     getPlaceDetailUseCase(_placeId.value)
                         .onStart { _placeDetails.value = LatestUiState.Loading }
                         .catch {
                             e -> e.printStackTrace()
                             _placeDetails.value = LatestUiState.Error(e)
                         }
                         .collectLatest {
                         Log.d("TAG", "fetchPlaceDetails: _placeId: ${_placeId.value}")
                         Log.d("TAG", "fetchPlaceDetails: _placeDetails: ${_placeDetails.value}")
                         _placeDetails.value = LatestUiState.Success(it)
                         _placeDetailsResult.value = it.result
                     }
                }
            }catch (e: Throwable){
                e.printStackTrace()
            }
        }
    }

    // 위치 정보 가져오기
    fun fetchLatLngToPlaceId(latLng: LatLng? = null, getPlaceId: String = "") {
        viewModelScope.launch {
            Log.d("TAG", "fetchLatLngToPlaceId: launch")
            try {
                if (latLng != null){
                    Log.d("TAG", "fetchLatLngToPlaceId: ${latLng.latitude},${latLng.longitude}")
                    val latLng = "${latLng.latitude},${latLng.longitude}"
                    getPlaceIdUseCase(latLng).collect {
                        val placeId = it.results.firstOrNull()?.placeId
                        if (placeId != null) {
                            _placeId.value = placeId
                        } else {
                            Log.d("TAG", "fetchPlaceId: null ")
                            _placeId.value = "Place ID is null"
                        }
                    }
                }
                else if(getPlaceId.isEmpty()){
                    Log.d("TAG", "fetchPlaceId: empty ")
                    _placeId.value = "nothing insert"
                }else {
                    Log.d("TAG", "fetchPlaceId: $getPlaceId ")
                    _placeId.value = getPlaceId
                }
            } catch (e: Exception) {

                Log.d("TAG", "fetchPlaceId: ${ e.printStackTrace()}")
                _placeId.value = e.toString()
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
}