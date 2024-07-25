package pjo.travelapp.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.data.entity.TravelDestinationAbroad
import pjo.travelapp.data.entity.TravelDestinationDomestic
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

    private var _selectedPlaceNameList = MutableStateFlow(ArrayList<String>())
    val selectedPlaceNameList: StateFlow<List<String>> get() = _selectedPlaceNameList

    private var _selectedCalendarDatePeriod = MutableStateFlow<String>("")
    val selectedCalendarDatePeriod: StateFlow<String> get() = _selectedCalendarDatePeriod

    private val _domesticPlace = MutableStateFlow<TravelDestinationDomestic?>(null)
    val domesticPlace: StateFlow<TravelDestinationDomestic?> get() = _domesticPlace

    private val _abroadPlace = MutableStateFlow<TravelDestinationAbroad?>(null)
    val abroadPlace: StateFlow<TravelDestinationAbroad?> get() = _abroadPlace

    init {
        fetchPlaceAndDate()
        fetchPlaceList()
        fetchDomesticPlace()
        fetchAbroadPlace()
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

    private fun fetchPlaceList() {
        viewModelScope.launch {
            selectedPlaceName.collectLatest {
                _selectedPlaceNameList.value.add(it)
            }
        }
    }

    fun fetchUserSchedule() {

    }


    fun fetchDomesticPlace() {
        _domesticPlace.value = TravelDestinationDomestic()
    }

    fun fetchAbroadPlace() {
        _abroadPlace.value = TravelDestinationAbroad()
    }

    private fun fetchPlaceAndDate() {
        var place = ""
        var trip = ""
        var placeAndDate = ""
        viewModelScope.launch {
            tripPeriod.collectLatest {
                trip = it
            }
            selectedPlaceName.collectLatest {
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