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
import androidx.fragment.app.Fragment
import com.yy.mobile.rollingtextview.CharOrder
import com.yy.mobile.rollingtextview.strategy.Strategy
import dagger.hilt.android.AndroidEntryPoint
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentHomeBinding
import pjo.travelapp.presentation.util.AppNavigator
import pjo.travelapp.presentation.util.Fragments
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var navigator: AppNavigator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSpinnerItems()
        startRollingTextAnimation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setSpinnerItems() {
        val items = resources.getStringArray(R.array.arr_location)
        val mAdapter = ArrayAdapter(requireContext(), R.layout.sp_item, items)
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spLocation.apply {
            adapter = mAdapter
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
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
