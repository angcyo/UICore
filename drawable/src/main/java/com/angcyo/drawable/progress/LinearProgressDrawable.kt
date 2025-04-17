package com.angcyo.drawable.progress

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
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

    /**固定进度的宽度*/
    var fixedWidth: Boolean = true

    /**是否绘制进度*/
    var drawProgress: Boolean = true

    /**是否自动检查进度的值, 如果超出范围自动控制[drawProgress]不绘制进度*/
    var checkDrawProgress: Boolean = false

    /**进度固定的宽度*/
    var progressFixedWidth: Float = 50f

    init {
        //滑块颜色渐变
        progressGradientColors =
            intArrayOf(Color.parseColor("#FFD666"), Color.parseColor("#FFD666"))
        //背景颜色渐变
        backgroundGradientColors =
            intArrayOf(Color.parseColor("#B0B8CB"), Color.parseColor("#B0B8CB"))
        //当前进度
        currentProgressValue = 50
        //背景的高度
        backgroundWidth
        //滑块的高度
        progressWidth
    }

    override fun invalidateSelf() {
        super.invalidateSelf()
        if (checkDrawProgress) {
            drawProgress = currentProgressValue in minProgressValue..maxProgressValue
        }
    }

    override fun validProgressValue(progressValue: Int): Int {
        if (checkDrawProgress) {
            return progressValue
        }
        return super.validProgressValue(progressValue)
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        val rect = drawRect
        _backgroundShader = if (backgroundGradientColors != null) LinearGradient(
            rect.left.toFloat(),
            0f,
            rect.right.toFloat(),
            0f,
            backgroundGradientColors!!,
            null,
            Shader.TileMode.REPEAT
        ) else null
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

        if (backgroundGradientColors != null) {
            textPaint.shader = _backgroundShader
            canvas.drawRoundRect(
                _tempRect.left,
                _tempRect.top,
                _tempRect.right,
                _tempRect.bottom,
                roundRadius, roundRadius, textPaint
            )
        }

        //进度绘制
        if (drawProgress) {
            _tempRect.top = rect.centerY() - progressWidth / 2
            _tempRect.bottom = rect.centerY() + progressWidth / 2
            if (fixedWidth) {
                textPaint.shader = LinearGradient(
                    _tempRect.left,
                    0f,
                    _tempRect.right,
                    0f,
                    progressGradientColors,
                    null,
                    Shader.TileMode.REPEAT
                )
                _tempRect.left = rect.left + (rect.width() - progressFixedWidth) * progressRatio
                _tempRect.right = _tempRect.left + progressFixedWidth
            } else {
                _tempRect.right = rect.left + rect.width() * progressRatio
                textPaint.shader = _progressShader
            }
            canvas.drawRoundRect(
                _tempRect.left,
                _tempRect.top,
                _tempRect.right,
                _tempRect.bottom,
                roundRadius, roundRadius, textPaint
            )
        }
    }

}