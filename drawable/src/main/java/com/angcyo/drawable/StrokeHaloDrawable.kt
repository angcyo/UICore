package com.angcyo.drawable

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.angcyo.drawable.loading.BaseLoadingDrawable
import com.angcyo.library.ex._color
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.evaluateColor
import com.angcyo.library.ex.evaluateRectF

/**
 * 带有光晕的描边, 比如进入直播间按钮动效
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/05/01
 */
class StrokeHaloDrawable : BaseLoadingDrawable() {

    /**绘制的圆角*/
    var drawRound = 45f * dp

    /**光圈开始的颜色*/
    var haloColor: Int = _color(R.color.colorAccent)

    /**光圈结束的颜色*/
    var haloEndColor: Int = Color.TRANSPARENT

    /**留出距离用来绘制动画*/
    var haloPadding: Float = 20 * dp

    init {
        enableReverse = false
        textPaint.style = Paint.Style.STROKE
        textPaint.strokeWidth = 1 * dp
    }

    private val startRect = RectF()
    private val endRect = RectF()

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        startRect.set(
            bounds.left + haloPadding,
            bounds.top + haloPadding,
            bounds.right - haloPadding,
            bounds.bottom - haloPadding
        )
        endRect.set(bounds)
    }

    override fun draw(canvas: Canvas) {
        val fraction = loadingProgress / 100f
        evaluateRectF(fraction, startRect, endRect, drawRectF)
        textPaint.color = haloColor
        canvas.drawRoundRect(startRect, drawRound, drawRound, textPaint)
        textPaint.color = evaluateColor(fraction, haloColor, haloEndColor)
        canvas.drawRoundRect(drawRectF, drawRound, drawRound, textPaint)
        //L.i("loadingProgress:$loadingProgress $fraction")

        //动画
        doLoading()
    }
}