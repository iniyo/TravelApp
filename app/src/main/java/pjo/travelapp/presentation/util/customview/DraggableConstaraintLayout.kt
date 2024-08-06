package pjo.travelapp.presentation.util.customview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.abs

class DraggableConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var dX = 0f
    private var dY = 0f
    private var initialX = 0f
    private var initialY = 0f
    private var threshold = 16
    private var topThreshold = 300f // 상단 고정 기준점
    private var bottomThreshold = 500f // 하단 고정 기준점

    init {
        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    initialX = view.x
                    initialY = view.y
                }

                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX + dX
                    val newY = event.rawY + dY
                    view.x = newX
                    view.y = newY
                }

                MotionEvent.ACTION_UP -> {
                    val diffX = abs(view.x - initialX)
                    val diffY = abs(view.y - initialY)

                    if (diffX < threshold && diffY < threshold) {
                        // 이동 거리가 임계값 이하이면 클릭으로 간주
                        performClick()
                    } else {
                        // 중앙을 기준으로 정렬
                        val parentWidth = (view.parent as View).width
                        val parentHeight = (view.parent as View).height
                        val viewCenterX = view.x + view.width / 2

                        if (viewCenterX < parentWidth / 2) {
                            // 왼쪽에 붙이기
                            view.animate().x(30f).setDuration(200L).start()
                        } else {
                            // 오른쪽에 붙이기
                            view.animate().x(parentWidth - view.width.toFloat() - 30f)
                                .setDuration(200L).start()
                        }

                        // 상단과 하단의 기준점에 따라 정렬
                        if (view.y < topThreshold) {
                            // 상단에 붙이기
                            view.animate().y(topThreshold).setDuration(200L).start()
                        } else if (view.y + view.height > parentHeight - bottomThreshold) {
                            // 하단에 붙이기
                            view.animate().y(parentHeight - bottomThreshold).setDuration(200L)
                                .start()
                        }
                    }
                }

                else -> return@setOnTouchListener false
            }
            true
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
