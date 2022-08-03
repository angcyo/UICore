package com.angcyo.drawable.progress

import android.graphics.*
import androidx.annotation.Px

/**
 * 线性进度指示
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/03
 */
class LinearProgressDrawable : BaseValueProgressDrawable() {

    /**圆角半径*/
    @Px
    var roundRadius: Float = 15f

    init {
        progressGradientColors =
            intArrayOf(Color.parseColor("#FFD666"), Color.parseColor("#FFD666"))
        backgroundGradientColors =
            intArrayOf(Color.parseColor("#B0B8CB"), Color.parseColor("#B0B8CB"))
        currentProgressValue = 50
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        val rect = drawRect
        _backgroundShader = LinearGradient(
            rect.left.toFloat(),
            0f,
            rect.right.toFloat(),
            0f,
            backgroundGradientColors,
            null,
            Shader.TileMode.REPEAT
        )
        _progressShader = LinearGradient(
            rect.left.toFloat(),
            0f,
            rect.right.toFloat(),
            0f,
            progressGradientColors,
            null,
            Shader.TileMode.REPEAT
        )
    }

    val _tempRect = RectF()
    override fun draw(canvas: Canvas) {
        textPaint.style = Paint.Style.FILL
        val rect = drawRect
        _tempRect.set(rect)

        //背景绘制
        _tempRect.top = rect.centerY() - backgroundWidth / 2
        _tempRect.bottom = rect.centerY() + backgroundWidth / 2
        textPaint.shader = _backgroundShader
        canvas.drawRoundRect(
            _tempRect.left,
            _tempRect.top,
            _tempRect.right,
            _tempRect.bottom,
            roundRadius, roundRadius, textPaint
        )

        //进度绘制
        _tempRect.top = rect.centerY() - progressWidth / 2
        _tempRect.bottom = rect.centerY() + progressWidth / 2
        _tempRect.right = rect.left + rect.width() * progressRatio
        textPaint.shader = _progressShader
        canvas.drawRoundRect(
            _tempRect.left,
            _tempRect.top,
            _tempRect.right,
            _tempRect.bottom,
            roundRadius, roundRadius, textPaint
        )
    }

}