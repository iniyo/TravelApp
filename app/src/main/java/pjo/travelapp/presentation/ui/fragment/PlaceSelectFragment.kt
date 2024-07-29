package pjo.travelapp.presentation.ui.fragment

import android.util.Log
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
import pjo.travelapp.presentation.util.FlexboxItemManager
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
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

            val abroadData = TravelDestinationAbroad()
            val domesticData = TravelDestinationDomestic()
            planViewModel.fetchAbroadPlace()
            val itemManager = FlexboxItemManager(requireContext(), fblPlaceContainer)
            val addedItems = mutableSetOf<String>()

            adapter = PlaceSelectAdapter { (place, imageResId) ->
                title = place
                imgResId = imageResId
                // 중복 아이템 체크
                if (addedItems.contains(title)) {
                    Log.d("ItemManager", "${title}은 이미 추가하셨습니다.")
                } else if (addedItems.size >= 3) {
                    Log.d("ItemManager", "3개까지만 선택해주세요")
                } else {
                    itemManager.addDeletableItem(
                        imageResource = imageResId,
                        textResId = title!!
                    ) {
                        Log.d("TAG", "Deleting: $it")
                        planViewModel.deletePlace(Pair(it, imageResId))
                        addedItems.remove(it)
                    }
                    addedItems.add(title!!)
                    planViewModel.updateSelectedPlace(title!!, imgResId!!)
                }
            }

            fblPlaceContainer.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
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
        bind {
            title = null
            imgResId = null
        }
    }
}
