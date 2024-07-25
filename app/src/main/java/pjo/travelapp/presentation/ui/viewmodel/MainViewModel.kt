package pjo.travelapp.presentation.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.fragment.app.Fragment
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
import pjo.travelapp.domain.usecase.GetPlaceDetailUseCase
import pjo.travelapp.presentation.ui.fragment.RecycleItemFragment
import pjo.travelapp.presentation.util.LatestUiState
import java.util.Random
import javax.inject.Inject

private fun <T> List<T>.shuffled(): List<T> {
    return this.shuffled(Random(System.currentTimeMillis()))
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private var placesClient: PlacesClient
) : ViewModel() {

    /**
     * 변수 선언
     */
    private val types = listOf("tourist_attraction", "restaurant", "park", "cafe")

    private val _tokyoHotPlaceList =
        MutableStateFlow<LatestUiState<List<Pair<Place, Bitmap?>>>>(LatestUiState.Loading)
    val tokyoHotPlaceList: StateFlow<LatestUiState<List<Pair<Place, Bitmap?>>>> get() = _tokyoHotPlaceList

    private val _fukuokaHotPlaceList =
        MutableStateFlow<LatestUiState<List<Pair<Place, Bitmap?>>>>(LatestUiState.Loading)
    val fukuokaHotPlaceList: StateFlow<LatestUiState<List<Pair<Place, Bitmap?>>>> get() = _fukuokaHotPlaceList

    private val _parisHotPlaceList =
        MutableStateFlow<LatestUiState<List<Pair<Place, Bitmap?>>>>(LatestUiState.Loading)
    val parisHotPlaceList: StateFlow<LatestUiState<List<Pair<Place, Bitmap?>>>> get() = _parisHotPlaceList

    private val _voiceString = MutableStateFlow("")
    val voiceString: StateFlow<String> get() = _voiceString

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> get() = _inputText

    private val _placeDetailsList = MutableStateFlow<List<Pair<Place, Bitmap?>>>(emptyList())
    val placeDetailsList: StateFlow<List<Pair<Place, Bitmap?>>> get() = _placeDetailsList

    private val _nearbySearch =
        MutableStateFlow<LatestUiState<List<Pair<Place, Bitmap?>>>>(LatestUiState.Loading)
    val nearbySearch: StateFlow<LatestUiState<List<Pair<Place, Bitmap?>>>> get() = _nearbySearch

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> get() = _currentLocation

    private val _promotionData = MutableStateFlow<LatestUiState<List<Int>>>(LatestUiState.Loading)
    val promotionData: StateFlow<LatestUiState<List<Int>>> get() = _promotionData

    // 캐시용 변수 추가
    private val placeDetailCache = mutableMapOf<String, Place>()

    private val _shuffledHotPlaceList =
        MutableStateFlow<LatestUiState<List<Pair<Place, Bitmap?>>>>(LatestUiState.Loading)
    val shuffledHotPlaceList: StateFlow<LatestUiState<List<Pair<Place, Bitmap?>>>> get() = _shuffledHotPlaceList

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
    /**
     * 변수 선언 끝
     */

    init {
        fetchQueryTextSearch()
        observeHotPlaceLists()
        fetchPromotion()
    }

    private fun fetchPromotion() {
        _promotionData.value = LatestUiState.Success(listOf(
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
        hotPlaceList: StateFlow<LatestUiState<List<Pair<Place, Bitmap?>>>>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            hotPlaceList.collectLatest { state ->
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
            val tokyoList = (_tokyoHotPlaceList.value as? LatestUiState.Success)?.data ?: emptyList()
            val fukuokaList = (_fukuokaHotPlaceList.value as? LatestUiState.Success)?.data ?: emptyList()
            val parisList = (_parisHotPlaceList.value as? LatestUiState.Success)?.data ?: emptyList()

            val combinedList = (tokyoList + fukuokaList + parisList)
                .shuffled()
                .sortedByDescending { it.first.rating } // 평점 높은 순으로 정렬

            combinedList.forEach {
                Log.d("TAG", "viewmodel shuffleAndDistribute: ${it.first.name}")
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

    fun fetchVoiceString(voice: String) {
        _voiceString.value = voice
    }

    private fun clearList() {
        _placeDetailsList.value = emptyList()
    }

    private fun fetchQueryTextSearch() {
        val token = AutocompleteSessionToken.newInstance()
        val placesWithPhotos = mutableListOf<Pair<Place, Bitmap?>>()
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.PHOTO_METADATAS
        )

        viewModelScope.launch {
            inputText.collectLatest {
                clearList()
                val request = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(token)
                    .setTypesFilter(listOf("restaurant", "tourist_attraction", "cafe"))
                    .setQuery(it)
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
                        placeDetailCache[pred.placeId]?.let { cachedResult ->
                            addPlaceToResults(cachedResult, placesWithPhotos)
                        } ?: run {
                            fetchAndCachePlaceDetails(pred.placeId, placeFields, placesWithPhotos)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun fetchAndCachePlaceDetails(
        placeId: String,
        placeFields: List<Place.Field>,
        placesWithPhotos: MutableList<Pair<Place, Bitmap?>>
    ) {
        val req = FetchPlaceRequest.builder(placeId, placeFields).build()
        val placeDetails = placesClient.fetchPlace(req).await().place

        placeDetailCache[placeId] = placeDetails
        addPlaceToResults(placeDetails, placesWithPhotos)
    }

    private fun addPlaceToResults(
        place: Place,
        placesWithPhotos: MutableList<Pair<Place, Bitmap?>>
    ) {
        viewModelScope.launch {
            if (placesWithPhotos.none { it.first.name == place.name }) {
                val photoMetadatas = place.photoMetadatas
                val photoBitmap =
                    if (!photoMetadatas.isNullOrEmpty()) fetchPhoto(photoMetadatas) else null
                placesWithPhotos.add(Pair(place, photoBitmap))
                _placeDetailsList.value = placesWithPhotos
            }
        }
    }

    fun fetchCurrentLocation(lt: LatLng) {
        _currentLocation.value = lt
        /*fetchNearbyTouristAttractions()*/
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
            val placesWithPhotos = mutableListOf<Pair<Place, Bitmap?>>()

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

                Log.d("TAG", "fetchNearbyTouristAttractions: ${res.places}")
                newPlaces.forEach {
                    val photoMetadatas = it.photoMetadatas
                    if (!photoMetadatas.isNullOrEmpty()) {
                        val photoBitmap = fetchPhoto(photoMetadatas)
                        placesWithPhotos.add(Pair(it, photoBitmap))
                    } else {
                        placesWithPhotos.add(Pair(it, null))
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
        val placesWithPhotos = mutableListOf<Pair<Place, Bitmap?>>()
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
                            placesWithPhotos.add(Pair(it, fetchPhoto(photoMetadatas)))
                        } else {
                            placesWithPhotos.add(Pair(it, null))
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
            val placesWithPhotos = mutableListOf<Pair<Place, Bitmap?>>()
            val placeIds = mutableSetOf<String>() // Place ID를 저장할 Set 추가
            types.forEach { type ->
                val query = "tourist attractions in $city"
                val searchByTextRequest = SearchByTextRequest.builder(query, placeFields)
                    .setMaxResultCount(2)
                    .setMinRating(3.5)
                    .setIncludedType(type)
                    .setRankPreference(SearchByTextRequest.RankPreference.DISTANCE)
                    .build()
                try {
                    val response = placesClient.searchByText(searchByTextRequest).await()
                    response.places.forEach { place ->
                        if (place.id!! !in placeIds) { // 중복 여부 확인
                            val photoMetadatas = place.photoMetadatas
                            if (!photoMetadatas.isNullOrEmpty()) {
                                placesWithPhotos.add(Pair(place, fetchPhoto(photoMetadatas)))
                            } else {
                                placesWithPhotos.add(Pair(place, null))
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
    }

    private fun updatePlaceList(loc: String, newPlaces: List<Pair<Place, Bitmap?>>) {
        when (loc) {
            "도쿄" -> _tokyoHotPlaceList.value = LatestUiState.Success(newPlaces)
            "후쿠오카" -> _fukuokaHotPlaceList.value = LatestUiState.Success(newPlaces)
            "파리" -> _parisHotPlaceList.value = LatestUiState.Success(newPlaces)
            "근처" -> _nearbySearch.value = LatestUiState.Success(newPlaces)
            else -> {

            }
        }
    }

    private fun updateUiState(city: String, state: LatestUiState<List<Pair<Place, Bitmap?>>>) {
        when (city) {
            "도쿄" -> _tokyoHotPlaceList.value = state
            "후쿠오카" -> _fukuokaHotPlaceList.value = state
            "파리" -> _parisHotPlaceList.value = state
            "근처" -> _nearbySearch.value = state

            else -> {

            }
        }
    }

    private suspend fun fetchPhoto(photoMetadatas: MutableList<PhotoMetadata>?): Bitmap? {
        return withContext(Dispatchers.IO) {
            if (photoMetadatas.isNullOrEmpty()) {
                Log.e("TAG", "Photo metadatas are null or empty")
                return@withContext null
            }

            try {
                val photoRequest = FetchPhotoRequest.builder(photoMetadatas.first())
                    .setMaxWidth(800)
                    .setMaxHeight(1200)
                    .build()
                val fetchPhotoResponse = placesClient.fetchPhoto(photoRequest).await()
                fetchPhotoResponse.bitmap
            } catch (e: Exception) {
                Log.e("TAG", "fetchPhoto: $e")
                null
            }
        }
    }
}
