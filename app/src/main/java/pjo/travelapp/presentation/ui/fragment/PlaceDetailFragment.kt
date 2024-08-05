package pjo.travelapp.presentation.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentPlaceDetailBinding
import pjo.travelapp.presentation.adapter.ImageViewPagerAdapter
import pjo.travelapp.presentation.ui.consts.TAKE_REVIEWS
import pjo.travelapp.presentation.ui.viewmodel.DetailViewModel
import pjo.travelapp.presentation.util.extension.copyTextToClipboard

@AndroidEntryPoint
class PlaceDetailFragment : Fragment() {

    private var _binding: FragmentPlaceDetailBinding? = null
    private val binding get() = _binding!!
    private val detailViewModel: DetailViewModel by activityViewModels()
    private lateinit var imageViewPagerAdapter: ImageViewPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceDetailBinding.inflate(inflater, container, false)
        Log.d("TAG", "onCreateView: detail ")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
        initListener()
    }

    private fun initView() {
        binding.apply {
            vpImageSlider.offscreenPageLimit = 1
            imageViewPagerAdapter = ImageViewPagerAdapter()
            vpImageSlider.adapter = imageViewPagerAdapter
            diVpIndicator.attachTo(vpImageSlider)
        }
    }

    private fun initViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    detailViewModel.placeDetails.collectLatest { detail ->
                        val stringBuilder = StringBuilder()
                        detail?.let {
                            stringBuilder.append(it.bitmap?.size.toString())
                            stringBuilder.append(getString(R.string.page))
                            binding.tvImagesSize.text = stringBuilder
                            stringBuilder.clear()
                            val nonNullBitmaps = it.bitmap.orEmpty().filterNotNull()
                            imageViewPagerAdapter.setBitmaps(nonNullBitmaps)
                            binding.tvStoreTitle.text = it.place.name
                            binding.tvStoreType.text =
                                it.place.placeTypes?.get(0) ?: getString(R.string.travel_destination)
                            it.place.reviews?.take(TAKE_REVIEWS)?.forEach { review ->
                                stringBuilder.append("${review.authorAttribution.name}: ${review.text}\n\n")
                            }
                            binding.tvReviews.text = stringBuilder.toString().trim()
                            stringBuilder.clear()
                            binding.tvRatingScore.text = it.place.rating?.toString()
                            binding.rbScore.rating = it.place.rating?.toFloat()!!
                            val weekDayText = it.place.currentOpeningHours?.weekdayText
                            if (!weekDayText.isNullOrEmpty()) {
                                Log.d("TAG", "initViewModel: ${weekDayText}")
                                stringBuilder.append(weekDayText.joinToString("\n"))
                            }
                            binding.tvOpeningHours.text = stringBuilder
                        }
                    }
                }

                launch {
                    detailViewModel.placeResult.collectLatest { result ->
                        val stringBuilder = StringBuilder()
                        result?.let {
                            if (it.photos != null) {
                                stringBuilder.append(it.photos.size.toString())
                                stringBuilder.append(getString(R.string.page))
                                binding.tvImagesSize.text = stringBuilder
                                stringBuilder.clear()
                                val nonNullPhotos = it.photos
                                imageViewPagerAdapter.setPhotos(nonNullPhotos)
                            }
                            binding.tvStoreTitle.text = it.name
                            binding.tvStoreType.text =
                                it.types?.get(0) ?: getString(R.string.travel_destination)
                            it.reviews?.take(TAKE_REVIEWS)?.forEach { review ->
                                stringBuilder.append("${review.authorName}: ${review.text}\n\n")
                            }
                            binding.tvReviews.text = stringBuilder.toString().trim()
                            stringBuilder.clear()
                            binding.tvRatingScore.text = it.rating?.toString()
                            binding.rbScore.rating = it.rating?.toFloat()!!
                            val weekDayText = it.openingHours?.weekdayText
                            if (!weekDayText.isNullOrEmpty()) {
                                Log.d("TAG", "initViewModel: ${weekDayText}")
                                stringBuilder.append(weekDayText.joinToString("\n"))
                            }
                            binding.tvOpeningHours.text = stringBuilder
                        }
                    }
                }
            }
        }
    }

    private fun initListener() {
        binding.apply {
            // text copy
            tvStoreTitle.setOnLongClickListener {
                requireContext().copyTextToClipboard(tvStoreTitle.text.toString())
                true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
