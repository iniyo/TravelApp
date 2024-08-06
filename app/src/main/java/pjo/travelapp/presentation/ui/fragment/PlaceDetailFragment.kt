package pjo.travelapp.presentation.ui.fragment

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.R
import pjo.travelapp.data.entity.Photo
import pjo.travelapp.databinding.FragmentPlaceDetailBinding
import pjo.travelapp.presentation.adapter.ImageViewPagerAdapter
import pjo.travelapp.presentation.ui.consts.REVIEWS_MAX_LINE
import pjo.travelapp.presentation.ui.consts.TAKE_REVIEWS
import pjo.travelapp.presentation.ui.consts.ZERO_NUMBER
import pjo.travelapp.presentation.ui.viewmodel.DetailViewModel
import pjo.travelapp.presentation.util.extension.copyTextToClipboard
import pjo.travelapp.presentation.util.navigator.AppNavigator
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class PlaceDetailFragment : BaseFragment<FragmentPlaceDetailBinding>() {

    private val detailViewModel: DetailViewModel by activityViewModels()
    private lateinit var imageViewPagerAdapter: ImageViewPagerAdapter

    @Inject
    lateinit var navigator: AppNavigator

    override fun initCreate() {
        super.initCreate()
        Log.d("TAG", "생성: PlaceDetailFragment ")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
        initListener()
    }

    override fun initView() {
        binding.apply {
            vpImageSlider.offscreenPageLimit = 2
            imageViewPagerAdapter = ImageViewPagerAdapter()
            vpImageSlider.adapter = imageViewPagerAdapter
            diVpIndicator.attachTo(vpImageSlider)
        }
    }

    override fun initViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observePlaceDetails() }
                launch { observePlaceResult() }
            }
        }
    }

    private suspend fun observePlaceDetails() {
        detailViewModel.placeDetails.collectLatest { detail ->
            detail?.let {
                updateUIForPlaceDetail(
                    it.bitmap.orEmpty().filterNotNull(),
                    it.place.name,
                    it.place.placeTypes?.get(ZERO_NUMBER),
                    it.place.reviews?.take(TAKE_REVIEWS)
                        ?.map { review -> "${review.authorAttribution.name}: ${review.text}" },
                    it.place.rating,
                    it.place.openingHours?.weekdayText,
                    it.bitmap.orEmpty().size.toString() + getString(R.string.page)
                )
            }
        }
    }

    private suspend fun observePlaceResult() {
        detailViewModel.placeResult.collectLatest { result ->
            result?.let {
                updateUIForPlaceResult(
                    it.photos.orEmpty(),
                    it.name,
                    it.types?.get(ZERO_NUMBER),
                    it.reviews?.take(TAKE_REVIEWS)
                        ?.map { review -> "${review.authorName}: ${review.text}" },
                    it.rating,
                    it.openingHours?.weekdayText,
                    it.photos.orEmpty().size.toString() + getString(R.string.page),
                    it.openingHours?.openNow!!
                )
                Log.d("TAG", "observePlaceResult: ${result.openingHours?.openNow}")
            }
        }
    }

    private fun updateUIForPlaceDetail(
        bitmaps: List<Bitmap>,
        name: String?,
        type: String?,
        reviews: List<String>?,
        rating: Double?,
        weekDayText: List<String>?,
        imageSize: String?,
    ) {
        binding.apply {
            imageViewPagerAdapter.submitBitmaps(bitmaps)
            tvStoreTitle.text = name
            tvStoreType.text = type ?: getString(R.string.travel_destination)
            tvReviews.text = reviews?.joinToString("\n\n") ?: getString(R.string.not_founded_review)
            tvRatingScore.text = rating?.toString()
            rbScore.rating = rating?.toFloat() ?: ZERO_NUMBER.toFloat()
            tvOpeningHours.text =
                weekDayText?.joinToString("\n") ?: getString(R.string.not_founded_opening_hours)
            tvImagesSize.text = imageSize ?: (ZERO_NUMBER.toString() + getString(R.string.page))
        }
    }

    private fun updateUIForPlaceResult(
        photos: List<Photo>,
        name: String?,
        type: String?,
        reviews: List<String>?,
        rating: Double?,
        weekDayText: List<String>?,
        imageSize: String?,
        openStatus: Boolean
    ) {
        binding.apply {
            imageViewPagerAdapter.submitPhotos(photos)
            tvStoreTitle.text = name
            tvStoreType.text = type ?: getString(R.string.travel_destination)
            tvReviews.text = reviews?.joinToString("\n\n") ?: getString(R.string.not_founded_review)
            tvRatingScore.text = rating?.toString()
            rbScore.rating = rating?.toFloat() ?: ZERO_NUMBER.toFloat()
            tvOpeningHours.text = weekDayText?.joinToString(separator = ",\n\n") { it.replace(",", ",\n") }
                ?: getString(R.string.not_founded_opening_hours)
            tvImagesSize.text = imageSize ?: (ZERO_NUMBER.toString() + getString(R.string.page))
            Log.d("TAG", "updateUIForPlaceResult: $openStatus")
            if (openStatus) {
                tvOpenState.text = requireContext().getString(R.string.opening)
                val color = ContextCompat.getColor(requireContext(), R.color.selected_icon_color)
                tvOpenState.setTextColor(ColorStateList.valueOf(color))
            } else {
                tvOpenState.text = requireContext().getString(R.string.closed)
                val color = ContextCompat.getColor(requireContext(), R.color.dark_purple)
                tvOpenState.setTextColor(ColorStateList.valueOf(color))
            }
        }
    }

    override fun initListener() {
        binding.apply {
            // text copy
            tvStoreTitle.setOnLongClickListener {
                requireContext().copyTextToClipboard(tvStoreTitle.text.toString())
                true
            }
            cvPlaceInfo.setOnClickListener {
                if (tvOpenMore.visibility == View.VISIBLE) {
                    tvOpenMore.visibility = View.GONE
                    tvOpeningHours.visibility = View.VISIBLE
                } else {
                    tvOpenMore.visibility = View.VISIBLE
                    tvOpeningHours.visibility = View.GONE
                }
            }
            cvReviewsInfo.setOnClickListener {
                if (tvReviews.maxLines == REVIEWS_MAX_LINE) {
                    tvReviews.maxLines = Int.MAX_VALUE
                } else {
                    tvReviews.maxLines = REVIEWS_MAX_LINE
                }
            }
            toolbar.ivSignDisplayBackButton.setOnClickListener {
                navigator.navigateUp()
            }
        }
    }

    /*private fun formatOpeningHours(weekDayText: List<String>?): CharSequence {
        return weekDayText?.joinToString(separator = ",\n\n") { day ->
            val spannable = SpannableString(day)
            if (day.length >= 3) {
                spannable.setSpan(
                    ForegroundColorSpan(Color.RED), // 원하는 색상으로 변경
                    0, 3,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            spannable
        } ?: getString(R.string.not_founded_opening_hours)
    }*/
}
