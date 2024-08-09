package pjo.travelapp.presentation.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pjo.travelapp.R
import pjo.travelapp.data.entity.AutocompletePredictionItem
import pjo.travelapp.data.entity.FireStoreNotice
import pjo.travelapp.data.entity.HotelCard
import pjo.travelapp.data.entity.PlaceDetail
import pjo.travelapp.data.repo.HotelRepository
import pjo.travelapp.data.repo.NoticeRepository
import pjo.travelapp.data.repo.NoticeRepositoryImpl
import pjo.travelapp.presentation.util.LatestUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Random
import javax.inject.Inject

private fun <T> List<T>.shuffled(): List<T> {
    return this.shuffled(Random(System.currentTimeMillis()))
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private var placesClient: PlacesClient,
    private val hotelRepo: HotelRepository,
    private val noticeRepository: NoticeRepository,
) : ViewModel() {

    /**
     * 변수 선언
     */
    private val _tokyoHotPlaceList =
        MutableStateFlow<LatestUiState<List<PlaceDetail>>>(LatestUiState.Loading)
    val tokyoHotPlaceList: StateFlow<LatestUiState<List<PlaceDetail>>> get() = _tokyoHotPlaceList

    private val _fukuokaHotPlaceList =
        MutableStateFlow<LatestUiState<List<PlaceDetail>>>(LatestUiState.Loading)
    val fukuokaHotPlaceList: StateFlow<LatestUiState<List<PlaceDetail>>> get() = _fukuokaHotPlaceList

    private val _parisHotPlaceList =
        MutableStateFlow<LatestUiState<List<PlaceDetail>>>(LatestUiState.Loading)
    val parisHotPlaceList: StateFlow<LatestUiState<List<PlaceDetail>>> get() = _parisHotPlaceList

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> get() = _inputText

    private val _placeDetailsList = MutableStateFlow<List<PlaceDetail>>(emptyList())
    val placeDetailsList: StateFlow<List<PlaceDetail>> get() = _placeDetailsList

    private val _nearbySearch =
        MutableStateFlow<LatestUiState<List<PlaceDetail>>>(LatestUiState.Loading)
    val nearbySearch: StateFlow<LatestUiState<List<PlaceDetail>>> get() = _nearbySearch

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> get() = _currentLocation

    private val _promotionData = MutableStateFlow<LatestUiState<List<Int>>>(LatestUiState.Loading)
    val promotionData: StateFlow<LatestUiState<List<Int>>> get() = _promotionData

    private val _hotelState =
        MutableStateFlow<LatestUiState<List<HotelCard>>>(LatestUiState.Loading)
    val hotelState: StateFlow<LatestUiState<List<HotelCard>>> = _hotelState

    // 캐시용 변수 추가
    private val placeDetailCache = mutableMapOf<String, Place>()

    private val _shuffledHotPlaceList =
        MutableStateFlow<LatestUiState<List<PlaceDetail>>>(LatestUiState.Loading)
    val shuffledHotPlaceList: StateFlow<LatestUiState<List<PlaceDetail>>> get() = _shuffledHotPlaceList

    private val _noticeData = MutableStateFlow<List<FireStoreNotice>>(emptyList())
    val noticeData: StateFlow<List<FireStoreNotice>> get() = _noticeData

    // 공통 placeFields
    private val placeFields = listOf(
        Place.Field.ID,
        Place.Field.NAME,
        Place.Field.RATING,
        Place.Field.PHOTO_METADATAS,
        Place.Field.REVIEWS
    )
    private var isTokyoListInitialized = false
    private var isFukuokaListInitialized = false
    private var isParisListInitialized = false
    private var checkin: String = ""
    private var checkout: String = ""

    /**
     * 변수 선언 끝
     */

    init {
        fetchQueryTextSearch()
        observeHotPlaceLists()
        fetchPromotion()
    }

    fun fetchNoticeData() {
        viewModelScope.launch {
            try {
                (noticeRepository as NoticeRepositoryImpl).fetchAndSaveNotices()
                _noticeData.value = noticeRepository.getNotices()
            } catch (e: Exception) {
                Log.e("TAG", "fetchNoticeData: ", e)
            }
        }
    }

    fun updateNotice(notice: FireStoreNotice) {
        viewModelScope.launch {
            noticeRepository.updateNotice(notice)
            _noticeData.value = noticeRepository.getNotices() // 데이터 업데이트 후 다시 가져오기
        }
    }

    fun setDates() {
        val dates = getCheckinCheckoutDates()
        checkin = dates.first
        checkout = dates.second
    }

    fun searchHotels(cityName: String) {
        viewModelScope.launch {
            try {
                Log.d("TAG", "searchHotels first: $")
                val autoCompleteResponse = hotelRepo.autoComplete(cityName)
                Log.d("TAG", "searchHotels sec: $autoCompleteResponse")
                if (autoCompleteResponse.status && autoCompleteResponse.data.isNotEmpty()) {
                    Log.d("TAG", "searchHotels: $autoCompleteResponse")
                    val cityEntity =
                        autoCompleteResponse.data.firstOrNull { it.entityType == "city" }
                    val entityId = cityEntity?.entityId
                    if (entityId != null) {
                        val response = hotelRepo.searchHotels(entityId, checkin, checkout)
                        _hotelState.value = LatestUiState.Success(response.data.results.hotelCards)
                    } else {
                        _hotelState.value = LatestUiState.Error(Exception("City entity not found"))
                    }
                } else {
                    _hotelState.value = LatestUiState.Error(Exception("City not found"))
                }
            } catch (e: Exception) {
                _hotelState.value = LatestUiState.Error(e)
            }
        }
    }

    private fun getCheckinCheckoutDates(): Pair<String, String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // 현재 시간이 오후 3시 이후인지 확인
        if (calendar.get(Calendar.HOUR_OF_DAY) >= 15) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val checkin = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, 3)
        val checkout = dateFormat.format(calendar.time)

        return Pair(checkin, checkout)
    }

    private fun fetchPromotion() {
        _promotionData.value = LatestUiState.Success(
            listOf(
                R.drawable.banner1,
                R.drawable.banner2
            )
        )
    }

    private fun observeHotPlaceLists() {
        viewModelScope.launch {
            observeHotPlaceList(tokyoHotPlaceList) { isTokyoListInitialized = true }
            observeHotPlaceList(fukuokaHotPlaceList) { isFukuokaListInitialized = true }
            observeHotPlaceList(parisHotPlaceList) { isParisListInitialized = true }
        }
    }

    private fun observeHotPlaceList(
        hotPlaceList: StateFlow<LatestUiState<List<PlaceDetail>>>,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            hotPlaceList.collect { state ->
                if (state is LatestUiState.Success) {
                    onSuccess()
                    if (isAllListsInitialized()) {
                        shuffleAndDistribute()
                    }
                }
            }
        }
    }

    private fun isAllListsInitialized(): Boolean {
        return isTokyoListInitialized && isFukuokaListInitialized && isParisListInitialized
    }

    private fun shuffleAndDistribute() {
        viewModelScope.launch {
            val tokyoList =
                (_tokyoHotPlaceList.value as? LatestUiState.Success)?.data ?: emptyList()
            val fukuokaList =
                (_fukuokaHotPlaceList.value as? LatestUiState.Success)?.data ?: emptyList()
            val parisList =
                (_parisHotPlaceList.value as? LatestUiState.Success)?.data ?: emptyList()

            val combinedList = (tokyoList + fukuokaList + parisList)
                .shuffled()
                .sortedByDescending { it.place.rating } // 평점 높은 순으로 정렬

            combinedList.forEach {
                Log.d("TAG", "viewmodel shuffleAndDistribute: ${it.place.name}")
            }

            _shuffledHotPlaceList.value = LatestUiState.Success(combinedList)
        }
    }


    fun fetchData() {
        fetchTopRatedTouristAttractions("도쿄")
        fetchTopRatedTouristAttractions("후쿠오카")
        fetchTopRatedTouristAttractions("파리")
    }

    fun updateInputText(newText: String) {
        _inputText.value = newText
    }

    private fun clearList() {
        _placeDetailsList.value = emptyList()
    }

    private fun fetchQueryTextSearch() {
        val token = AutocompleteSessionToken.newInstance()
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.PHOTO_METADATAS
        )

        viewModelScope.launch {
            inputText.collectLatest { query ->
                if (query.isNotEmpty()) {
                    val placesWithPhotos = mutableListOf<PlaceDetail>()
                    val request = FindAutocompletePredictionsRequest.builder()
                        .setSessionToken(token)
                        .setTypesFilter(listOf("restaurant", "tourist_attraction", "cafe"))
                        .setQuery(query)
                        .build()

                    try {
                        val res = placesClient.findAutocompletePredictions(request).await()
                        val preds = res.autocompletePredictions.map { pred ->
                            AutocompletePredictionItem(
                                pred.placeId,
                                pred.getPrimaryText(null).toString()
                            )
                        }

                        preds.forEach { pred ->
                            fetchAndCachePlaceDetails(pred.placeId, placeFields, placesWithPhotos)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    _placeDetailsList.value = emptyList()
                }
            }
        }
    }


    private suspend fun fetchAndCachePlaceDetails(
        placeId: String,
        placeFields: List<Place.Field>,
        placesWithPhotos: MutableList<PlaceDetail>,
    ) {
        val req = FetchPlaceRequest.builder(placeId, placeFields).build()
        val placeDetails = placesClient.fetchPlace(req).await().place

        placeDetailCache[placeId] = placeDetails
        addPlaceToResults(placeDetails, placesWithPhotos)
    }

    private fun addPlaceToResults(
        place: Place,
        placesWithPhotos: MutableList<PlaceDetail>,
    ) {
        viewModelScope.launch {
            val photoMetadatas = place.photoMetadatas
            val photoBitmap =
                if (!photoMetadatas.isNullOrEmpty()) fetchPhotos(photoMetadatas) else null
            placesWithPhotos.add(PlaceDetail(place, photoBitmap))
            _placeDetailsList.value = placesWithPhotos.toList() // Immutable List로 업데이트
        }
    }

    fun fetchCurrentLocation(lt: LatLng) {
        _currentLocation.value = lt
        fetchNearbyTouristAttractions()
    }

    /* fun fetchNearbyPlacesFlow(): Flow<PagingData<Pair<Place, Bitmap?>>> {
         val currentLoc = _currentLocation.value ?: throw IllegalStateException("Location not set")
         return Pager(PagingConfig(pageSize = 4, initialLoadSize = 4)) {
             NearbyPlacePagingSource(placesClient, currentLoc, placeFields)
         }.flow.cachedIn(viewModelScope)
     }*/

    private fun fetchNearbyTouristAttractions() {
        Log.d("TAG", "fetchNearbyTouristAttractions: ")
        try {
            val placesWithPhotos = mutableListOf<PlaceDetail>()

            val currentLoc =
                LatLng(currentLocation.value!!.latitude, currentLocation.value!!.longitude)
            val bounds = CircularBounds.newInstance(currentLoc,  /* radius = */10000.0)
            val includedTypes = listOf("tourist_attraction", "restaurant", "cafe") // 포함
            val excludedTypes =
                listOf("meal_delivery", "indian_restaurant", "greek_restaurant") // 제외

            viewModelScope.launch {
                val searchByTextRequest = SearchNearbyRequest.builder(bounds, placeFields)
                    .setMaxResultCount(7)
                    .setIncludedTypes(includedTypes)
                    .setExcludedTypes(excludedTypes)
                    .setRankPreference(SearchNearbyRequest.RankPreference.DISTANCE)
                    .build()

                val res = placesClient.searchNearby(searchByTextRequest).await() //tasks api사용시 동기작업
                val newPlaces = res.places
                    .filter { it.photoMetadatas != null && it.photoMetadatas!!.isNotEmpty() && it.rating != null }
                    .sortedByDescending { it.rating } // 평점 순으로 정렬

                newPlaces.forEach {
                    val photoMetadatas = it.photoMetadatas
                    if (!photoMetadatas.isNullOrEmpty()) {
                        placesWithPhotos.add(PlaceDetail(it, fetchPhotos(photoMetadatas)))
                    } else {
                        placesWithPhotos.add(PlaceDetail(it, null))
                    }
                }
                updatePlaceList("근처", placesWithPhotos)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            updateUiState("근처", LatestUiState.Error(e))
        }
    }

    private fun fetchPopularTouristAttractions() {
        Log.d("TAG", "fetchPopularTouristAttractions: ")

        val query = "popular tourist attraction the world in best"
        val placesWithPhotos = mutableListOf<PlaceDetail>()
        viewModelScope.launch {
            val searchByTextRequest = SearchByTextRequest.builder(query, placeFields)
                .setMaxResultCount(1)
                .setMinRating(3.5)
                .setIncludedType("tourist_attraction")
                .setRankPreference(SearchByTextRequest.RankPreference.DISTANCE)
                .build()

            try {
                placesClient.searchByText(searchByTextRequest)
                    .result.places.forEach {
                        val photoMetadatas = it.photoMetadatas
                        if (!photoMetadatas.isNullOrEmpty()) {
                            placesWithPhotos.add(PlaceDetail(it, fetchPhotos(photoMetadatas)))
                        } else {
                            placesWithPhotos.add(PlaceDetail(it, null))
                        }
                    }

            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchTopRatedTouristAttractions(city: String) {
        Log.d("TAG", "fetchTopRatedTouristAttractions: started!")

        viewModelScope.launch {
            val placesWithPhotos = mutableListOf<PlaceDetail>()
            val placeIds = mutableSetOf<String>() // Place ID를 저장할 Set 추가
            val query = "tourist attractions in $city"
            val searchByTextRequest = SearchByTextRequest.builder(query, placeFields)
                .setMaxResultCount(2)
                .setMinRating(3.5)
                .setStrictTypeFiltering(true) // type에 설정된 타입에 일치하는 경우에만
                .setIncludedType("tourist_attraction")
                .setRankPreference(SearchByTextRequest.RankPreference.DISTANCE)
                .build()
            try {
                val response = placesClient.searchByText(searchByTextRequest).await()

                response.places.forEach { place ->
                    if (place.id!! !in placeIds) { // 중복 여부 확인
                        val photoMetadatas = place.photoMetadatas
                        if (!photoMetadatas.isNullOrEmpty()) {
                            placesWithPhotos.add(
                                PlaceDetail(
                                    place,
                                    fetchPhotos(photoMetadatas)
                                )
                            )
                        } else {
                            placesWithPhotos.add(PlaceDetail(place, null))
                        }
                        place.id?.let { placeIds.add(it) } // Set에 Place ID 추가
                    }
                }
                updatePlaceList(city, placesWithPhotos)
            } catch (e: Exception) {
                e.printStackTrace()
                updateUiState(city, LatestUiState.Error(e))
            }
        }
    }

    private fun updatePlaceList(loc: String, newPlaces: List<PlaceDetail>) {
        when (loc) {
            "도쿄" -> _tokyoHotPlaceList.value = LatestUiState.Success(newPlaces)
            "후쿠오카" -> _fukuokaHotPlaceList.value = LatestUiState.Success(newPlaces)
            "파리" -> _parisHotPlaceList.value = LatestUiState.Success(newPlaces)
            "근처" -> _nearbySearch.value = LatestUiState.Success(newPlaces)
            else -> {

            }
        }
    }

    private fun updateUiState(city: String, state: LatestUiState<List<PlaceDetail>>) {
        when (city) {
            "도쿄" -> _tokyoHotPlaceList.value = state
            "후쿠오카" -> _fukuokaHotPlaceList.value = state
            "파리" -> _parisHotPlaceList.value = state
            "근처" -> _nearbySearch.value = state

            else -> {

            }
        }
    }

    private suspend fun fetchPhotos(photoMetadatas: MutableList<PhotoMetadata>?): List<Bitmap?> {
        return withContext(Dispatchers.IO) {
            if (photoMetadatas.isNullOrEmpty()) {
                Log.e("TAG", "Photo metadatas are null or empty")
                return@withContext emptyList<Bitmap?>()
            }

            val bitmaps = mutableListOf<Bitmap?>()

            try {
                photoMetadatas.forEach { metadata ->
                    val photoRequest = FetchPhotoRequest.builder(metadata)
                        .setMaxWidth(800)
                        .setMaxHeight(1200)
                        .build()
                    val fetchPhotoResponse = placesClient.fetchPhoto(photoRequest).await()
                    bitmaps.add(fetchPhotoResponse.bitmap)
                }
            } catch (e: Exception) {
                Log.e("TAG", "fetchPhoto: $e")
            }
            bitmaps
        }
    }
}
