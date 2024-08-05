package pjo.travelapp.presentation.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xwray.groupie.ExpandableGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pjo.travelapp.data.entity.ParentGroupData
import pjo.travelapp.data.entity.ParentGroups
import pjo.travelapp.data.entity.TravelDestinationAbroad
import pjo.travelapp.data.entity.TravelDestinationDomestic
import pjo.travelapp.data.entity.UserNote
import pjo.travelapp.data.entity.UserPlan
import pjo.travelapp.data.repo.UserRepository
import pjo.travelapp.presentation.util.BitmapUtil
import pjo.travelapp.presentation.util.makeItemId
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val userRepo: UserRepository
) : ViewModel() {

    private var _tripPeriod = MutableStateFlow(0)

    private var _selectedCalendarDatePeriod = MutableStateFlow("")
    val selectedCalendarDatePeriod: StateFlow<String> get() = _selectedCalendarDatePeriod

    private val _domesticPlace = MutableStateFlow<TravelDestinationDomestic?>(null)
    private val _abroadPlace = MutableStateFlow<TravelDestinationAbroad?>(null)
    val abroadPlace: StateFlow<TravelDestinationAbroad?> get() = _abroadPlace

    private var _title = MutableStateFlow(String())
    val title: StateFlow<String> get() = _title

    private val _planAdapterList = MutableStateFlow<ParentGroups?>(null)
    val planAdapterList: StateFlow<ParentGroups?> get() = _planAdapterList

    private val _userScheduleList = MutableStateFlow<List<UserPlan>>(emptyList())
    val userScheduleList: StateFlow<List<UserPlan>> get() = _userScheduleList

    private val _selectedPlace = MutableStateFlow<List<Pair<String, Bitmap>>>(emptyList())
    val selectedPlace: StateFlow<List<Pair<String, Bitmap>>> get() = _selectedPlace

    /*private val _placeDetailList = MutableStateFlow<List<PlaceDetail>?>(null)*/

    private val _parentGroups = MutableStateFlow<List<ExpandableGroup>>(emptyList())
    val parentGroups: StateFlow<List<ExpandableGroup>> = _parentGroups

    private val _userPlan = MutableStateFlow<UserPlan?>(null)
    val userPlan: StateFlow<UserPlan?> get() = _userPlan

    /*private val _toggleBottomSheetEvent = MutableSharedFlow<Int>()
    val toggleBottomSheetEvent = _toggleBottomSheetEvent.asSharedFlow()

    private val _showDialogEvent = MutableSharedFlow<Unit>()
    val showDialogEvent = _showDialogEvent.asSharedFlow()*/

    private var _placeAndPhotoList = MutableStateFlow<List<Pair<String, String>>?>(null)
    val placeAndPhotoList: StateFlow<List<Pair<String, String>>?> get() = _placeAndPhotoList

    private val _tempUserNotes = MutableStateFlow<Map<Int, UserNote>>(emptyMap())
    val tempUserNotes: StateFlow<Map<Int, UserNote>> get() = _tempUserNotes

    fun updateTempUserNote(note: UserNote) {
        _tempUserNotes.value =
            _tempUserNotes.value.toMutableMap().apply { put(note.position, note) }
    }

    fun getTempUserNote(position: Int): UserNote? {
        return _tempUserNotes.value[position]
    }

    fun clearTempUserNote() {
        _tempUserNotes.value = emptyMap()
    }

    init {
        fetchDomesticPlace()
        fetchAbroadPlace()
        /*fetchUserPlanList()*/
    }

    /**
     * public function
     */

    fun saveBitmaps(context: Context) {
        viewModelScope.launch {
            val bitmapUtil = BitmapUtil(context)
            val placeAndPaths = withContext(Dispatchers.IO) {
                selectedPlace.value.map { (place, bitmap) ->
                    val path = bitmapUtil.saveBitmap(
                        bitmap,
                        "${System.currentTimeMillis()}_${place.hashCode()}.png"
                    )
                    place to path
                }
            }
            _placeAndPhotoList.value = placeAndPaths
        }
    }

    fun fetchParentGroups(parentGroups: List<ExpandableGroup>) {
        Log.d("TAG", "fetchParentGroups: ")
        _parentGroups.value = parentGroups
    }

    fun fetchUserSchedule(userPlan: UserPlan) {
        viewModelScope.launch {
            userRepo.insertUserPlan(userPlan)
            _userPlan.value = userPlan
            fetchTripPeriod(userPlan.period)
            fetchSelectedCalendarDatePeriod(userPlan.datePeriod)
            fetchTitle(userPlan.title)
        }
    }

    fun fetchUserPlan(userPlan: UserPlan) {
        _userPlan.value = userPlan
    }


    fun fetchUserPlan() {
        _userPlan.value = UserPlan(
            id = makeItemId(),
            title = title.value,
            period = _tripPeriod.value,
            datePeriod = _selectedCalendarDatePeriod.value,
            placeAndPhotoPaths = placeAndPhotoList.value!!,
            parentGroups = planAdapterList.value!!
        )
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

    fun fetchUserSchedules() {
        viewModelScope.launch {
            _userScheduleList.value = userRepo.getAllUserPlans()
        }
    }

    fun fetchUserAdapter(selectedMonthsAndDays: List<Pair<Int, Int>>?) {
        if (!selectedMonthsAndDays.isNullOrEmpty()) {
            val parentGroups = ParentGroups(
                parentGroupDataList = selectedMonthsAndDays.map {
                    ParentGroupData(
                        parentItem = it,
                        userNote = null,
                        childItems = null
                    )
                }
            )

            _planAdapterList.value = parentGroups
        }
    }

    fun fetchAbroadPlace() {
        _abroadPlace.value = TravelDestinationAbroad()
    }

    /*fun fetchSelectedPlace(place: PlaceDetail) {
        val currnetList = _placeDetailList.value?.toMutableList()
        currnetList?.add(place)
        _placeDetailList.value = currnetList
    }*/

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

    fun deleteUserSchedule(userPlan: UserPlan) {
        viewModelScope.launch {
            userRepo.deleteUserPlan(userPlan)
            _userScheduleList.value = userRepo.getAllUserPlans()
        }
    }

    /*fun deleteAllUserSchedules() {
        viewModelScope.launch {
            userScheduleDao.deleteAllSchedules()
            _userScheduleList.value = emptyList()
        }
    }*/

    /**
     * public function end
     */

    /**
     * private fun
     */
    private fun fetchTitle(title: String?) {
        if (title != null) {
            _title.value = title
        }
    }

    private fun fetchDomesticPlace() {
        _domesticPlace.value = TravelDestinationDomestic()
    }

    // 중복된 아이템 여부를 확인하는 함수
    private suspend fun isItemAlreadyAdded(itemId: String): Boolean {
        return userRepo.getUserPlanById(itemId) != null
    }

    suspend fun saveOrUpdateUserPlan(userPlan: UserPlan): Boolean {
        return if (isItemAlreadyAdded(userPlan.id)) {
            userRepo.updateUserPlan(userPlan)
            true // 업데이트 됨
        } else {
            userRepo.insertUserPlan(userPlan)
            false // 새로 추가됨
        }
    }

    /*private fun onPlaceClick(position: Int) {
        viewModelScope.launch {
            _toggleBottomSheetEvent.emit(position)
        }
    }

    private fun onDialogClick() {
        viewModelScope.launch {
            _showDialogEvent.emit(Unit)
        }
    }*/

    /*private fun fetchUserPlanList() {
        viewModelScope.launch {
            planAdapterList.collect { planListDate ->
                // 빈 리스트가 아닌지 확인
                if (planListDate.isNotEmpty()) {
                    // parentGroups 초기화
                    val newParentGroups = mutableListOf<ExpandableGroup>()

                    // 각 날짜에 대해 ParentPlanItem 생성 및 ExpandableGroup 추가
                    planListDate.mapIndexed { index, date ->
                        val parentItem = ParentPlanItem(
                            item = date,
                            parentIndex = index,
                            noteClickListener = { onDialogClick() },
                            placeClickListener = { position ->
                                Log.d("TAG", "dddd: $position")
                                onPlaceClick(position)
                            }
                        )
                        val expandableGroup = ExpandableGroup(parentItem)
                        newParentGroups.add(expandableGroup)
                    }
                    // update
                    _parentGroups.value = newParentGroups
                }
            }
        }
    }*/

    private fun deletePlaceList() {
        val currentList = _selectedPlace.value.toMutableList()
        currentList.clear()
        _selectedPlace.value = currentList
        _tripPeriod.value = 0
        formatTitleList()
    }

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
    /**
     * private fun end
     */

}
