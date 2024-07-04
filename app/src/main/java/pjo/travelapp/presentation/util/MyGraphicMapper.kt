package pjo.travelapp.presentation.util

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP


object MyGraphicMapper {
    private fun convertDensity(value: Float, isPxToDp: Boolean): Float {
        val resources = Resources.getSystem()
        val metrics = resources.displayMetrics // 스마트폰 크기를 가져옮.

        return if (isPxToDp) {
            value / (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
        } else {
            value * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
        }
    }
    private val GESTURE_THRESHOLD_DP = 16.0f

    private var gestureThreshold: Int = 0
    fun getpx(context: Context) {
        val a = TypedValue.applyDimension(
            COMPLEX_UNIT_DIP,
            GESTURE_THRESHOLD_DP + 0.5f,
            context.resources.displayMetrics)
    }

    fun getNavigationBarHeight(context: Context) {
        val resourceId =
            context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        var navigationBarHeight = 0
        // resource id 유효성 체크
        if (resourceId > 0) {
            navigationBarHeight = context.resources.getDimensionPixelSize(resourceId)
            Log.d("deviceSize", "navigation bar : ${px2dp(navigationBarHeight)}")
        }
    }

    // px2
    fun px2dp(px: Int): Int = convertDensity(px.toFloat(), true).toInt()
    fun px2dp(px: Float): Float = convertDensity(px, true)

    // dp2
    fun dp2px(dp: Int): Int = convertDensity(dp.toFloat(), false).toInt()
    fun dp2px(dp: Float): Float = convertDensity(dp, false)

    // offset - viewpager에서 간격으로 사용
    fun offsetPx(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val pageMarginPx = (screenWidth * 0.05).toInt()  // 화면 너비의 5%를 페이지 마진으로 사용
        val pagerWidth = (screenWidth * 0.85).toInt()     // 화면 너비 비례 페이지 너비 사용
        val offsetPx = screenWidth - pageMarginPx - pagerWidth
        return -offsetPx
    }

    // 화면 너비
    fun getScreenWidth(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.widthPixels
    }

    // 화면 높이
    fun getScreenHeight(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.heightPixels
    }

}