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
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import pjo.travelapp.R
import pjo.travelapp.databinding.ActivityMainBinding
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

        initContentView()
        setNavigationOnClick()
        setFloatingButton()
        observeDestinationChanges()
    }

    private fun initContentView() {
        splashScreen = installSplashScreen()
        startSplash()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setFloatingButton() {
        binding.apply {
            Glide.with(baseContext)
                .asGif()
                .load(R.raw.gif_floating_button)
                .into(ivFloatingAiButton)
        }
    }

    private fun startSplash() {
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            ObjectAnimator.ofPropertyValuesHolder(splashScreenView.iconView).run {
                interpolator = AnticipateInterpolator()
                duration = 1500L
                doOnEnd {
                    splashScreenView.remove()
                }
                start()
            }
        }
    }

    private fun setNavigationOnClick() {
        binding.apply {
            cnbItem.setOnItemSelectedListener { id ->
                when (id) {
                    R.id.nav_home -> {
                        navigator.navigateTo(Fragments.HOME_PAGE)
                    }
                    R.id.nav_explorer -> {
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
                if (destinationId == R.id.signFragment || destinationId == R.id.searchFragment) {
                    tvFloatingAiText.visibility = View.GONE
                    ivFloatingAiButton.visibility = View.GONE
                } else {
                    tvFloatingAiText.visibility = View.VISIBLE
                    ivFloatingAiButton.visibility = View.VISIBLE
                }
            }
        }
    }
}
