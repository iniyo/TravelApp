package pjo.travelapp.presentation.ui.fragment

import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentUserDetailBinding
import pjo.travelapp.presentation.adapter.TopSlideViewPagerAdapter
import pjo.travelapp.presentation.util.mapper.MyGraphicMapper
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import javax.inject.Inject

@AndroidEntryPoint
class UserDetailFragment : BaseFragment<FragmentUserDetailBinding>() {

    @Inject
    lateinit var navigator: AppNavigator

    override fun initView() {
        super.initView()
        setClickListner()
        setAdapter()
    }

    private fun setClickListner() {
        bind {
            btnLoginAndSignup.setOnClickListener {
                navigator.navigateTo(Fragments.SIGN_PAGE, "")
            }
        }
    }

    private fun setAdapter() {
        val a = listOf(
            R.drawable.banner1,
            R.drawable.banner2
        )

        val (pageTransX, decoration) = MyGraphicMapper.getDecoration()

        bind {
            vpUserSchedule.apply {
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
        }
    }

    private fun setScreenSize() {
        binding.apply {
            val screenWidth = MyGraphicMapper.getScreenWidth(root.context)
            /*btnFavorite.layoutParams.height = (screenWidth * 0.4).toInt()
            btnFavorite.layoutParams.width = (screenWidth * 0.4).toInt()
            btnReservation.layoutParams.height = (screenWidth * 0.4).toInt()
            btnReservation.layoutParams.width = (screenWidth * 0.4).toInt()
            llBtnMyTrip.layoutParams.height = (screenWidth * 0.4).toInt()
            llBtnMyTrip.layoutParams.width = (screenWidth * 0.4).toInt()
            btnScheduleCalendar.layoutParams.height = (screenWidth * 0.4).toInt()
            btnScheduleCalendar.layoutParams.width = (screenWidth * 0.4).toInt()*/
        }
    }

}