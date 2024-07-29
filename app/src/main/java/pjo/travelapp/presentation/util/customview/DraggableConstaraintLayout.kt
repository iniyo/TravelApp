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
    private var lastAction = 0
    private var widgetInitialX = 0f
    private var widgetInitialY  = 0f

    init {
        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    lastAction = MotionEvent.ACTION_DOWN
                }

                MotionEvent.ACTION_MOVE -> {
                    view.x = event.rawX + dX
                    view.y = event.rawY + dY
                    widgetInitialX = view.x
                    widgetInitialY = view.y
                    lastAction = MotionEvent.ACTION_MOVE
                }

                MotionEvent.ACTION_UP -> {
                    if (lastAction == MotionEvent.ACTION_DOWN) {
                        // 클릭 이벤트로 간주
                        performClick()
                    } else {
                        // 중앙을 기준으로 정렬
                        val parentWidth = (view.parent as View).width
                        val viewCenterX = view.x + view.width / 2

                        if (viewCenterX < parentWidth / 2) {
                            // 왼쪽에 붙이기
                            view.animate().x(30f).setDuration(200L)

                        } else {
                            // 오른쪽에 붙이기
                            view.animate().x(parentWidth - view.width.toFloat() - 30f).setDuration(200L)
                        }
                    }
                    // 이동이 별로 없다면 클릭으로 간주
                    if (abs(view.x - widgetInitialX) <= 16 && abs(view.y - widgetInitialY) <= 16)
                        view.performClick()
                }

                else -> return@setOnTouchListener false
            }
            true
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        // 여기서 클릭 이벤트를 처리할 수 있습니다.
        return true
    }
}
