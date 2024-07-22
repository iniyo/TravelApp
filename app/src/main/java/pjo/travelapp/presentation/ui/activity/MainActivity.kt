package pjo.travelapp.presentation.ui.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import pjo.travelapp.R
import pjo.travelapp.databinding.ActivityMainBinding
import pjo.travelapp.presentation.ui.viewmodel.BaseViewModel
import pjo.travelapp.presentation.util.navigator.AppNavigator
import pjo.travelapp.presentation.util.navigator.Fragments
import javax.inject.Inject

@AndroidEntryPoint
open class MainActivity : AppCompatActivity() {

    private lateinit var splashScreen: SplashScreen
    private val viewModel: BaseViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var navigator: AppNavigator

    private var backPressedOnce = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        // splash 및 화면 초기화
        startSplash()
        initContentView()
        setNavigationOnClick()
        observeDestinationChanges()
        setupOnBackPressedDispatcher()
        setViewModel()
    }

    private fun startSplash() {
        splashScreen = installSplashScreen()
    }

    private fun initContentView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun handleBackStack() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 3) {
            fragmentManager.popBackStack(
                fragmentManager.getBackStackEntryAt(0).id,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }

    // chip nav bar 이동
    private fun setNavigationOnClick() {
        binding.apply {
            cnbItem.setOnItemSelectedListener { id ->
                when (id) {
                    R.id.nav_home -> navigator.navigateTo(Fragments.HOME_PAGE, "")
                    R.id.nav_map -> navigator.navigateTo(Fragments.MAPS_PAGE, "")
                    R.id.nav_planner -> navigator.navigateTo(Fragments.CALENDAR_PAGE, "")
                    R.id.nav_profile -> navigator.navigateTo(Fragments.USER_PAGE, "")
                }
            }
        }
    }

    private fun setViewModel() {
        viewModel.fetchData()
    }

    // 현재 프래그먼트 계산
    private fun observeDestinationChanges() {
        navigator.destinationChangedListener { destinationId ->
            handleBackStack()
            binding.apply {
                when (destinationId) {
                    R.id.homeFragment -> cnbItem.setItemSelected(R.id.nav_home)
                    R.id.mapsFragment -> cnbItem.setItemSelected(R.id.nav_map)
                    R.id.planFragment -> cnbItem.setItemSelected(R.id.nav_planner)
                    R.id.userDetailFragment -> cnbItem.setItemSelected(R.id.nav_profile)
                }
                if (destinationId == R.id.mainSearchFragment) {
                    tvFloatingAiText.visibility = View.GONE
                    lavFloatingAiButton.visibility = View.GONE
                    cnbItem.visibility = View.GONE
                } else {
                    tvFloatingAiText.visibility = View.VISIBLE
                    lavFloatingAiButton.visibility = View.VISIBLE
                    cnbItem.visibility = View.VISIBLE
                }
                if (destinationId == R.id.checkFragment || destinationId == R.id.mapsFragment || destinationId == R.id.signFragment) {
                    cnbItem.visibility = View.GONE
                } else {
                    cnbItem.visibility = View.VISIBLE
                }
            }
        }
    }

    // 뒤로가기 버튼
    private fun setupOnBackPressedDispatcher() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navController = navigator.retrieveNavController()
                if (navController.currentDestination?.id == R.id.homeFragment) {
                    if (backPressedOnce) {
                        finish()
                    } else {
                        backPressedOnce = true
                        Snackbar.make(binding.root, "한번 더 누르면 앱이 종료됩니다.", Snackbar.LENGTH_SHORT).show()
                        handler.postDelayed({ backPressedOnce = false }, 2000) // 2초 안에 클릭 시 종료
                    }
                } else {
                    navController.navigateUp()
                }
            }
        })
    }
}
