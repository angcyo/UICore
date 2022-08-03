package com.angcyo.drawable.progress

import android.graphics.*
import androidx.core.graphics.withRotation
import kotlin.math.min

/**
 * 圆环进度指示
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/03
 */
class CircleProgressDrawable : BaseValueProgressDrawable() {

    /**开始绘制时, 需要旋转的角度*/
    var startDegrees: Float = -90f

    /**进度偏移绘制的角度*/
    var startOffsetAngle: Float = 0f

    init {
        progressGradientColors =
            intArrayOf(Color.parseColor("#79B2FF"), Color.parseColor("#437AFF"))
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
        _progressShader = SweepGradient(
            rect.centerX().toFloat(),
            rect.centerY().toFloat(),
            progressGradientColors,
            null
        )
    }

    val _tempRect = Rect()

    override fun draw(canvas: Canvas) {
        val rect = drawRect
        val cx = rect.centerX().toFloat()
        val cy = rect.centerY().toFloat()
        canvas.withRotation(startDegrees, cx, cy) {
            textPaint.style = Paint.Style.STROKE
            val radius = min(rect.width(), rect.height()) / 2f

            //背景绘制
            textPaint.strokeWidth = backgroundWidth
            textPaint.shader = _backgroundShader
            canvas.drawCircle(
                rect.centerX().toFloat(),
                rect.centerY().toFloat(),
                radius - backgroundWidth / 2 - (progressWidth - backgroundWidth) / 2,
                textPaint
            )

            //进度绘制
            textPaint.style = Paint.Style.STROKE
            textPaint.strokeWidth = progressWidth
            textPaint.shader = _progressShader
            _tempRect.set(rect)
            val inset = (progressWidth / 2).toInt()
            _tempRect.inset(inset, inset)
            canvas.drawArc(
                _tempRect.left.toFloat(),
                _tempRect.top.toFloat(),
                _tempRect.right.toFloat(),
                _tempRect.bottom.toFloat(),
                startOffsetAngle,
                360f * progressRatio,
                false,
                textPaint
            )
        }
    }
}