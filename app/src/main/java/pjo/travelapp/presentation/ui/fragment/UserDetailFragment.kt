package pjo.travelapp.presentation.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import pjo.travelapp.R
import pjo.travelapp.databinding.FragmentUserDetailBinding
import pjo.travelapp.presentation.adapter.ViewPagerTopSlideAdapter
import pjo.travelapp.presentation.util.MyGraphicMapper
import pjo.travelapp.presentation.util.PageDecoration
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import pjo.travelapp.presentation.util.signmanager.KakaoSignManager
import pjo.travelapp.presentation.util.signmanager.NaverSignManager
import javax.inject.Inject

@AndroidEntryPoint
class UserDetailFragment : Fragment() {

    private var _binding: FragmentUserDetailBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var navigator: AppNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentUserDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListner()
        setAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setClickListner() {
        binding.apply {
            btnLoginAndSignup.setOnClickListener {
                navigator.navigateTo(Fragments.SIGN_PAGE)
            }
        }
    }

    private fun setAdapter() {
        val a = listOf(
            R.drawable.banner1,
            R.drawable.banner2
        )

        val (pageTransX, decoration) = MyGraphicMapper.getDecoration()

        binding.apply {
            vpUserSchedule.apply {
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