package pjo.travelapp.presentation.ui.activity

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import pjo.travelapp.R
import pjo.travelapp.databinding.ActivityMainBinding
import pjo.travelapp.presentation.util.Activitys
import pjo.travelapp.presentation.util.AppNavigator
import pjo.travelapp.presentation.util.Fragments
import pjo.travelapp.presentation.viewmodel.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
open class MainActivity : AppCompatActivity() {

    private lateinit var splashScreen: SplashScreen
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var navigator: AppNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initContentView()
        setNavigationOnClick()
    }

    private fun initContentView() {
        /*// 현재 액티비티의 윈도우 객체 참조 - 액티비티의 UI를 나타냄.
        window.apply {
            // FLAG_LAYOUT_NO_LIMITS - 창의 레이아웃의 화면의 경계를 무시할 수 있도록.
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }*/
        splashScreen = installSplashScreen()
        startSplash()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    // splash의 애니메이션 설정
    private fun startSplash() {
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val bounceAnim = AnimationUtils.loadAnimation(this, R.anim.anim_bounce)

            // bounceAnim 애니메이션을 아이콘 뷰에 적용
            splashScreenView.iconView.startAnimation(bounceAnim)

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

                    R.id.nav_bookmark -> {

                    }

                    R.id.nav_profile -> {
                        navigator.navigateTo(Fragments.DETAIL_PAGE)
                    }
                }
            }
        }
    }
}