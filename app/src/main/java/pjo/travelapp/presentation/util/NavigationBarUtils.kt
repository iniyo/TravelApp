package pjo.travelapp.presentation.util

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowInsets
import androidx.annotation.RequiresApi

class NavigationBarUtils(private val context: Context) {

    fun getNavigationBarHeight(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            calculateNavigationBarHeightForApi30AndAbove()
        } else {
            calculateNavigationBarHeightForBelowApi30()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun calculateNavigationBarHeightForApi30AndAbove(): Int {
        val windowMetrics = (context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager).currentWindowMetrics
        val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars())
        return insets.bottom
    }

    private fun calculateNavigationBarHeightForBelowApi30(): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val usableHeight = displayMetrics.heightPixels

        windowManager.defaultDisplay.getRealMetrics(displayMetrics)

        val realHeight = displayMetrics.heightPixels

        return realHeight - usableHeight
    }

    fun getNavigationBarWidth(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            calculateNavigationBarWidthForApi30AndAbove()
        } else {
            calculateNavigationBarWidthForBelowApi30()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun calculateNavigationBarWidthForApi30AndAbove(): Int {
        val windowMetrics = (context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager).currentWindowMetrics
        val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars())
        return insets.right
    }

    private fun calculateNavigationBarWidthForBelowApi30(): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val usableWidth = displayMetrics.widthPixels

        windowManager.defaultDisplay.getRealMetrics(displayMetrics)

        val realWidth = displayMetrics.widthPixels

        return realWidth - usableWidth
    }
}
