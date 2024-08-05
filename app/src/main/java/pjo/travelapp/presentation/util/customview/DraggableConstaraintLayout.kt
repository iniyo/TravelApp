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
    private var maxMoveX = 1000f // X축 이동의 최대 허용 값
    private var maxMoveY = 1000f // Y축 이동의 최대 허용 값

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
                    if (abs(newX - initialX) < maxMoveX && abs(newY - initialY) < maxMoveY) {
                        view.x = newX
                        view.y = newY
                    }
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
                        val viewCenterY = view.y + view.height / 2

                        if (diffX > maxMoveX || diffY > maxMoveY) {
                            // 최대 이동 범위를 초과하면 원래 위치로 돌아감
                            view.animate().x(initialX).y(initialY).setDuration(200L).start()
                        } else {
                            if (viewCenterX < parentWidth / 2) {
                                // 왼쪽에 붙이기
                                view.animate().x(30f).setDuration(200L).start()
                            } else {
                                // 오른쪽에 붙이기
                                view.animate().x(parentWidth - view.width.toFloat() - 30f).setDuration(200L).start()
                            }

                            if (viewCenterY < parentHeight / 2) {
                                // 상단에 붙이기
                                view.animate().y(30f).setDuration(200L).start()
                            } else {
                                // 하단에 붙이기
                                view.animate().y(parentHeight - view.height.toFloat() - 30f).setDuration(200L).start()
                            }
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
