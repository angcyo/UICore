package com.angcyo.drawable

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.angcyo.drawable.loading.BaseLoadingDrawable
import com.angcyo.library.ex.alphaRatio
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.linearGradientShader

/**
 * 危险警告提示[Drawable]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/01
 */
class DangerWarningDrawable : BaseLoadingDrawable() {

    /**颜色*/
    var warnColor: Int = Color.RED.alphaRatio(0.4f)

    /**宽度*/
    var size: Int = 20 * dpi

    init {
        textPaint.style = Paint.Style.FILL
        loadingStep = 3f
        if (isInEditMode) {
            loadingProgress = 100f
        }
    }

    val _tempBounds = RectF()

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        //绘制左边的警告
        val s = size * (loadingProgress) / 100f
        _tempBounds.set(
            bounds.left.toFloat(),
            bounds.top.toFloat(),
            bounds.left + s,
            bounds.bottom.toFloat()
        )
        textPaint.shader = linearGradientShader(
            _tempBounds.left,
            0f,
            _tempBounds.right,
            0f,
            intArrayOf(warnColor, Color.TRANSPARENT)
        )
        canvas.drawRect(_tempBounds, textPaint)

        //上
        _tempBounds.set(
            bounds.left.toFloat(),
            bounds.top.toFloat(),
            bounds.right.toFloat(),
            bounds.top + s
        )
        textPaint.shader = linearGradientShader(
            0f,
            _tempBounds.top,
            0f,
            _tempBounds.bottom,
            intArrayOf(warnColor, Color.TRANSPARENT)
        )
        canvas.drawRect(_tempBounds, textPaint)

        //右
        _tempBounds.set(
            bounds.right - s,
            bounds.top.toFloat(),
            bounds.right.toFloat(),
            bounds.bottom.toFloat()
        )
        textPaint.shader = linearGradientShader(
            _tempBounds.right,
            0f,
            _tempBounds.left,
            0f,
            intArrayOf(warnColor, Color.TRANSPARENT)
        )
        canvas.drawRect(_tempBounds, textPaint)

        //下
        _tempBounds.set(
            bounds.left.toFloat(),
            bounds.bottom - s,
            bounds.right.toFloat(),
            bounds.bottom.toFloat()
        )
        textPaint.shader = linearGradientShader(
            0f,
            _tempBounds.bottom,
            0f,
            _tempBounds.top,
            intArrayOf(warnColor, Color.TRANSPARENT)
        )
        canvas.drawRect(_tempBounds, textPaint)

        //动画
        doLoading()
    }
}