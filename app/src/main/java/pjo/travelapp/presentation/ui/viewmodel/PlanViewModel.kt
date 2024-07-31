package pjo.travelapp.presentation.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pjo.travelapp.data.datasource.UserScheduleDao
import pjo.travelapp.data.entity.PlaceDetail
import pjo.travelapp.data.entity.TravelDestinationAbroad
import pjo.travelapp.data.entity.TravelDestinationDomestic
import pjo.travelapp.data.entity.UserSchduleEntity
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val userScheduleDao: UserScheduleDao
) : ViewModel() {

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

    private val _selectedPlace = MutableStateFlow<List<Pair<String, Bitmap>>>(emptyList())
    val selectedPlace: StateFlow<List<Pair<String, Bitmap>>> get() = _selectedPlace

    private val _placeDetailLiist = MutableStateFlow<List<PlaceDetail>?>(null)
    val placeDetailLiist: StateFlow<List<PlaceDetail>?> get() = _placeDetailLiist

    init {
        fetchDomesticPlace()
        fetchAbroadPlace()
    }

    /**
     * public function
     */
    fun fetchUserSchedule(userEntity: UserSchduleEntity) {
        viewModelScope.launch {
            userScheduleDao.insertSchedule(userEntity)

            fetchTripPeriod(userEntity.period)
            fetchSelectedCalendarDatePeriod(userEntity.datePeriod)
            fetchTitle(userEntity.title)
            fetchUserAdapter(userEntity.planListDate)
        }
    }

    fun clearCurrentUserEntity() {
        deletePlaceList()
        fetchSelectedCalendarDatePeriod(null)
        fetchTitle(null)
        fetchUserAdapter(null)
    }

    fun fetchTripPeriod(date: Int) {
        Log.d("TAG", "fetchTripPeriod: $date ")
        _tripPeriod.value = date
        formatTitleList()
    }

    fun fetchSelectedCalendarDatePeriod(date: CharSequence?) {
        _selectedCalendarDatePeriod.value = date.toString()
    }

    fun fetchTitle(title: String?) {
        if (title != null) {
            _title.value = title
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

    fun fetchSelectedPlace(place: PlaceDetail) {
        val currnetList = _placeDetailLiist.value?.toMutableList()
        currnetList?.add(place)
        _placeDetailLiist.value = currnetList
    }

    fun updateSelectedPlace(placeName: String, bitmap: Bitmap) {
        Log.d("TAG", "updateSelectedPlace: $placeName, $bitmap")
        val currentList = _selectedPlace.value.toMutableList()
        if (currentList.size > 3) {
            currentList.removeAt(1)
        }
        currentList.add(Pair(placeName, bitmap))
        _selectedPlace.value = currentList
        formatTitleList(currentList, _tripPeriod.value)
    }

    fun deletePlace(removeItem: Pair<String, Bitmap>) {
        val currentList = _selectedPlace.value.toMutableList()
        if (currentList.contains(removeItem)) {
            currentList.remove(removeItem)
            _selectedPlace.value = currentList
            formatTitleList(currentList, _tripPeriod.value)
        }
    }

    fun deletePlaceList() {
        val currentList = _selectedPlace.value.toMutableList()
        currentList.clear()
        _selectedPlace.value = currentList
        _tripPeriod.value = 0
        formatTitleList()
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

    /**
     * public function end
     */

    private fun formatTitleList(
        placeList: List<Pair<String, Bitmap>> = _selectedPlace.value,
        period: Int = _tripPeriod.value
    ) {
        Log.d("TAG", "formatTitleList: $placeList, $period")
        if (placeList.isNotEmpty()) {
            val formattedPlaces = placeList.joinToString("-") { it.first }
            _title.value = "$period 일 간 $formattedPlaces 여행"
        } else if (period > 0) {
            _title.value = "$period 일 간의 여행"
        } else {
            _title.value = ""
        }
    }
}
