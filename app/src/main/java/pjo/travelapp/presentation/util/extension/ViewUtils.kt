package pjo.travelapp.presentation.util.extension


import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.findViewTreeLifecycleOwner
import pjo.travelapp.presentation.util.calendar.StatusBarColorLifecycleObserver

/**
 * View 확장 함수
 */

// View를 VISIBLE로 설정
fun View.makeVisible() {
    visibility = View.VISIBLE
}
// View를 INVISIBLE로 설정
fun View.makeInVisible() {
    visibility = View.INVISIBLE
}
// View를 GONE으로 설정
fun View.makeGone() {
    visibility = View.GONE
}
/**
 * View 확장 함수 끝
 */

/**
 * context 확장 프로퍼티 및 함수
 */

internal val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)

internal val Context.inputMethodManager
    get() = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

// drawable resource 가져오기
internal fun Context.getDrawableCompat(@DrawableRes drawable: Int): Drawable =
    requireNotNull(ContextCompat.getDrawable(this, drawable))

// color resource 가져오기
internal fun Context.getColorCompat(@ColorRes color: Int) =
    ContextCompat.getColor(this, color)

// base context 찾기
fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}
// text 복사하기
fun Context.copyTextToClipboard(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", text)
    clipboard.setPrimaryClip(clip)
}

/**
 * context 확장 프로퍼티 및 함수 끝
 */

// 텍스트 색상 설정
internal fun TextView.setTextColorRes(@ColorRes color: Int) =
    setTextColor(context.getColorCompat(color))

// Fragment 확장 함수: 상태 표시줄 색상을 업데이트하는 Observer를 추가 -> ViewTree 사용
fun Fragment.addStatusBarColorUpdate(@ColorRes colorRes: Int) {
    view?.findViewTreeLifecycleOwner()?.lifecycle?.addObserver(
        StatusBarColorLifecycleObserver(
            requireActivity(),
            requireContext().getColorCompat(colorRes),
        ),
    )
}


