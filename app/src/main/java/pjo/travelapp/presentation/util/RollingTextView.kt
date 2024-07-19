package pjo.travelapp.presentation.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.animation.*
import androidx.appcompat.widget.AppCompatTextView
import pjo.travelapp.R

class RollingTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var textArray: Array<String> = arrayOf()
    private var currentIndex = 0
    private var duration: Long = 3000L // Default duration
    private val handler = Handler(Looper.getMainLooper())

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.RollingTextView, 0, 0).apply {
            try {
                duration = getInteger(R.styleable.RollingTextView_duration, 3000).toLong()
            } finally {
                recycle()
            }
        }
    }

    fun setTextArray(textArray: Array<String>) {
        this.textArray = textArray
        currentIndex = 0
        startRolling()
    }

    fun setDuration(duration: Long) {
        this.duration = duration
    }

    private fun startRolling() {
        if (textArray.isEmpty()) return

        handler.post { animateText() }
    }

    private fun animateText() {
        val nextText = textArray[currentIndex]
        currentIndex = (currentIndex + 1) % textArray.size

        // 애니메이션 세트를 사용하여 중간에 잠시 멈추는 효과를 추가
        val animationSet = AnimationSet(true)

        // 텍스트가 생성되는 위치를 약간 아래로 조정
        val enterAnimation = TranslateAnimation(0f, 0f, -height.toFloat() * 0.7f, 0f)
        enterAnimation.duration = duration / 2
        enterAnimation.interpolator = DecelerateInterpolator()

        // 텍스트가 소멸되는 위치를 약간 위로 조정
        val exitAnimation = TranslateAnimation(0f, 0f, 0f, height.toFloat() * 0.7f)
        exitAnimation.startOffset = duration / 2
        exitAnimation.duration = duration / 2
        exitAnimation.interpolator = AccelerateInterpolator()

        animationSet.addAnimation(enterAnimation)
        animationSet.addAnimation(exitAnimation)
        animationSet.fillAfter = true
        animationSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                text = nextText
            }

            override fun onAnimationEnd(animation: Animation?) {
                // 애니메이션 종료 시 바로 다음 애니메이션 시작
                animateText()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        this.startAnimation(animationSet)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startRolling()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacksAndMessages(null)
    }
}
