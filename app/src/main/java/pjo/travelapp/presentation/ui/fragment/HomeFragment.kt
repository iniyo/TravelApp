package pjo.travelapp.presentation.ui.fragment

import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentHomeBinding
import pjo.travelapp.presentation.adapter.CategoryRecyclerAdapter
import pjo.travelapp.presentation.adapter.MorePlacesViewPagerAdapter
import pjo.travelapp.presentation.adapter.RecommendedRecyclerAdapter
import pjo.travelapp.presentation.adapter.TopSlideViewPagerAdapter
import pjo.travelapp.presentation.ui.viewmodel.MainViewModel
import pjo.travelapp.presentation.util.mapper.MyGraphicMapper
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    @Inject
    lateinit var navigator: AppNavigator
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var a: List<Int>
    private lateinit var b: List<Int>
    private lateinit var c: List<Int>


    override fun initCreate() {
        super.initCreate()
        setImgaeList()
    }

    override fun initView() {
        super.initView()
        startRollingTextAnimation()
        setLottieAnimation()
        setAdapter()
        setTabLayout()
    }

    private fun setTabLayout() {
        val marginSize = resources.getDimensionPixelSize(R.dimen.tab_item_margin)
        bind {
            setTabItemMargin(tlTop, marginSize, marginSize)
            tlTop.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.let {
                        Log.d("TAG", "onTabSelected: ${tab.position}")
                        val fragments = mainViewModel.getForkFragments(it.position)
                        adapter = MorePlacesViewPagerAdapter(requireActivity(), fragments)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {

                }

                override fun onTabReselected(tab: TabLayout.Tab?) {

                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        binding.adapter = null
    }

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

    private fun setImgaeList() {
        a = listOf(
            R.drawable.banner1,
            R.drawable.banner2
        )
        b = listOf(
            R.drawable.cat1,
            R.drawable.cat2,
            R.drawable.cat3,
            R.drawable.cat4,
            R.drawable.cat5
        )
        c = listOf(
            R.drawable.item_1,
            R.drawable.item_2,
            R.drawable.item_3,
            R.drawable.item_4,
            R.drawable.item_5
        )
    }

    private fun setAdapter() {

        val (pageTransX, decoration) = MyGraphicMapper.getDecoration()

        binding.apply {
            vpTopSlider.apply {
                addItemDecoration(decoration)

                setPageTransformer { page, position ->
                    page.translationX = position * -pageTransX
                }

                clipToPadding = false
                clipChildren = false
                adapter = TopSlideViewPagerAdapter(a)
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                offscreenPageLimit = 2
            }

            rvCategory.apply {
                adapter = CategoryRecyclerAdapter(b)
                layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                setItemViewCacheSize(b.size) // cache할 아이템 사이즈
                setHasFixedSize(true) // size 일정
            }

            rvRecommended.apply {
                adapter = RecommendedRecyclerAdapter(c)
                layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                setItemViewCacheSize(c.size)
                setHasFixedSize(true)
            }

            /*rvPopular.apply {
                adapter = PopularRecyclerAdapter(c)
                layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                setItemViewCacheSize(c.size)
                setHasFixedSize(true)
            }*/
            /*   val fragments = mainViewModel.getForkFragments(0)
               adapter = MorePlacesViewPagerAdapter(requireActivity(), fragments)*/
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


