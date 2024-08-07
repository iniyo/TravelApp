package pjo.travelapp.presentation.ui.fragment

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.data.entity.PlaceDetail
import pjo.travelapp.data.entity.UserSchduleEntity
import pjo.travelapp.databinding.FragmentScehduleBinding
import pjo.travelapp.presentation.adapter.ScheduleDefaultAdapter
import pjo.travelapp.presentation.adapter.UserScehduleAdapter
import pjo.travelapp.presentation.ui.viewmodel.DetailViewModel
import pjo.travelapp.presentation.ui.viewmodel.MainViewModel
import pjo.travelapp.presentation.ui.viewmodel.PlanViewModel
import pjo.travelapp.presentation.util.LatestUiState
import pjo.travelapp.presentation.util.mapper.MyGraphicMapper
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import javax.inject.Inject

@AndroidEntryPoint
class ScehduleFragment : BaseFragment<FragmentScehduleBinding>() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val planViewModel: PlanViewModel by activityViewModels()
    private val detailViewModel: DetailViewModel by activityViewModels()

    @Inject
    lateinit var navigator: AppNavigator

    override fun initCreate() {
        planViewModel.clearCurrentUserEntity()
        planViewModel.fetchUserSchedules()
    }

    override fun initViewModel() {

        bind {
            launchWhenStarted {
                launch {
                    mainViewModel.nearbySearch.collectLatest {
                        handleUiState(it, choose = true)
                    }
                }
                launch {
                    mainViewModel.shuffledHotPlaceList.collectLatest {
                        handleUiState(it, choose = false)
                    }
                }
                launch {
                    planViewModel.userScheduleList.collectLatest {
                        if (it.isEmpty()) {
                            vpTrips.visibility = View.GONE
                            tvNoSchedule.visibility = View.VISIBLE
                        } else {
                            vpTrips.visibility = View.VISIBLE
                            tvNoSchedule.visibility = View.GONE
                            scheduleAdapter?.submitList(it)
                        }
                    }
                }
            }
        }
    }

    private fun handleUiState(state: LatestUiState<List<PlaceDetail>>? = null, choose: Boolean) {
        bind {
            when (state) {
                is LatestUiState.Loading -> {

                }

                is LatestUiState.Success -> {
                    when (choose) {
                        true -> {
                            state.data.forEach {
                                defaultAdapter1?.addPlace(it)
                            }
                        }

                        else -> {
                            state.data.forEach {
                                defaultAdapter2?.addPlace(it)
                            }
                        }
                    }
                }

                is LatestUiState.Error -> {
                    // 에러 처리 로직 추가
                    Toast.makeText(context, "Error: ${state.exception.message}", Toast.LENGTH_SHORT)
                        .show()
                }

                null -> {}
            }
        }
    }

    override fun initAdapter() {
        super.initAdapter()
        bind {
            scheduleAdapter = UserScehduleAdapter(
                itemClickList = {
                    navigator.navigateTo(Fragments.PLAN_PAGE)
                    planViewModel.fetchUserSchedule(it)
                },
                deleteClickList = {
                    showDeleteConfirmationDialog(it)
                }
            )
            val scheduleAdapter = ScheduleDefaultAdapter {
                detailViewModel.fetchPlaceDetails(it)
                navigator.navigateTo(Fragments.PLACE_DETAIL_PAGE_ITEM)
            }
            defaultAdapter1 = scheduleAdapter
            defaultAdapter2 = scheduleAdapter


            vpTrips.apply {
                val (pageTransX, decoration) = MyGraphicMapper.getDecoration()

                addItemDecoration(decoration)
                setPageTransformer { page, position ->
                    page.translationX = position * -pageTransX
                }
                offscreenPageLimit = 1
            }
        }
    }

    private fun showDeleteConfirmationDialog(userSchedule: UserSchduleEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("삭제 확인")
            .setMessage("정말로 이 항목을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                planViewModel.deleteUserSchedule(userSchedule)
            }
            .setNegativeButton("취소", null)
            .show()
    }
}