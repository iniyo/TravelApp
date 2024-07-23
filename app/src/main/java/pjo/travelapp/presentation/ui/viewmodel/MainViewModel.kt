package pjo.travelapp.presentation.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pjo.travelapp.data.entity.AutocompletePredictionItem
import pjo.travelapp.data.entity.NearbySearchResponse
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.domain.usecase.GetPlaceDetailUseCase
import pjo.travelapp.presentation.util.LatestUiState
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private var placesClient: PlacesClient,
    private val getPlaceDetailUseCase: GetPlaceDetailUseCase
) : ViewModel() {

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

    private val _voiceString = MutableStateFlow<String>("")
    val voiceString: StateFlow<String> get() = _voiceString

    private val _inputText = MutableStateFlow<String>("")
    val inputText: StateFlow<String> get() = _inputText

    private val _placeDetailResult = MutableStateFlow<PlaceResult?>(null)
    val placeDetailResult: StateFlow<PlaceResult?> get() = _placeDetailResult

    private val _placeDetailsList = MutableStateFlow<List<PlaceResult>>(emptyList())
    val placeDetailsList: StateFlow<List<PlaceResult>> get() = _placeDetailsList

    private val _nearbySearch =
        MutableStateFlow<LatestUiState<List<NearbySearchResponse>>>(LatestUiState.Loading)
    val nearbySearch: StateFlow<LatestUiState<List<NearbySearchResponse>>> get() = _nearbySearch

    // 캐시용 변수 추가
    private val placeDetailCache = mutableMapOf<String, PlaceResult>()

    init {
        fetchQueryTextSearch()
    }
    private val placeFields = listOf(
        Place.Field.ID,
        Place.Field.NAME,
        Place.Field.ADDRESS,
        Place.Field.RATING,
        Place.Field.PHOTO_METADATAS
    )

    fun fetchData() {
        fetchTopRatedTouristAttractions("Tokyo")
        fetchTopRatedTouristAttractions("Fukuoka")
        fetchTopRatedTouristAttractions("Paris")
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
                        AutocompletePredictionItem(pred.placeId, pred.getPrimaryText(null).toString())
                    }

                    preds.forEach { pred ->
                        if (placeDetailCache.containsKey(pred.placeId)) {
                            val cachedResult = placeDetailCache[pred.placeId]
                            cachedResult?.let {
                                val currentList = _placeDetailsList.value.toMutableList()
                                if (currentList.none { cl -> cl.name == it.name }) {
                                    currentList.add(it)
                                    _placeDetailsList.value = currentList
                                }
                            }
                        } else {
                            getPlaceDetailUseCase(pred.placeId).collectLatest { res ->
                                placeDetailCache[pred.placeId] = res.result
                                val currentList = _placeDetailsList.value.toMutableList()
                                if (currentList.none { cl -> cl.name == res.result.name }) {
                                    currentList.add(res.result)
                                    _placeDetailsList.value = currentList
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

 /*   private fun fetchNearbyTouristAttractions() {
        val query = "popular tourist attractions japan, europe"
        viewModelScope.launch {
            val searchByTextRequest = SearchNearbyRequest.builder()
                .setMaxResultCount(1)
                .setRankPreference(SearchNearbyRequest.RankPreference.DISTANCE)
                .setPlaceFields(placeFields)
                .build()

            val response = placesClient.searchNearby(searchByTextRequest).await()
            val places = response.places

            placesClient.searchNearby()
        }
    }*/

    private fun fetchPopularTouristAttractions() {
        val query = "popular tourist attractions japan, europe"
        val placesWithPhotos = mutableListOf<Pair<Place, Bitmap?>>()
        viewModelScope.launch {
            val searchByTextRequest = SearchByTextRequest.builder(query, placeFields)
                .setMaxResultCount(1)
                .setMinRating(3.5)
                .setIncludedType("tourist_attraction")
                .setRankPreference(SearchByTextRequest.RankPreference.DISTANCE)
                .build()

           try {
               placesClient.searchByText(searchByTextRequest).result.places.forEach {
                   val photoMetadatas = it.photoMetadatas
                   if (!photoMetadatas.isNullOrEmpty()) {
                       val photoRequest = FetchPhotoRequest.builder(photoMetadatas.first())
                           .setMaxWidth(800)
                           .setMaxHeight(1200)
                           .build()
                       val fetchPhotoResponse = placesClient.fetchPhoto(photoRequest).await()
                       val photoBitmap = fetchPhotoResponse.bitmap
                       placesWithPhotos.add(Pair(it, photoBitmap))
                   } else {
                       placesWithPhotos.add(Pair(it, null))
                   }
               }
           }catch (e: Throwable) {
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
                        if (place.id !in placeIds) { // 중복 여부 확인
                            val photoMetadatas = place.photoMetadatas
                            if (!photoMetadatas.isNullOrEmpty()) {
                                val photoRequest = FetchPhotoRequest.builder(photoMetadatas.first())
                                    .setMaxWidth(800)
                                    .setMaxHeight(1200)
                                    .build()
                                val fetchPhotoResponse = placesClient.fetchPhoto(photoRequest).await()
                                val photoBitmap = fetchPhotoResponse.bitmap
                                placesWithPhotos.add(Pair(place, photoBitmap))
                            } else {
                                placesWithPhotos.add(Pair(place, null))
                            }
                            place.id?.let { placeIds.add(it) } // Set에 Place ID 추가
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    updateUiState(city, LatestUiState.Error(e))
                }
            }
            updatePlaceList(city, placesWithPhotos)
        }
    }

    private fun updatePlaceList(city: String, newPlaces: List<Pair<Place, Bitmap?>>) {
        when (city) {
            "Tokyo" -> {
                _tokyoHotPlaceList.value = LatestUiState.Success(newPlaces)
            }

            "Fukuoka" -> {
                _fukuokaHotPlaceList.value = LatestUiState.Success(newPlaces)
            }

            "Paris" -> {
                _parisHotPlaceList.value = LatestUiState.Success(newPlaces)
            }
        }
    }

    private fun updateUiState(city: String, state: LatestUiState<List<Pair<Place, Bitmap?>>>) {
        when (city) {
            "Tokyo" -> {
                _tokyoHotPlaceList.value = state
            }

            "Fukuoka" -> {
                _fukuokaHotPlaceList.value = state
            }

            "Paris" -> {
                _parisHotPlaceList.value = state
            }
        }
    }
}
