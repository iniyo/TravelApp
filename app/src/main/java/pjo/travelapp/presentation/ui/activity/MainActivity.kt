package pjo.travelapp.presentation.ui.activity

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import pjo.travelapp.R
import pjo.travelapp.databinding.ActivityMainBinding
import pjo.travelapp.databinding.TopToolbarBinding
import pjo.travelapp.presentation.ui.viewmodel.BaseViewModel
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.AppNavigatorImpl
import pjo.travelapp.presentation.util.navigator.Fragments
import javax.inject.Inject

@AndroidEntryPoint
open class MainActivity : AppCompatActivity() {

    private lateinit var splashScreen: SplashScreen
    private val viewModel: BaseViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    @Inject
    lateinit var navigator: AppNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()
    }

    private fun init() {
        // splash 및 화면 초기화
        startSplash()
        initContentView()
        // 기타 설정
        setClickListener()
        setNavigationOnClick()
        observeDestinationChanges()
    }

    private fun initContentView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setClickListener() {

    }

    private fun startSplash() {
        splashScreen = installSplashScreen()
    }

    private fun setNavigationOnClick() {
        binding.apply {
            cnbItem.setOnItemSelectedListener { id ->
                when (id) {
                    R.id.nav_home -> {
                        navigator.navigateTo(Fragments.HOME_PAGE)
                    }
                    R.id.nav_map -> {
                        navigator.navigateTo(Fragments.MAPS_PAGE)
                    }
                    R.id.nav_planner -> {
                        navigator.navigateTo(Fragments.CALENDAR_PAGE)
                    }
                    R.id.nav_profile -> {
                        navigator.navigateTo(Fragments.USER_PAGE)
                    }
                }
            }
        }
    }

    private fun observeDestinationChanges() {

        navigator.destinationChangedListener { destinationId ->
            binding.apply {
                when (destinationId) {
                    R.id.homeFragment -> {
                        cnbItem.setItemSelected(R.id.nav_home)
                    }
                    R.id.mapsFragment -> {
                        cnbItem.setItemSelected(R.id.nav_map)
                    }
                    R.id.planFragment -> {
                        cnbItem.setItemSelected(R.id.nav_planner)
                    }
                    R.id.userDetailFragment -> {
                        cnbItem.setItemSelected(R.id.nav_profile)
                    }
                }
                if (destinationId == R.id.signFragment || destinationId == R.id.searchFragment) {
                    tvFloatingAiText.visibility = View.GONE
                    lavFloatingAiButton.visibility = View.GONE
                    cnbItem.visibility = View.GONE
                } else {
                    tvFloatingAiText.visibility = View.VISIBLE
                    lavFloatingAiButton.visibility = View.VISIBLE
                    cnbItem.visibility = View.VISIBLE
                }
                if(destinationId == R.id.checkFragment || destinationId == R.id.mapsFragment) {
                    cnbItem.visibility = View.GONE
                }else {
                    cnbItem.visibility = View.VISIBLE
                }
            }
        }
    }
}
