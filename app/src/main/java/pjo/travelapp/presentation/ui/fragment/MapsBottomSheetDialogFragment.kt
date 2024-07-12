package pjo.travelapp.presentation.ui.fragment

import android.content.res.ColorStateList
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentMapsBottomSheetBinding
import pjo.travelapp.presentation.ui.viewmodel.MapsViewModel
import pjo.travelapp.presentation.util.LatestUiState

class MapsBottomSheetDialogFragment :
    BaseFragment<FragmentMapsBottomSheetBinding>(R.layout.fragment_maps_bottom_sheet) {

    private val viewModel: MapsViewModel by viewModels()

    override fun initView() {
        super.initView()
        bind {
            main = this@MapsBottomSheetDialogFragment

            setDialog()
            setVariable()
        }
    }

    private fun setDialog() {
        bind {
            clTabContainer1.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
            }
        }
    }

    private fun setVariable() {
        bind {
            launchWhenStarted {
                viewModel.placeDetails.collect { res ->
                    /*when (res) {
                        is LatestUiState.Success -> {
                            res.data.result.apply {
                                Log.d("TAG", "setVariable : ${name}")
                                tvStoreTitle.text = name.ifEmpty { "" }
                                tvStoreType.text = types[0].ifEmpty { "" }
                                tvRatingScore.text = rating.toString().ifEmpty { "" }
                                rbScore.rating = if (rating.toString()
                                        .isNotEmpty()
                                ) rating.toFloat() else 0f

                                photos.getOrNull(0)?.let { photo ->
                                    val photoRef = photo.photoReference
                                    Log.d("TAG", "fetchPlaceIdAndDetails: $photoRef")
                                    Glide.with(requireContext())
                                        .load(photo.getPhotoUrl())
                                        .placeholder(R.drawable.img_bg_title)
                                        .into(ivStoreTitle)
                                }
                                tvMapsLocation.text = vicinity.ifEmpty { "" }
                                tvMapsWebsite.text = website?.ifEmpty { "" }
                                tvCallNumber.text = formattedPhoneNumber?.ifEmpty { "" }
                                if (openingHours?.openNow == true) {
                                    tvOpenCloseCheck.text = resources.getString(R.string.opening)
                                    val color =
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.selected_icon_color
                                        )
                                    tvOpenCloseCheck.setTextColor(ColorStateList.valueOf(color))
                                } else {
                                    tvOpenCloseCheck.text = resources.getString(R.string.closed)
                                    val color = ContextCompat.getColor(
                                        requireContext(),
                                        R.color.dark_light_gray
                                    )
                                    tvOpenCloseCheck.setTextColor(ColorStateList.valueOf(color))
                                }

                                val weekdayTextList = openingHours?.weekdayText
                                tvOpenCloseTime.text = if (!weekdayTextList.isNullOrEmpty()) {
                                    weekdayTextList.joinToString("\n")
                                } else {
                                    ""
                                }
                            }
                        }

                        is LatestUiState.Error -> {
                            Log.e("TAG", "setVariable: error")
                        }

                        LatestUiState.Loading -> {}
                    }*/
                }
            }
        }
    }

}