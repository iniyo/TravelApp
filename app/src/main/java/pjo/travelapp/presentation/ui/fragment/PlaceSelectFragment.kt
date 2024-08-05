package pjo.travelapp.presentation.ui.fragment

import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.data.entity.TravelDestinationAbroad
import pjo.travelapp.data.entity.TravelDestinationDomestic
import pjo.travelapp.databinding.FragmentPlaceSelectBinding
import pjo.travelapp.presentation.adapter.PlaceSelectAdapter
import pjo.travelapp.presentation.ui.viewmodel.PlanViewModel
import pjo.travelapp.presentation.util.BitmapUtil
import pjo.travelapp.presentation.util.FlexboxItemManager
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import pjo.travelapp.presentation.util.showCustomSnackbar
import javax.inject.Inject

@AndroidEntryPoint
class PlaceSelectFragment : BaseFragment<FragmentPlaceSelectBinding>() {

    private val planViewModel: PlanViewModel by activityViewModels()

    @Inject
    lateinit var navigator: AppNavigator
    private var title: String? = null
    private var imgResId: Int? = null

    override fun initView() {
        bind {
            planViewModel.clearCurrentUserEntity()
            planViewModel.fetchAbroadPlace()
            val abroadData = TravelDestinationAbroad()
            val domesticData = TravelDestinationDomestic()
            val itemManager = FlexboxItemManager(requireContext(), fblPlaceContainer)
            val addedItems = mutableSetOf<String>()

            adapter = PlaceSelectAdapter { (place, imageResId) ->
                title = place
                val bitmaps = BitmapFactory.decodeResource(binding.root.context.resources, imageResId)
                // 중복 아이템 체크
                if (addedItems.contains(title)) {
                    showCustomSnackbar(root, "ItemManager ${title}은 이미 추가하셨습니다.", requireContext())
                } else if (addedItems.size > 2) {
                    showCustomSnackbar(root, "3개만 선택해 주세요", requireContext())
                } else {
                    itemManager.addDeletableItem(
                        imageResource = imageResId,
                        textResId = title!!
                    ) {
                        Log.d("TAG", "Deleting: $it")
                        planViewModel.deletePlace(Pair(it, bitmaps))
                        addedItems.remove(it)
                    }
                    addedItems.add(title!!)

                    planViewModel.updateSelectedPlace(title!!, bitmaps)
                    planViewModel.saveBitmaps(requireContext())
                }
            }

            fblPlaceContainer.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                btnSelectPlace.isEnabled = fblPlaceContainer.childCount > 0
            }

            if (fblPlaceContainer.childCount > 0) {
                Log.d("TAG", "btnSelectPlace: ${fblPlaceContainer.childCount} ")
            } else {
                Log.d("TAG", "btnSelectPlace: ${fblPlaceContainer.childCount} ")
            }

            tlChooseDomesticOrAbroad.addOnTabSelectedListener(object :
                TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.let {
                        when (it.position) {
                            0 -> {
                                // 해외
                                adapter?.setData(
                                    abroadData.getImgList(),
                                    abroadData.getTitleList(),
                                    abroadData.getSubTitleList()
                                )
                            }

                            1 -> {
                                // 국내
                                adapter?.setData(
                                    domesticData.getImgList(),
                                    domesticData.getTitleList(),
                                    domesticData.getSubTitleList()
                                )
                            }

                            else -> {}
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    // 필요시 구현
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    // 필요시 구현
                }
            })
        }
    }

    override fun initViewModel() {
        bind {
            launchWhenStarted {
                launch {
                    planViewModel.abroadPlace.collectLatest {
                        it?.let {
                            adapter?.setData(
                                it.getImgList(),
                                it.getTitleList(),
                                it.getSubTitleList()
                            )
                        }
                    }
                }
                launch {
                    planViewModel.planAdapterList.collectLatest {

                    }
                }
            }
        }
    }

    override fun initListener() {
        bind {
            btnSelectPlace.setOnClickListener {
                Log.d("TAG", "btnSelectPlace: ")
                navigator.navigateTo(Fragments.CALENDAR_PAGE)
            }
            ivBack.setOnClickListener {
                navigator.navigateUp()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("TAG", "onPause: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("TAG", "onDestroy: ")
        bind {
            title = null
            imgResId = null
        }
    }
}
