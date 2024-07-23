package pjo.travelapp.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PlanViewModel @Inject constructor(

) : ViewModel() {

    private var _selectedCalendarDate = MutableStateFlow<String>("")
    val selectedCalendarDate: StateFlow<String> get() = _selectedCalendarDate

    private var _tripPeriod = MutableStateFlow<String>("")
    val tripPeriod: StateFlow<String> get() = _tripPeriod

    private var _selectedPlaceName = MutableStateFlow<String>("")
    val selectedPlaceName: StateFlow<String> get() = _selectedPlaceName

    private var _selectedCalendarDatePeriod = MutableStateFlow<String>("")
    val selectedCalendarDatePeriod: StateFlow<String> get() = _selectedCalendarDatePeriod


    init {
        fetchPlaceAndDate()
    }

    private fun fetchSelectedCalendarDate(date: String) {
        _selectedCalendarDate.value = date
    }

    fun fetchTripPeriod(date: String) {
        _tripPeriod.value = date
    }

    fun fetchPlace(date: String) {
        _selectedPlaceName.value = date
    }

    fun fetchSelectedCalendarDatePeriod(date: CharSequence) {
        _selectedCalendarDatePeriod.value = date.toString()
    }

    private fun fetchPlaceAndDate() {
        var place = ""
        var trip = ""
        var placeAndDate = ""
        viewModelScope.launch {
            tripPeriod.collectLatest {
                trip = ""
                trip = it
            }
            selectedPlaceName.collectLatest {
                place = ""
                place = it
            }

            if (place.isNotEmpty() && trip.isNotEmpty()) {
                placeAndDate = trip + "일간의" + place + " 여행 일정 계획"
                fetchSelectedCalendarDate(placeAndDate)
            } else {
                fetchSelectedCalendarDate("기간 혹은 장소가 선택되지 않았습니다.")
            }
        }
    }
}