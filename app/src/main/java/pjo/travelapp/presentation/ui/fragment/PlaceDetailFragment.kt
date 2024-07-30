package pjo.travelapp.presentation.ui.fragment

import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import pjo.travelapp.databinding.FragmentPlaceDetailBinding
import pjo.travelapp.presentation.ui.viewmodel.DetailViewModel

@AndroidEntryPoint
class PlaceDetailFragment : BaseFragment<FragmentPlaceDetailBinding>() {
    private val detailViewModel: DetailViewModel by activityViewModels()

    override fun initView() {
        bind {
            vpImageSlider.offscreenPageLimit = 1
            diVpIndicator.attachTo(vpImageSlider)

        }
    }

    override fun initViewModel() {
        bind {
            launchWhenStarted {
                detailViewModel.placeDetails.collectLatest {
                    it?.let { detail ->
                        tvStoreTitle.text = it.place.name
                        val reviewsText = StringBuilder()
                        it.place.reviews?.forEach { review ->
                            reviewsText.append(review.text).append("\n")
                        }
                        tvReviews.text = reviewsText.toString().trim()
                        tvRatingScore.text = it.place.rating?.toString() ?: "0"
                        rbScore.rating = it.place.rating?.toFloat() ?: 0f
                        val nonNullBitmaps = detail.bitmap.orEmpty().filterNotNull()
                        adapter?.setBitmaps(nonNullBitmaps)
                    }
                }
            }
        }
    }
}