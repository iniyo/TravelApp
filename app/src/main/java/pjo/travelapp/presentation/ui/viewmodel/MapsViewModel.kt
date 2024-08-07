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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okio.IOException
import pjo.travelapp.data.entity.AutocompletePredictionItem
import pjo.travelapp.data.entity.PlaceDetailsResponse
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.data.entity.RoutesRequest
import pjo.travelapp.data.entity.RoutesResponse
import pjo.travelapp.domain.usecase.GetDirectionsUseCase
import pjo.travelapp.domain.usecase.GetPlaceDetailUseCase
import pjo.travelapp.domain.usecase.GetPlaceIdUseCase
import pjo.travelapp.presentation.util.LatestUiState
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class MapsViewModel @Inject constructor(
    private val getDirectionsUseCase: GetDirectionsUseCase,
    private val getPlaceIdUseCase: GetPlaceIdUseCase,
    private val getPlaceDetailUseCase: GetPlaceDetailUseCase,
    private val geocoder: Geocoder,
    private var placeClient: PlacesClient
) : ViewModel() {

    /**
     * 변수 선언
     * @param: direction: 출발지 목적지로 얻어오는 경로
     * @param: placeDetails: 장소 세부정보에 대한 상태, 결과값, 주소, 데이터 출처 저장
     * @param: placeDetailResult: 장소 세부정보 결과
     * @param: placeDetailResult: 장소 세부정보 결과 리스트
     * @param: predictionList: 검색 결과 리스트
     * @param: query: 검색결과 저장되는 변수 - 양방향 데이터 바인딩에 사용
     * @param: placeId: 검색, 선택으로 받아오는 place의 고유값
     * @param: startQuery: direction 출발지점
     * @param: endQuery: direction 도착지점
     */
    private val _directions =
        MutableStateFlow<LatestUiState<Pair<RoutesResponse?, Int>>>(LatestUiState.Loading)
    val directions: StateFlow<LatestUiState<Pair<RoutesResponse?, Int>>> get() = _directions

    private val _placeDetails =
        MutableStateFlow<LatestUiState<PlaceDetailsResponse?>>(LatestUiState.Loading)

    private val _placeDetailsResult = MutableStateFlow<PlaceResult?>(null)
    val placeDetailsResult: StateFlow<PlaceResult?> get() = _placeDetailsResult

    private val _placeDetailsList = MutableStateFlow<List<PlaceResult>>(emptyList())
    val placeDetailsList: StateFlow<List<PlaceResult>> get() = _placeDetailsList

    private val _predictionList = MutableStateFlow<List<AutocompletePredictionItem>>(emptyList())
    val predictionList: StateFlow<List<AutocompletePredictionItem>> get() = _predictionList

    val query = MutableStateFlow("") // 양방향 데이터바인딩 지원시

    private var _placeId = MutableStateFlow<String?>("")
    val placeId: StateFlow<String?> get() = _placeId

    private var _startQuery = MutableStateFlow<PlaceResult?>(null)
    val startQuery: StateFlow<PlaceResult?> get() = _startQuery

    private var _endQuery = MutableStateFlow<PlaceResult?>(null)
    val endQuery: StateFlow<PlaceResult?> get() = _endQuery

    /**
     * 변수 선언 끝
     */

    init {
        fetchPlaceDetailsList()
        // query 값 변경 시 300 millis이후에 검색 실행
        viewModelScope.launch {
            query
                .debounce(300)
                .distinctUntilChanged()
                .collect {
                    clearPlaceList()
                    performSearch(it)
                }
        }
        viewModelScope.launch {
            predictionList.collectLatest { predictions ->
                predictions.forEach { prediction ->
                    fetchPlaceDetails(prediction.placeId)
                }
            }
        }
    }

    /**
     * public fetch
     */

    // 출발지점
    fun fetchStartQuery(start: PlaceResult) {
        _startQuery.value = start
    }

    // 도착지점
    fun fetchEndQuery(end: PlaceResult) {
        _endQuery.value = end
    }

    // 거리 계산 결과
    fun fetchDirections(directionsRequest: RoutesRequest, color: Int) {
        viewModelScope.launch {
            getDirectionsUseCase(directionsRequest)
                .onStart {
                    _directions.value = LatestUiState.Loading
                }
                .catch { e ->
                    e.printStackTrace()
                    _directions.value = LatestUiState.Error(e)
                }
                .collectLatest {
                    _directions.value = LatestUiState.Success(Pair(it, color))
                }
        }
    }

    // 장소 세부정보 변수 및 리스트 변수 초기화
    fun fetchPlaceDetails(placeId: String) {
        Log.d("TAG", "fetchPlaceDetails: ")
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
                    _placeDetailsResult.value = it.result
                }
        }
    }

    // 리스트 클리어
    fun clearPlaceList() {
        _placeDetailsList.value = emptyList()
        _predictionList.value = emptyList()
    }

    // 시작지점, 출발지점 placeId 초기화
    fun getStartAndEndPlaceId(
        start: String?,
        end: String?,
        callback: (Pair<LatLng?, LatLng?>) -> Unit
    ) {
        viewModelScope.launch {
            val startDeferred = CompletableDeferred<LatLng?>()
            val endDeferred = CompletableDeferred<LatLng?>()

            if (start.isNullOrEmpty() || end.isNullOrEmpty()) {
                Log.d("TAG", "getStartAndEndPlaceId: null or empty")
            } else {
                searchLocation(start) { result ->
                    startDeferred.complete(result)
                }

                searchLocation(end) { result ->
                    endDeferred.complete(result)
                }
            }

            val startLocation = startDeferred.await()
            val endLocation = endDeferred.await()

            callback(Pair(startLocation, endLocation))
        }
    }

    // location 정보로 placeid 초기화
    fun fetchLatLngToPlaceId(latLng: LatLng) {
        viewModelScope.launch {
            val latLngString = "${latLng.latitude},${latLng.longitude}"
            getPlaceIdUseCase(latLngString).collect {
                val placeId = it.results.firstOrNull()?.placeId
                if (placeId != null) {
                    Log.d("TAG", "fetchLatLngToPlaceId: latLng")
                    _placeId.value = placeId
                    fetchPlaceDetails(placeId)
                }
            }
        }
    }

    // placeid로 placeid 초기화
    fun fetchPlaceId(getPlaceId: String) {
        viewModelScope.launch {
            if (getPlaceId.isNotEmpty()) {
                Log.d("TAG", "fetchLatLngToPlaceId: placeid")
                _placeId.value = getPlaceId
                fetchPlaceDetails(getPlaceId)
            } else {
                Log.d("TAG", "fetchLatLngToPlaceId: empty placeId")
            }
        }
    }

    /**
     * public fetch 끝
     */


    // 검색 요청을 실행하고 이전 작업을 취소하는 메서드
    private fun performSearch(query: String, currentLatLng: LatLng? = null) {
        viewModelScope.launch {
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

            placeClient.findAutocompletePredictions(request)
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

    // 세부정보 리스트 업데이트
    private fun fetchPlaceDetailsList() {
        viewModelScope.launch {
            _placeDetailsResult.collectLatest { placeResult ->
                placeResult?.let {
                    val currentList = _placeDetailsList.value.toMutableList()
                    // 현재 리스트에 있는 이름과 비교, 없으면 삽입
                    if (currentList.none { it.name == placeResult.name }) {
                        currentList.add(placeResult)
                        _placeDetailsList.value = currentList
                    }
                }
            }
        }
    }

    private fun fetchPlaceResult(res: PlaceResult) {
        viewModelScope.launch {
            _placeDetailsResult.value = res
        }
    }

    @Suppress("DEPRECATION")
    private fun searchLocation(location: String, callback: (LatLng?) -> Unit) {
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
