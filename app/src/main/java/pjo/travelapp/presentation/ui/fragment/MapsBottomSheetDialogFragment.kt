package pjo.travelapp.presentation.ui.fragment

import android.util.Log
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import pjo.travelapp.R
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.databinding.FragmentMapsBottomSheetBinding
import pjo.travelapp.presentation.ui.viewmodel.MapsViewModel
import pjo.travelapp.presentation.util.LatestUiState
import kotlin.math.log

class MapsBottomSheetDialogFragment :
    BaseFragment<FragmentMapsBottomSheetBinding>(R.layout.fragment_maps_bottom_sheet) {

    private val viewModel: MapsViewModel by viewModels()

    override fun initView() {
        super.initView()
        bind {
            setDialog()
        }
    }

    private fun setDialog() {
        bind {
            clTabContainer1.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
            }
        }
    }

    override fun initViewModel() {
        bind {
            launchWhenStarted {
                viewModel.placeDetails.collectLatest { res ->
                    when (res) {
                        is LatestUiState.Error -> {
                            Log.d("TAG", "initViewModel: Error")
                        }
                        LatestUiState.Loading -> {
                            Log.d("TAG", "initViewModel: Loading")
                        }
                        is LatestUiState.Success -> {
                            res.data?.result?.let {
                                Log.d("TAG", "initViewModel: Success")
                                place = it
                            }
                        }
                    }
                }
            }
        }
    }

}