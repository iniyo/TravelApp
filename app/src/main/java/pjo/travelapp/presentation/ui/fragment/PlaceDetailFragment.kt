package pjo.travelapp.presentation.ui.fragment

import android.util.Log
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentPlaceDetailBinding
import pjo.travelapp.presentation.adapter.ImageViewPagerAdapter
import pjo.travelapp.presentation.ui.consts.TAKE_REVIEWS
import pjo.travelapp.presentation.ui.viewmodel.DetailViewModel
import pjo.travelapp.presentation.util.extension.copyTextToClipboard
import pjo.travelapp.presentation.util.navigator.AppNavigator
import javax.inject.Inject

@AndroidEntryPoint
class PlaceDetailFragment : BaseFragment<FragmentPlaceDetailBinding>() {
    private val detailViewModel: DetailViewModel by activityViewModels()
    private lateinit var imageViewPagerAdapter: ImageViewPagerAdapter

    @Inject
    lateinit var navigator: AppNavigator

    override fun initView() {
        bind {
            vpImageSlider.offscreenPageLimit = 1
            imageViewPagerAdapter = ImageViewPagerAdapter()
            vpImageSlider.adapter = imageViewPagerAdapter
            diVpIndicator.attachTo(vpImageSlider)
        }
    }

    override fun initViewModel() {
        bind {
            launchWhenStarted {
                detailViewModel.placeDetails.collectLatest {
                    val stringBuilder = StringBuilder()
                    it?.let { detail ->
                        placeDetail = detail
                        stringBuilder.append(detail.bitmap?.size.toString())
                        stringBuilder.append(getString(R.string.page))
                        tvImagesSize.text = stringBuilder
                        stringBuilder.clear()
                        val nonNullBitmaps = detail.bitmap.orEmpty().filterNotNull()
                        imageViewPagerAdapter.setBitmaps(nonNullBitmaps)
                        tvStoreTitle.text = it.place.name
                        tvStoreType.text =
                            it.place.placeTypes?.get(0) ?: getString(R.string.travel_destination)
                        it.place.reviews?.take(TAKE_REVIEWS)?.forEach { review ->
                            stringBuilder.append("${review.authorAttribution.name}: ${review.text}\n\n")
                        }
                        tvReviews.text = stringBuilder.toString().trim()
                        stringBuilder.clear()
                        tvRatingScore.text = it.place.rating?.toString()
                        rbScore.rating = it.place.rating?.toFloat()!!
                        val weekDayText = it.place.currentOpeningHours?.weekdayText
                        if(!weekDayText.isNullOrEmpty()) {
                            Log.d("TAG", "initViewModel: ${weekDayText}")
                            stringBuilder.append(weekDayText.joinToString("\n"))
                        }
                        tvOpeningHours.text = stringBuilder
                    }
                }
            }
        }
    }

    override fun initListener() {
        bind {
            toolbar.ivSignDisplayBackButton.setOnClickListener {
                navigator.navigateUp()
            }

            // text copy
            tvStoreTitle.setOnLongClickListener {
                requireContext().copyTextToClipboard(tvStoreTitle.text.toString())
                true
            }
        }
    }
}