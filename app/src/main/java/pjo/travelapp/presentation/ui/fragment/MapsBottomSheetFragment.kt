package pjo.travelapp.presentation.ui.fragment

import android.util.Log
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.databinding.FragmentMapsBottomSheetBinding
import pjo.travelapp.presentation.adapter.ImageViewPagerAdapter
import pjo.travelapp.presentation.ui.viewmodel.MapsViewModel

@AndroidEntryPoint
class MapsBottomSheetFragment : BaseFragment<FragmentMapsBottomSheetBinding>() {

    private lateinit var imageViewPagerAdapter: ImageViewPagerAdapter
    private val mapsViewModel: MapsViewModel by activityViewModels()

    override fun initCreate() {
        Log.d("TAG", "initCreate maps bottomsheet: $")
    }

    override fun initView() {
        super.initView()
        bind {
            Log.d("TAG", "initView maps bottomsheet: $")

        }
    }

    override fun initViewModel() {
        bind {
            launchWhenStarted {
                launch{
                    mapsViewModel.placeDetailsResult.collectLatest {
                        it?.photos?.let { nonNullPhotos ->
                            imageViewPagerAdapter.submitPhotos(nonNullPhotos)
                        }
                    }
                }
            }
        }
    }
}