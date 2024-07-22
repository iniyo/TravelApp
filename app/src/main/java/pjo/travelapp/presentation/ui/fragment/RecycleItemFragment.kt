package pjo.travelapp.presentation.ui.fragment

import android.graphics.Bitmap
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentRecycleItemBinding
import pjo.travelapp.presentation.adapter.MorePlaceRecyclerAdapter
import pjo.travelapp.presentation.ui.viewmodel.BaseViewModel
import pjo.travelapp.presentation.util.GridSpacingItemDecoration
import pjo.travelapp.presentation.util.LatestUiState

class RecycleItemFragment(
    private val pop: String
) : BaseFragment<FragmentRecycleItemBinding>(R.layout.fragment_recycle_item) {

    private val viewModel: BaseViewModel by activityViewModels()

    override fun initView() {
        super.initView()
        bind {

            rvMorePlaces.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            rvMorePlaces.addItemDecoration(GridSpacingItemDecoration(2, 16, true)) // 16dp의 마진 적용
            adapter = MorePlaceRecyclerAdapter()
        }
    }
    private fun handleUiState(state: LatestUiState<List<Pair<Place, Bitmap?>>>) {
        bind {
            when (state) {
                is LatestUiState.Loading -> {
                    pbPopularMore.visibility = View.VISIBLE
                }
                is LatestUiState.Success -> {
                    pbPopularMore.visibility = View.GONE
                    state.data.forEach {
                        adapter?.addPlace(it)
                    }
                }
                is LatestUiState.Error -> {
                    pbPopularMore.visibility = View.GONE
                    // 에러 처리 로직 추가
                    Toast.makeText(context, "Error: ${state.exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
    override fun initViewModel() {
        super.initViewModel()
        bind {

            launchWhenStarted {
                when (pop) {
                    "Tokyo" -> {
                        launch {
                            viewModel.tokyoHotPlaceList.collectLatest { state ->
                                handleUiState(state)
                                tvCountryTitle.text = pop
                            }
                        }
                    }
                    "Fukuoka" -> {
                        launch {
                            viewModel.fukuokaHotPlaceList.collectLatest { state ->
                                handleUiState(state)
                                tvCountryTitle.text = pop
                            }
                        }
                    }
                    "Paris" -> {
                        launch {
                            viewModel.parisHotPlaceList.collectLatest { state ->
                                handleUiState(state)
                                tvCountryTitle.text = pop
                            }
                        }
                    }
                }
            }
        }
    }
}