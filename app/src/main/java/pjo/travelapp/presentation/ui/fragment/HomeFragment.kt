package pjo.travelapp.presentation.ui.fragment

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentHomeBinding
import pjo.travelapp.presentation.adapter.CategoryRecyclerAdapter
import pjo.travelapp.presentation.adapter.MorePlacesViewPagerAdapter
import pjo.travelapp.presentation.adapter.PromotionSlideAdapter
import pjo.travelapp.presentation.adapter.RecommendedRecyclerAdapter
import pjo.travelapp.presentation.adapter.ScheduleDefaultAdapter
import pjo.travelapp.presentation.ui.viewmodel.AiChatViewModel
import pjo.travelapp.presentation.ui.viewmodel.DetailViewModel
import pjo.travelapp.presentation.ui.viewmodel.MainViewModel
import pjo.travelapp.presentation.util.LatestUiState
import pjo.travelapp.presentation.util.mapper.MyGraphicMapper
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    @Inject
    lateinit var navigator: AppNavigator
    private val mainViewModel: MainViewModel by activityViewModels()
    private val detailViewModel: DetailViewModel by activityViewModels()

    override fun initView() {
        super.initView()
        startRollingTextAnimation()
        setLottieAnimation()
        setTabLayout()
    }

    override fun initViewModel() {
        super.initViewModel()
        bind {
            launchWhenStarted {
                launch {
                    mainViewModel.shuffledHotPlaceList.collectLatest {
                        when (it) {
                            is LatestUiState.Loading -> { sflPopular.visibility = View.VISIBLE}
                            is LatestUiState.Success -> {
                                sflPopular.visibility = View.GONE
                                it.data.forEach { res ->
                                    popularRecycleAdapter?.addPlace(res)
                                }
                            }
                            is LatestUiState.Error -> it.exception.printStackTrace()
                        }
                    }
                }
                launch {
                    mainViewModel.shuffledHotPlaceList.collectLatest {
                        when (it) {
                            is LatestUiState.Loading -> {sflRecommended.visibility = View.VISIBLE}
                            is LatestUiState.Success -> {
                                sflRecommended.visibility = View.GONE
                                it.data.forEach { res ->
                                    recommendedRecycleAdapter?.addPlace(res)
                                }
                            }
                            is LatestUiState.Error -> it.exception.printStackTrace()
                        }
                    }
                }

                launch {
                    mainViewModel.promotionData.collectLatest {
                        when (it) {
                            is LatestUiState.Loading -> pbBanner.visibility = View.VISIBLE
                            is LatestUiState.Success -> {
                                pbBanner.visibility = View.GONE
                                it.data.forEach { res ->
                                    topPromotionViewpagerAdapter?.addAd(res)
                                }
                            }
                            is LatestUiState.Error -> it.exception.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private fun setTabLayout() {
        val marginSize = resources.getDimensionPixelSize(R.dimen.tab_item_margin)
        bind {
            setTabItemMargin(tlTop, marginSize, marginSize)

            tlTop.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.let {
                        // ViewPager의 어댑터를 탭의 위치에 따라 다시 설정
                        morePlaceViewpagerAdapter = MorePlacesViewPagerAdapter(requireActivity(), it.position)
                        vpTabItemsShow.adapter = morePlaceViewpagerAdapter

                        // ViewPager의 현재 페이지를 0으로 설정 (첫 페이지)
                        vpTabItemsShow.setCurrentItem(0, true)

                        Log.d("TAG", "onTabSelected: ${tab.position}")
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    // 필요한 경우 추가적인 처리를 여기에 작성
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    // 필요한 경우 추가적인 처리를 여기에 작성
                }
            })
        }
    }


    /* override fun onPause() {
         super.onPause()
         binding.morePlaceViewpagerAdapter = null
     }*/

    // TabLayout Tab 사이 간격 부여
    private fun setTabItemMargin(tabLayout: TabLayout, marginStart: Int, marginEnd: Int) {
        val tabs = tabLayout.getChildAt(0) as ViewGroup
        for (i in 0 until tabs.childCount) {
            val tab = tabs.getChildAt(i)
            val lp = tab.layoutParams as LinearLayout.LayoutParams
            lp.marginStart = marginStart
            lp.marginEnd = marginEnd
            tab.layoutParams = lp


            tabLayout.requestLayout()
        }
    }

    override fun initAdapter() {
        val (pageTransX, decoration) = MyGraphicMapper.getDecoration()

        bind {
            vpPromotion.apply {
                addItemDecoration(decoration)
                setPageTransformer { page, position ->
                    page.translationX = position * -pageTransX
                }
                offscreenPageLimit = 2
            }
            rvCategory.setHasFixedSize(true)
            vpTabItemsShow.offscreenPageLimit = 2

            val scehduleAdapter = ScheduleDefaultAdapter{
                detailViewModel.fetchPlaceDetails(it)
                navigator.navigateTo(Fragments.PLACE_DETAIL_PAGE)
            }

            topPromotionViewpagerAdapter = PromotionSlideAdapter()

            categoryRecycleAdapter = CategoryRecyclerAdapter {
                when(it) {
                    "항공" -> {}
                    "호텔" -> {}
                    "관광지" -> {}
                    "내 여행 계획" -> { navigator.navigateTo(Fragments.PLACE_SELECT_PAGE)}
                    "근처 맛집" -> {}
                }
            }
            recommendedRecycleAdapter = RecommendedRecyclerAdapter{
                detailViewModel.fetchPlaceDetails(it)
                navigator.navigateTo(Fragments.PLACE_DETAIL_PAGE)
            }
            popularRecycleAdapter = scehduleAdapter
            morePlaceViewpagerAdapter = MorePlacesViewPagerAdapter(requireActivity(), 0)
        }
    }

    private fun setLottieAnimation() {
        binding.lavBell.playAnimation()
    }

    private fun startRollingTextAnimation() {
        val rollingText = resources.getStringArray(R.array.arr_rolling)

        binding.rtvSearch.apply {
            /* animationDuration = 1500L
             animationInterpolator = AccelerateDecelerateInterpolator()
             addCharOrder(CharOrder.Alphabet)
             addCharOrder(CharOrder.UpperAlphabet)
             addCharOrder(CharOrder.Number)
             addCharOrder(CharOrder.Hex)
             addCharOrder(CharOrder.Binary)

             charStrategy = Strategy.StickyAnimation(0.9)

             addAnimatorListener(object : AnimatorListenerAdapter() {
                 override fun onAnimationEnd(animation: Animator) {
                     super.onAnimationEnd(animation)
                     // 애니메이션 종료 시 다음 텍스트로 변경
                     textIndex = (textIndex.inc()) % rollingText.size
                     setText(rollingText[textIndex])
                 }
             })
             setText(rollingText[textIndex]) // 초기 텍스트 설정 및 애니메이션 시작*/

            setTextArray(rollingText)
            setDuration(3000L) // 2 seconds duration


            setOnClickListener {
                navigator.navigateTo(Fragments.SEARCH_PAGE)
            }
        }
    }
}


