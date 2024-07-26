package pjo.travelapp.presentation.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.model.Place
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import pjo.travelapp.data.datasource.UserScheduleDao
import pjo.travelapp.data.entity.TravelDestinationAbroad
import pjo.travelapp.data.entity.TravelDestinationDomestic
import pjo.travelapp.data.entity.UserSchduleEntity
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val userScheduleDao: UserScheduleDao
) : ViewModel() {

    private var _selectedCalendarDate = MutableStateFlow<String>("")
    val selectedCalendarDate: StateFlow<String> get() = _selectedCalendarDate

    private var _tripPeriod = MutableStateFlow<Int>(0)
    val tripPeriod: StateFlow<Int> get() = _tripPeriod

    private var _selectedCalendarDatePeriod = MutableStateFlow<String>("")
    val selectedCalendarDatePeriod: StateFlow<String> get() = _selectedCalendarDatePeriod

    private val _domesticPlace = MutableStateFlow<TravelDestinationDomestic?>(null)
    private val _abroadPlace = MutableStateFlow<TravelDestinationAbroad?>(null)
    val abroadPlace: StateFlow<TravelDestinationAbroad?> get() = _abroadPlace

    private var _title = MutableStateFlow(String())
    val title: StateFlow<String> get() = _title

    private val _planAdapterList = MutableStateFlow<List<Pair<Int, Int>>>(emptyList())
    val planAdapterList: StateFlow<List<Pair<Int, Int>>> get() = _planAdapterList

    private val _userScheduleList = MutableStateFlow<List<UserSchduleEntity>>(emptyList())
    val userScheduleList: StateFlow<List<UserSchduleEntity>> get() = _userScheduleList

    private val _selectedPlace = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val selectedPlace: StateFlow<List<Pair<String, Int>>> get() = _selectedPlace

    init {
        fetchDomesticPlace()
        fetchAbroadPlace()
    }

    fun fetchTripPeriod(date: Int) {
        _tripPeriod.value = date
    }

    fun fetchSelectedCalendarDatePeriod(date: CharSequence) {
        _selectedCalendarDatePeriod.value = date.toString()
    }

    fun fetchUserSchedule(userSchduleEntity: UserSchduleEntity) {
        viewModelScope.launch {
            userScheduleDao.insertSchedule(userSchduleEntity)
            _userScheduleList.value = userScheduleDao.getAllSchedules()
        }
    }

    fun fetchUserSchedules() {
        viewModelScope.launch {
            _userScheduleList.value = userScheduleDao.getAllSchedules()
        }
    }

    fun fetchUserAdapter(selectedMonthsAndDays: List<Pair<Int, Int>>?) {
        if (!selectedMonthsAndDays.isNullOrEmpty()) {
            _planAdapterList.value = selectedMonthsAndDays
        }
    }

    fun fetchDomesticPlace() {
        _domesticPlace.value = TravelDestinationDomestic()
    }

    fun fetchAbroadPlace() {
        _abroadPlace.value = TravelDestinationAbroad()
    }

    fun updateSelectedPlace(placeName: String, imageResId: Int) {
        val currentList = _selectedPlace.value.toMutableList()
        if (currentList.size >= 3) {
            currentList.removeAt(0)
        }
        currentList.add(Pair(placeName, imageResId))
        _selectedPlace.value = currentList
        formatTitleList(currentList, _tripPeriod.value)
    }

    private fun formatTitleList(
        placeList: List<Pair<String, Int>> = _selectedPlace.value,
        period: Int = _tripPeriod.value
    ) {
        if (placeList.isNotEmpty() && period > 0) {
            val formattedPlaces = placeList.joinToString("-") { it.first }
            _title.value = "$period 일 간 $formattedPlaces 여행"
        } else if (period > 0) {
            _title.value = "$period 일 간의 여행"
        } else {
            _title.value = ""
        }
    }

    fun deletePlace(removeItem: Pair<String, Int>) {
        val currentList = _selectedPlace.value.toMutableList()
        if (currentList.contains(removeItem)) {
            currentList.remove(removeItem)
            _selectedPlace.value = currentList
            formatTitleList(currentList, _tripPeriod.value)
        }
    }

    fun deletePlaceList() {
        _selectedPlace.value = emptyList()
        formatTitleList(emptyList(), _tripPeriod.value)
    }

    fun deleteUserSchedule(userSchduleEntity: UserSchduleEntity) {
        viewModelScope.launch {
            userScheduleDao.deleteSchedule(userSchduleEntity)
            _userScheduleList.value = userScheduleDao.getAllSchedules()
        }
    }

    fun deleteAllUserSchedules() {
        viewModelScope.launch {
            userScheduleDao.deleteAllSchedules()
            _userScheduleList.value = emptyList()
        }
    }
}
