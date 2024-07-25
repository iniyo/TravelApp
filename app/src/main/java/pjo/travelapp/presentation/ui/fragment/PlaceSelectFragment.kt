package pjo.travelapp.presentation.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.databinding.FragmentPlaceSelectBinding
import pjo.travelapp.presentation.adapter.PlaceSelectAdapter
import pjo.travelapp.presentation.ui.viewmodel.PlanViewModel
import pjo.travelapp.presentation.util.FlexboxItemManager
import pjo.travelapp.data.entity.TravelDestinationAbroad
import pjo.travelapp.data.entity.TravelDestinationDomestic
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class PlaceSelectFragment : BaseFragment<FragmentPlaceSelectBinding>() {

    private val planViewModel: PlanViewModel by activityViewModels()
    @Inject lateinit var navigator: AppNavigator

    override fun initView() {
        bind {

            val abroadData = TravelDestinationAbroad()
            val domesticData = TravelDestinationDomestic()
            planViewModel.fetchAbroadPlace()

            btnSelectPlace.isEnabled = fblPlaceContainer.childCount > 0
            if( fblPlaceContainer.childCount > 0){
                Log.d("TAG", "btnSelectPlace: ${fblPlaceContainer.childCount} ")
            }else {
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

            val itemManager = FlexboxItemManager(requireContext(), fblPlaceContainer)
            adapter = PlaceSelectAdapter { (title, imgResId) ->
                itemManager.addDeletableItem(
                    imageResource = imgResId,
                    textResId = title // 필요한 텍스트 리소스 ID를 여기에 설정
                )
                planViewModel.fetchPlace(title)
            }
            rvPlaceList.adapter = adapter
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
            }
        }
    }

    override fun initListener() {
        bind {
            btnSelectPlace.setOnClickListener {
                Log.d("TAG", "btnSelectPlace: ")
                navigator.navigateTo(Fragments.CALENDAR_PAGE)
            }
        }
    }

    private fun generateRandomId(prefix: String): String {
        return prefix + UUID.randomUUID().toString()
    }
}
