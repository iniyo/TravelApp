package pjo.travelapp.presentation.util


import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.core.widget.NestedScrollView
import com.bumptech.glide.Glide
import pjo.travelapp.R

class FloatingImageViewAnimator(
    private val flAnimater: ViewGroup
) {
    private var isAdVisible = false
    private var isInitialized = false
    private var duration = 300L // 기본 지속 시간

    init {
        // 초기 상태 설정: 처음에 오른쪽 아래 모서리에 맞춰 보이지 않도록 설정
        flAnimater.visibility = View.GONE
        flAnimater.scaleX = 0.0f
        flAnimater.scaleY = 0.0f
    }

    fun setGiffForImageView(imageview: ImageView, context: Context) {
        Glide.with(context)
            .asGif()
            .load(R.raw.gif_floating_button)
            .into(imageview)
    }

    // 계속 맨 처음엔 왼쪽 위에서부터 레이아웃이 확장되는 문제가 발생
    // addOnGlobalLayoutListener를 사용하여 화면에 그려진 뒤에 피벗을 설정.
    // view가 화면에 그려지기 전에 계속해서 위치값을 조정했으므로 설정이 안 되었던 것.
    private fun initializePivot() {
        if (!isInitialized) {
            // viewTreeObserver를 통해 view tree에서 변경을 감지하고 그에대한 응답을 받을 수 있다.
            flAnimater.apply {
                viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        // pivot으로 뷰 위치 설정.
                        pivotX = width.toFloat()
                        pivotY = height.toFloat()
                        isInitialized = true
                    }
                })
            }
        }
    }

    // 스크롤 이벤트 설정
    fun setFloatingAd(nestedScrollView: NestedScrollView) {
        nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY && !isAdVisible) {
                showAd()
            } else if (scrollY == 0 && isAdVisible) {
                hideAd()
            }
        })
    }

    private fun showAd() {
        initializePivot()
        if (!isAdVisible) {
            isAdVisible = true // 애니메이션 시작 전에 설정
            flAnimater.visibility = View.VISIBLE
            flAnimater.animate()
                .alpha(1.0f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(duration)
                .start()
        }
    }

    private fun hideAd() {
        if (isAdVisible) {
            flAnimater.animate()
                .alpha(0.0f)
                .scaleX(0.0f)
                .scaleY(0.0f)
                .setDuration(duration)
                .withEndAction {
                    flAnimater.visibility = View.GONE
                    isAdVisible = false
                }
                .start()
        }
    }

    fun setDuration(newDuration: Long) {
        duration = newDuration
    }
}
