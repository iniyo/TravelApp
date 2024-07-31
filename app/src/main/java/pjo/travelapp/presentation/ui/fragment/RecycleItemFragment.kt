package pjo.travelapp.presentation.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.data.entity.HotelCard
import pjo.travelapp.data.entity.PlaceDetail
import pjo.travelapp.databinding.FragmentRecycleItemBinding
import pjo.travelapp.presentation.adapter.MorePlaceRecyclerAdapter
import pjo.travelapp.presentation.ui.viewmodel.DetailViewModel
import pjo.travelapp.presentation.ui.viewmodel.MainViewModel
import pjo.travelapp.presentation.util.GridSpacingItemDecoration
import pjo.travelapp.presentation.util.LatestUiState
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import javax.inject.Inject

@AndroidEntryPoint
class RecycleItemFragment : BaseFragment<FragmentRecycleItemBinding>() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val detailViewModel: DetailViewModel by activityViewModels()
    @Inject
    lateinit var navigator: AppNavigator
    private lateinit var pop: String

    override fun initView() {
        super.initView()
        bind {
            if (rvMorePlaces.adapter == null) {
                rvMorePlaces.layoutManager =
                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                rvMorePlaces.addItemDecoration(
                    GridSpacingItemDecoration(
                        2,
                        16,
                        true
                    )
                ) // 16dp의 마진 적용
                adapter = MorePlaceRecyclerAdapter(
                    placeItemClickListener = {
                        detailViewModel.fetchPlaceDetails(it)
                        navigator.navigateTo(Fragments.PLACE_DETAIL_PAGE)
                    },
                    hotelItemClickListener = {
                        detailViewModel.fetchHotelDetail(it)
                        navigator.navigateTo(Fragments.PLACE_DETAIL_PAGE)
                    }
                )
            }
        }
    }

    override fun initViewModel() {
        super.initViewModel()
        bind {
            launchWhenStarted {
                when (pop) {
                    "도쿄" -> {
                        title = pop
                        launch {
                            mainViewModel.tokyoHotPlaceList.collectLatest {
                                handleUiState(it)
                            }
                        }
                    }

                    "후쿠오카" -> {
                        title = pop
                        launch {
                            mainViewModel.fukuokaHotPlaceList.collectLatest {
                                handleUiState(it)
                            }
                        }
                    }

                    "파리" -> {
                        title = pop
                        launch {
                            mainViewModel.parisHotPlaceList.collectLatest {
                                handleUiState(it)
                            }
                        }
                    }

                    "근처" -> {
                        title = pop
                        launch {
                            mainViewModel.nearbySearch.collectLatest {
                                handleUiState(it)
                            }
                        }
                    }

                    "숙소" -> {
                        title = pop
                        launch {
                            mainViewModel.hotelState.collectLatest { state ->
                                handleUiState(hotelState = state)
                            }
                        }
                    }

                    else -> {
                        launch {
                            title = pop
                        }
                    }
                }
            }
        }
    }

    private fun handleUiState(
        placeState: LatestUiState<List<PlaceDetail>>? = null,
        hotelState: LatestUiState<List<HotelCard>>? = null
    ) {
        bind {
            when (placeState) {
                is LatestUiState.Loading -> {
                    pbPopularMore.visibility = View.VISIBLE
                }

                is LatestUiState.Success -> {
                    pbPopularMore.visibility = View.GONE
                    adapter?.updatePlaces(placeState.data)
                }

                is LatestUiState.Error -> {
                    pbPopularMore.visibility = View.GONE
                    Toast.makeText(
                        context,
                        "Error: ${placeState.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {}
            }

            when (hotelState) {
                is LatestUiState.Loading -> {
                    pbPopularMore.visibility = View.VISIBLE
                }

                is LatestUiState.Success -> {
                    pbPopularMore.visibility = View.GONE
                    hotelState.data.forEach {
                        Log.d("TAG", "hotel handle: $it")
                        adapter?.updateHotels(hotelState.data)
                    }
                }

                is LatestUiState.Error -> {
                    pbPopularMore.visibility = View.GONE
                    Log.d("TAG", "handleUiState: ${hotelState.exception.message}")
                    Toast.makeText(
                        context,
                        "Error: ${hotelState.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {}
            }
        }
    }

    companion object {
        private const val ARG_POP = "pop"

        fun newInstance(pop: String): RecycleItemFragment {
            val fragment = RecycleItemFragment()
            val args = Bundle()
            args.putString(ARG_POP, pop)
            fragment.arguments = args
            return fragment
        }
    }

    override fun initCreate() {
        super.initCreate()
        arguments?.let {
            pop = it.getString(ARG_POP) ?: ""
        }
    }
}
