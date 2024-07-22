package pjo.travelapp.presentation.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pjo.travelapp.presentation.util.LatestUiState
import javax.inject.Inject

@HiltViewModel
class BaseViewModel @Inject constructor(
    private var placesClient: PlacesClient
) : ViewModel() {

    fun fetchData() {
        fetchTopRatedTouristAttractions("Tokyo")
        fetchTopRatedTouristAttractions("Fukuoka")
        fetchTopRatedTouristAttractions("Paris")
    }

    private val types = listOf("restaurant", "museum", "park", "cafe")

    private val _tokyoHotPlaceList = MutableStateFlow<LatestUiState<List<Pair<Place, Bitmap?>>>>(LatestUiState.Loading)
    val tokyoHotPlaceList: StateFlow<LatestUiState<List<Pair<Place, Bitmap?>>>> get() = _tokyoHotPlaceList

    private val _fukuokaHotPlaceList = MutableStateFlow<LatestUiState<List<Pair<Place, Bitmap?>>>>(LatestUiState.Loading)
    val fukuokaHotPlaceList: StateFlow<LatestUiState<List<Pair<Place, Bitmap?>>>> get() = _fukuokaHotPlaceList

    private val _parisHotPlaceList = MutableStateFlow<LatestUiState<List<Pair<Place, Bitmap?>>>>(LatestUiState.Loading)
    val parisHotPlaceList: StateFlow<LatestUiState<List<Pair<Place, Bitmap?>>>> get() = _parisHotPlaceList


    private fun fetchTopRatedTouristAttractions(city: String) {
        try {

            Log.d("TAG", "fetchTopRatedTouristAttractions: started!")
            val placeFields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.RATING,
                Place.Field.PHOTO_METADATAS
            )

            viewModelScope.launch {
                val placesWithPhotos = mutableListOf<Pair<Place, Bitmap?>>()
                types.forEach { type ->
                    val query = "tourist attractions in $city"
                    val searchByTextRequest = SearchByTextRequest.builder(query, placeFields)
                        .setMaxResultCount(10)
                        .setMinRating(3.5)
                        .setIncludedType(type)
                        .setRankPreference(SearchByTextRequest.RankPreference.DISTANCE)
                        .build()

                    try {
                        val response = placesClient.searchByText(searchByTextRequest).await()
                        response.places.forEach { place ->
                            val photoMetadatas = place.photoMetadatas
                            if (photoMetadatas != null && photoMetadatas.isNotEmpty()) {
                                val photoRequest = FetchPhotoRequest.builder(photoMetadatas.first())
                                    .setMaxWidth(800)
                                    .setMaxHeight(1200)
                                    .build()
                                try {
                                    val fetchPhotoResponse = placesClient.fetchPhoto(photoRequest).await()
                                    val photoBitmap = fetchPhotoResponse.bitmap
                                    placesWithPhotos.add(Pair(place, photoBitmap))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    placesWithPhotos.add(Pair(place, null))
                                }
                            } else {
                                placesWithPhotos.add(Pair(place, null))
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        updateUiState(city, LatestUiState.Error(e))
                    }
                }
                updatePlaceList(city, placesWithPhotos)
            }
        }catch (e: Exception){
            e.printStackTrace()
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
