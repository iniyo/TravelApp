package pjo.travelapp.presentation.ui.fragment

import android.graphics.Bitmap
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.libraries.places.api.model.Place
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.databinding.FragmentScehduleBinding
import pjo.travelapp.presentation.adapter.PromotionSlideAdapter
import pjo.travelapp.presentation.adapter.ScheduleDefaultAdapter
import pjo.travelapp.presentation.ui.viewmodel.MainViewModel
import pjo.travelapp.presentation.util.LatestUiState
import pjo.travelapp.presentation.util.mapper.MyGraphicMapper

@AndroidEntryPoint
class ScehduleFragment : BaseFragment<FragmentScehduleBinding>() {

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun initViewModel() {
        super.initViewModel()
        launchWhenStarted {
             launch{
                 mainViewModel.nearbySearch.collectLatest {
                     handleUiState(it, choose = 1)
                 }
             }
            launch {
                mainViewModel.shuffledHotPlaceList.collectLatest {
                    handleUiState(it, choose = 2)
                }
            }
            launch {
                mainViewModel.promotionData.collectLatest {
                    handleUiState(list = it, choose = 0)
                }
            }
        }
    }

    private fun handleUiState(state: LatestUiState<List<Pair<Place, Bitmap?>>>? = null, list: LatestUiState<List<Int>>? = null, choose: Int) {
        bind {
            when (state) {
                is LatestUiState.Loading -> {

                }
                is LatestUiState.Success -> {
                    when (choose) {
                        1 -> {
                            state.data.forEach {
                                defaultAdapter1?.addPlace(it)
                            }
                        }
                        2 -> {
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
            when (list) {
                is LatestUiState.Loading -> {
                }
                is LatestUiState.Success -> {
                    list.data.forEach {
                        scheduleAdapter?.addAd(it)
                    }
                }
                is LatestUiState.Error -> {
                }
                null -> {}
            }
        }
    }

    override fun initAdapter() {
        super.initAdapter()
        bind {
            scheduleAdapter = PromotionSlideAdapter()
            defaultAdapter1 = ScheduleDefaultAdapter()
            defaultAdapter2 = ScheduleDefaultAdapter()

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

}