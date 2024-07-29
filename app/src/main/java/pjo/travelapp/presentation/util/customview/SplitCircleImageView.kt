package pjo.travelapp.presentation.util.customview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class SplitCircleImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
    }

    private val whitePaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
    }

    private val path = Path()
    private var bitmaps: List<Bitmap> = emptyList()
    private var spaceAngle = 5f // 각 이미지 사이의 여백 각도

    fun setBitmaps(bitmaps: List<Bitmap>) {
        this.bitmaps = bitmaps
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (bitmaps.isEmpty()) {
            super.onDraw(canvas)
            return
        }

        val count = bitmaps.size
        val cx = width / 2f
        val cy = height / 2f
        val radius = width.coerceAtMost(height) / 2f

        canvas.save()
        path.reset()
        path.addCircle(cx, cy, radius, Path.Direction.CW)
        canvas.clipPath(path)

        if (count == 1) {
            // 이미지가 하나일 때는 전체 영역에 이미지를 그립니다.
            val bitmap = bitmaps[0]
            canvas.drawBitmap(
                bitmap,
                null,
                RectF(cx - radius, cy - radius, cx + radius, cy + radius),
                paint
            )
        } else {
            // 이미지가 여러 개일 때는 균등하게 분할하여 그립니다.
            val totalSpaceAngle = spaceAngle * count
            val angleStep = (360f - totalSpaceAngle) / count

            bitmaps.forEachIndexed { index, bitmap ->
                val startAngle = index * (angleStep + spaceAngle)
                val sweepAngle = angleStep

                // Draw the image slice
                path.reset()
                path.moveTo(cx, cy)
                path.arcTo(
                    cx - radius,
                    cy - radius,
                    cx + radius,
                    cy + radius,
                    startAngle,
                    sweepAngle,
                    false
                )
                path.close()

                canvas.save()
                canvas.clipPath(path)
                canvas.drawBitmap(
                    bitmap,
                    null,
                    RectF(cx - radius, cy - radius, cx + radius, cy + radius),
                    paint
                )
                canvas.restore()

                // Draw the white space
                if (spaceAngle > 0) {
                    path.reset()
                    path.moveTo(cx, cy)
                    path.arcTo(
                        cx - radius,
                        cy - radius,
                        cx + radius,
                        cy + radius,
                        startAngle + sweepAngle,
                        spaceAngle,
                        false
                    )
                    path.close()

                    canvas.save()
                    canvas.clipPath(path)
                    canvas.drawPath(path, whitePaint)
                    canvas.restore()
                }
            }
        }

        canvas.restore()
    }
}
