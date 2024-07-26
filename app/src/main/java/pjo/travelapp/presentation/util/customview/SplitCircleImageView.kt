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
        val angleStep = 360f / count
        val cx = width / 2f
        val cy = height / 2f
        val radius = width.coerceAtMost(height) / 2f

        bitmaps.forEachIndexed { index, bitmap ->
            val startAngle = index * angleStep + spaceAngle / 2
            val sweepAngle = angleStep - spaceAngle

            // Draw the white space
            path.reset()
            path.moveTo(cx, cy)
            path.arcTo(
                cx - radius,
                cy - radius,
                cx + radius,
                cy + radius,
                index * angleStep,
                spaceAngle,
                false
            )
            path.close()

            canvas.save()
            canvas.clipPath(path)
            canvas.drawPath(path, whitePaint)
            canvas.restore()

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
        }
    }
}
