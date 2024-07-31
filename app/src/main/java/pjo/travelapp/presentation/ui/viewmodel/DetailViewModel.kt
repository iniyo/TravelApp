package pjo.travelapp.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import pjo.travelapp.data.entity.HotelCard
import pjo.travelapp.data.entity.PlaceDetail
import pjo.travelapp.presentation.util.LatestUiState
import javax.inject.Inject


@HiltViewModel
class DetailViewModel @Inject constructor(

) : ViewModel() {
    private val _placeDetails = MutableStateFlow<PlaceDetail?>(null)
    val placeDetails: StateFlow<PlaceDetail?> get() = _placeDetails

    private val _shuffledHotPlaceList =
        MutableStateFlow<LatestUiState<List<PlaceDetail>>>(LatestUiState.Loading)
    val shuffledHotPlaceList: StateFlow<LatestUiState<List<PlaceDetail>>> get() = _shuffledHotPlaceList

    private val _hotelDetail = MutableStateFlow<HotelCard?>(null)
    val hotelDetail: StateFlow<HotelCard?> get() = _hotelDetail


    fun fetchPlaceDetails(placeDetail: PlaceDetail) {
        _placeDetails.value = placeDetail
    }

    fun fetchHotelDetail(hotelDetail: HotelCard) {
        _hotelDetail.value = hotelDetail
    }
}