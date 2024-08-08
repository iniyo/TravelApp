package pjo.travelapp.presentation.ui.fragment

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

    override fun initView() {
        super.initView()
        bind {
            vpImageSlider.offscreenPageLimit = 2
            imageViewPagerAdapter = ImageViewPagerAdapter()
            vpImageSlider.adapter = imageViewPagerAdapter
            diVpIndicator.attachTo(vpImageSlider)
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