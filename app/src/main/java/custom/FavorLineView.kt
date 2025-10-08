// 文件路径：app/src/main/java/com/example/nativechatdemo/custom/FavorLineView.kt
package com.example.nativechatdemo.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.nativechatdemo.data.model.FavorPoint
import kotlin.math.abs

class FavorLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val points = mutableListOf<FavorPoint>()

    companion object {
        private const val MAX_BARS = 45
        private const val BAR_WIDTH_DP = 20f
        private const val BAR_GAP_DP = 4f
        private const val CLICK_THRESHOLD = 10f
    }

    private val barWidth: Float
    private val barGap: Float
    private val totalWidth: Float

    private var downX = 0f
    private var downY = 0f
    private var isSwiping = false

    init {
        val density = context.resources.displayMetrics.density
        barWidth = BAR_WIDTH_DP * density
        barGap = BAR_GAP_DP * density
        totalWidth = MAX_BARS * (barWidth + barGap)
    }

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F5F5F5")
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#999999")
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    var onPointClickListener: ((FavorPoint) -> Unit)? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = totalWidth.toInt()
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        if (points.isEmpty()) {
            canvas.drawText(
                "开始对话，好感度将显示在此",
                width / 2f,
                height / 2f,
                textPaint
            )
            return
        }

        points.forEachIndexed { index, point ->
            if (index >= MAX_BARS) return@forEachIndexed
            drawBar(canvas, index, point)
        }
    }

    private fun drawBar(canvas: Canvas, index: Int, point: FavorPoint) {
        val x = index * (barWidth + barGap)
        val barHeight = (point.favor / 100f) * height
        val top = height - barHeight

        barPaint.color = getBarColor(point.favor)

        canvas.drawRect(
            x,
            top,
            x + barWidth,
            height.toFloat(),
            barPaint
        )

        // 如果有原因，根据好感度变化绘制不同的心形
        if (point.reason.isNotEmpty()) {
            val heartCenterX = x + barWidth / 2
            val heartCenterY = 30f

            if (point.favorChange < 0) {
                // 负变化：绘制裂心💔
                drawBrokenHeart(canvas, heartCenterX, heartCenterY, 36f)
                Log.d("FavorLineView", "绘制裂心: Bar $index, change=${point.favorChange}")
            } else {
                // 正变化：绘制完整红心❤️
                drawHeart(canvas, heartCenterX, heartCenterY, 36f)
                Log.d("FavorLineView", "绘制红心: Bar $index, change=${point.favorChange}")
            }
        }
    }

    /**
     * 绘制完整的红心❤️
     */
    private fun drawHeart(canvas: Canvas, centerX: Float, centerY: Float, size: Float) {
        val path = Path()

        val top = centerY - size * 0.3f

        path.moveTo(centerX, top)

        // 左半边
        path.cubicTo(
            centerX - size * 0.5f, top - size * 0.4f,
            centerX - size, top + size * 0.3f,
            centerX, top + size
        )

        // 右半边
        path.cubicTo(
            centerX + size, top + size * 0.3f,
            centerX + size * 0.5f, top - size * 0.4f,
            centerX, top
        )

        path.close()

        // 红色填充
        val heartPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            style = Paint.Style.FILL
        }
        canvas.drawPath(path, heartPaint)

        // 白色边框
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawPath(path, borderPaint)
    }

    /**
     * 绘制裂开的心💔
     */
    private fun drawBrokenHeart(canvas: Canvas, centerX: Float, centerY: Float, size: Float) {
        val top = centerY - size * 0.3f

        // 左半边心形
        val leftPath = Path()
        leftPath.moveTo(centerX, top)
        leftPath.cubicTo(
            centerX - size * 0.5f, top - size * 0.4f,
            centerX - size, top + size * 0.3f,
            centerX, top + size
        )
        // 裂缝的左边缘（锯齿状）
        leftPath.lineTo(centerX - size * 0.1f, top + size * 0.6f)
        leftPath.lineTo(centerX + size * 0.05f, top + size * 0.4f)
        leftPath.lineTo(centerX - size * 0.05f, top + size * 0.2f)
        leftPath.lineTo(centerX, top)
        leftPath.close()

        // 右半边心形
        val rightPath = Path()
        rightPath.moveTo(centerX, top)
        rightPath.lineTo(centerX + size * 0.05f, top + size * 0.2f)
        rightPath.lineTo(centerX - size * 0.05f, top + size * 0.4f)
        rightPath.lineTo(centerX + size * 0.1f, top + size * 0.6f)
        rightPath.lineTo(centerX, top + size)
        rightPath.cubicTo(
            centerX + size, top + size * 0.3f,
            centerX + size * 0.5f, top - size * 0.4f,
            centerX, top
        )
        rightPath.close()

        // 深红色填充
        val brokenHeartPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CC0000")
            style = Paint.Style.FILL
        }

        canvas.drawPath(leftPath, brokenHeartPaint)
        canvas.drawPath(rightPath, brokenHeartPaint)

        // 白色边框
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawPath(leftPath, borderPaint)
        canvas.drawPath(rightPath, borderPaint)

        // 绘制裂缝（黑色细线）
        val crackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }

        val crackPath = Path()
        crackPath.moveTo(centerX, top)
        crackPath.lineTo(centerX - size * 0.05f, top + size * 0.2f)
        crackPath.lineTo(centerX + size * 0.05f, top + size * 0.4f)
        crackPath.lineTo(centerX - size * 0.1f, top + size * 0.6f)
        crackPath.lineTo(centerX, top + size)

        canvas.drawPath(crackPath, crackPaint)
    }

    private fun getBarColor(favor: Int): Int {
        return when {
            favor >= 80 -> Color.parseColor("#FF1493")
            favor >= 60 -> Color.parseColor("#FF69B4")
            favor >= 40 -> Color.parseColor("#FFB6C1")
            favor >= 20 -> Color.parseColor("#FFC0CB")
            else -> Color.parseColor("#FFE4E1")
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                isSwiping = false
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = abs(event.x - downX)
                val deltaY = abs(event.y - downY)

                if (deltaX > CLICK_THRESHOLD || deltaY > CLICK_THRESHOLD) {
                    isSwiping = true
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (!isSwiping) {
                    val clickedPoint = findClickedBar(event.x)
                    clickedPoint?.let {
                        onPointClickListener?.invoke(it)
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findClickedBar(x: Float): FavorPoint? {
        if (points.isEmpty()) return null

        points.forEachIndexed { index, point ->
            if (index >= MAX_BARS) return@forEachIndexed

            val barLeft = index * (barWidth + barGap)
            val barRight = barLeft + barWidth

            if (x >= barLeft && x <= barRight) {
                return point
            }
        }

        return null
    }

    fun updatePoints(newPoints: List<FavorPoint>) {
        points.clear()
        points.addAll(newPoints)
        invalidate()
    }
}