package pjo.travelapp.presentation.ui.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.yy.mobile.rollingtextview.CharOrder
import com.yy.mobile.rollingtextview.strategy.Strategy
import dagger.hilt.android.AndroidEntryPoint
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentHomeBinding
import pjo.travelapp.presentation.adapter.CategoryAdapter
import pjo.travelapp.presentation.adapter.PopularAdapter
import pjo.travelapp.presentation.adapter.RecommendedAdapter
import pjo.travelapp.presentation.adapter.ViewPagerTopSlideAdapter
import pjo.travelapp.presentation.util.MyGraphicMapper
import pjo.travelapp.presentation.util.PageDecoration
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    @Inject
    lateinit var navigator: AppNavigator
    private lateinit var a: List<Int>
    private lateinit var b: List<Int>
    private lateinit var c: List<Int>
    private lateinit var d: List<Int>
    lateinit var items: Array<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        items = resources.getStringArray(R.array.arr_location)
        setSpinnerItems()
        startRollingTextAnimation()
        setLottieAnimation()
        setImgaeList()
        setAdapter()
    }

    private fun setSpinnerItems() {

        val mAdapter = ArrayAdapter(requireContext(), R.layout.sp_item, items)
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spLocation.apply {
            adapter = mAdapter
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
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
        val itemMargin = 24
        val previewWidth = 30
        val decoMargin = previewWidth + itemMargin
        val pageTransX = decoMargin + previewWidth
        val decoration = PageDecoration(decoMargin)

        binding.apply {
            vpTopSlider.apply {
                addItemDecoration(decoration)

                setPageTransformer { page, position ->
                    page.translationX = position * - pageTransX
                }

                clipToPadding = false
                clipChildren = false
                adapter = ViewPagerTopSlideAdapter(a)
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                offscreenPageLimit = 2
            }

            rvCategory.apply {
                adapter = CategoryAdapter(b)
                layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                setItemViewCacheSize(b.size) // cache할 아이템 사이즈
                setHasFixedSize(true) // size 일정
            }

            rvRecommended.apply {
                adapter = RecommendedAdapter(c)
                layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                setItemViewCacheSize(c.size)
                setHasFixedSize(true)
            }

            rvPopular.apply {
                adapter = PopularAdapter(c)
                layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                setItemViewCacheSize(c.size)
                setHasFixedSize(true)
            }
        }
    }

    /* // firebase 인증상태 확인
     private fun signInAnonymously() {
         FirebaseAuth.getInstance().signInAnonymously()
             .addOnCompleteListener { task ->
                 if (task.isSuccessful) {
                     fetchImagesFromDatabase()
                 } else {
                     Log.w("TAG", "signInAnonymously:failure", task.exception)
                 }
             }
     }*/


    private fun setLottieAnimation() {
        binding.lavBell.playAnimation()
    }

    private fun startRollingTextAnimation() {
        val rollingText = resources.getStringArray(R.array.arr_rolling)
        var textIndex = 0

        binding.rtvSearch.apply {
            animationDuration = 2000L
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
            setText(rollingText[textIndex]) // 초기 텍스트 설정 및 애니메이션 시작

            setOnClickListener {
                navigator.navigateTo(Fragments.SEARCH_PAGE)
            }
        }
    }
}


