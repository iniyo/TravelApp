package pjo.travelapp.presentation.ui

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateInterpolator
import androidx.activity.viewModels
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import pjo.travelapp.R
import pjo.travelapp.databinding.ActivityMainBinding
import pjo.travelapp.presentation.BaseActivity
import pjo.travelapp.presentation.viewmodel.MainViewModel
import java.time.Duration
import java.time.Instant

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var splashScreen: SplashScreen
    // 해당 프로젝트의 설정 된 minSdkVersion 이후의 API버전을 사용할때 warning을 없애고 사용할 수 있게 해줌
    @SuppressLint("NewApi") // 경고만 무시하므로 낮은 API레벨에서 문제가 발생할 수 있음.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                duration = 2000L
                doOnEnd {
                    splashScreenView.remove()
                }
                start()
            }

        }
    }
}