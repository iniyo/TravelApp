package pjo.travelapp.presentation.util

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import pjo.travelapp.R
import java.lang.ref.WeakReference


/**
 * 상태 표시줄 색상 업데이트 옵저버
 */
@Suppress("DEPRECATION") // API 레벨 30 이하에서도 사용이 가능하도록
class StatusBarColorLifecycleObserver(
    activity: Activity,
    @ColorInt private val color: Int,
) : DefaultLifecycleObserver {
    private val isLightColor = ColorUtils.calculateLuminance(color) > 0.5
    private val defaultStatusBarColor = activity.getColorCompat(R.color.color_primary_dark)
    private val activity = WeakReference(activity)

    override fun onStart(owner: LifecycleOwner) {
        activity.get()?.window?.apply {
            statusBarColor = color
            if (isLightColor) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    decorView.windowInsetsController?.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        activity.get()?.window?.apply {
            statusBarColor = defaultStatusBarColor
            if (isLightColor) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    decorView.windowInsetsController?.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    decorView.systemUiVisibility = 0
                }
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) = activity.clear()
}
