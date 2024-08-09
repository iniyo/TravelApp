package pjo.travelapp.presentation.ui.fragment

import android.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.data.entity.PlaceDetail
import pjo.travelapp.data.entity.UserPlan

import pjo.travelapp.databinding.FragmentScehduleBinding
import pjo.travelapp.presentation.adapter.ScheduleDefaultAdapter
import pjo.travelapp.presentation.adapter.UserScheduleAdapter
import pjo.travelapp.presentation.ui.viewmodel.DetailViewModel
import pjo.travelapp.presentation.ui.viewmodel.MainViewModel
import pjo.travelapp.presentation.ui.viewmodel.PlanViewModel
import pjo.travelapp.presentation.util.LatestUiState
import pjo.travelapp.presentation.util.mapper.MyGraphicMapper
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import javax.inject.Inject

@AndroidEntryPoint
class ScheduleFragment : BaseFragment<FragmentScehduleBinding>() {

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
                    sflNextTrips.visibility = View.VISIBLE
                    sflAroundPlace.visibility = View.VISIBLE
                    rvNextTrips.visibility = View.GONE
                    rvAroundPlace.visibility = View.GONE
                }

                is LatestUiState.Success -> {
                    when (choose) {
                        true -> {
                            rvAroundPlace.visibility = View.VISIBLE
                            sflAroundPlace.visibility = View.GONE
                            defaultAdapter1?.submitList(state.data)
                        }

                        else -> {
                            rvNextTrips.visibility = View.VISIBLE
                            sflNextTrips.visibility = View.GONE
                            defaultAdapter2?.submitList(state.data)
                        }
                    }
                }

                is LatestUiState.Error -> {
                    // 에러 처리 로직 추가
                    when (choose) {
                        true -> {
                            rvAroundPlace.visibility = View.VISIBLE
                            sflAroundPlace.visibility = View.GONE
                        }

                        else -> {
                            rvNextTrips.visibility = View.VISIBLE
                            sflNextTrips.visibility = View.GONE
                        }
                    }
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
            scheduleAdapter = UserScheduleAdapter(
                itemClickList = {
                    navigator.navigateTo(Fragments.PLAN_PAGE)
                    planViewModel.fetchUserSchedule(it)
                },
                deleteClickList = {
                    showDeleteConfirmationDialog(it)
                }
            )
            val scheduleAdapter = ScheduleDefaultAdapter {
               /* detailViewModel.fetchPlaceDetails(it)*/
                navigator.navigateTo(Fragments.PLACE_DETAIL_PAGE_ITEM)
            }
            defaultAdapter1 = scheduleAdapter
            defaultAdapter2 = scheduleAdapter

            vpTrips.apply {
                val itemMargin = (MyGraphicMapper.getScreenWidth(requireContext()) * 0.02)
                val previewWidth = (MyGraphicMapper.getScreenWidth(requireContext()) * 0.02)
                val (pageTransX, decoration) = MyGraphicMapper.getDecoration(
                    itemMargin.toInt(),
                    previewWidth.toInt()
                )

                addItemDecoration(decoration)
                setPageTransformer { page, position ->
                    page.translationX = position * -pageTransX
                }
                offscreenPageLimit = 1
            }
        }
    }

    private fun showDeleteConfirmationDialog(userSchedule: UserPlan) {
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