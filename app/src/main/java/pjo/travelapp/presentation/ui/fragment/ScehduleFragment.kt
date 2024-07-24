package pjo.travelapp.presentation.ui.fragment

import android.graphics.Bitmap
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.libraries.places.api.model.Place
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentScehduleBinding
import pjo.travelapp.presentation.adapter.TopSlideViewPagerAdapter
import pjo.travelapp.presentation.ui.viewmodel.MainViewModel
import pjo.travelapp.presentation.util.LatestUiState

@AndroidEntryPoint
class ScehduleFragment : BaseFragment<FragmentScehduleBinding>() {

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun initView() {
        super.initView()
        bind {

        }
    }

    override fun initViewModel() {
        super.initViewModel()

        bind {
            launchWhenStarted {
                mainViewModel.nearbySearch.collectLatest {
                    handleUiState(it, 1)
                }

                mainViewModel.shuffledHotPlaceList.collectLatest {
                    handleUiState(it, 2)
                }
            }
        }
    }

    private fun handleUiState(state: LatestUiState<List<Pair<Place, Bitmap?>>>, choose: Int) {
        bind {
            val a = listOf(
                R.drawable.banner1,
                R.drawable.banner2
            )
            when (state) {
                is LatestUiState.Loading -> {

                }

                is LatestUiState.Success -> {
                    state.data.forEach {
                        when (choose) {
                            0 -> scheduleAdapter = TopSlideViewPagerAdapter(a)
                            1 -> defaultAdapter1?.addPlace(it)
                            2 -> defaultAdapter2?.addPlace(it)
                        }
                    }
                }

                is LatestUiState.Error -> {
                    // 에러 처리 로직 추가
                    Toast.makeText(context, "Error: ${state.exception.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}