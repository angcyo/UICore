package com.angcyo.drawable.loading

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.angcyo.drawable.base.BaseSectionDrawable
import com.angcyo.library.ex.dp

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/02
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class ArcLoadingDrawable : BaseSectionDrawable() {

    /**
     * 线宽
     */
    var strokeWidth = 3 * dp

    /**
     * 2个扇形, 间隙角度
     */
    var spaceAngle = 25f

    /**
     * 开始的绘制角度
     */
    var startAngle = 0f

    //动画控制的角度
    var animAngle = 0f

    var arcColor: Int = Color.RED

    init {
        sections = floatArrayOf(0.2f, 0.3f, 0.3f, 0.2f)
    }

    override fun onDrawBefore(
        canvas: Canvas,
        maxSection: Int,
        totalProgress: Float
    ) {
        textPaint.alpha = 255
    }

    override fun onDrawProgressSection(
        canvas: Canvas, index: Int,
        startProgress: Float, endProgress: Float,
        totalProgress: Float, sectionProgress: Float
    ) {
        drawRectF.set(bounds)
        drawRectF.inset((strokeWidth / 2), (strokeWidth / 2))

        //两个点, 分散效果
        if (index == 0) {
            textPaint.color = arcColor
            textPaint.style = Paint.Style.FILL
            textPaint.strokeWidth = 0f
            textPaint.strokeCap = Paint.Cap.ROUND
            val offset: Float = drawRectF.width() / 2 * sectionProgress
            canvas.drawCircle(
                drawRectF.centerX() - offset,
                drawRectF.centerY(),
                (strokeWidth / 2),
                textPaint
            )
            canvas.drawCircle(
                drawRectF.centerX() + offset,
                drawRectF.centerY(),
                (strokeWidth / 2),
                textPaint
            )
        } else if (index == 3) { //往里缩的效果
            textPaint.color = arcColor
            textPaint.style = Paint.Style.STROKE
            textPaint.strokeWidth = strokeWidth * (1 - sectionProgress)
            textPaint.strokeCap = Paint.Cap.ROUND
            textPaint.alpha = (255 * (1 - sectionProgress)).toInt()
            val inset: Float = bounds.width() * sectionProgress
            drawRectF.inset(inset, inset)
            animAngle = sectionProgress * 360
            val startDrawAngle = animAngle + 130
            val sweepAngle = 20f
            val ratio = 1 - sectionProgress
            canvas.drawArc(
                drawRectF,
                startDrawAngle, sweepAngle * ratio, false, textPaint
            )
            canvas.drawArc(
                drawRectF,
                startDrawAngle + 180, sweepAngle * ratio, false, textPaint
            )
        } else {
            animAngle = sectionProgress * 180
            textPaint.color = arcColor
            textPaint.style = Paint.Style.STROKE
            textPaint.strokeWidth = strokeWidth
            textPaint.strokeCap = Paint.Cap.ROUND
            var startDrawAngle = animAngle - startAngle
            val sweepAngle = (360 - 2 * spaceAngle) / 2
            var ratio = 1f
            if (index == 1) { //由小变大
                ratio = sectionProgress
            } else if (index == 2) { //由大到小
                startDrawAngle += sweepAngle * sectionProgress
                ratio = 1 - sectionProgress
            }
            canvas.drawArc(
                drawRectF,
                startDrawAngle, sweepAngle * ratio, false, textPaint
            )
            canvas.drawArc(
                drawRectF,
                startDrawAngle + spaceAngle + sweepAngle, sweepAngle * ratio, false, textPaint
            )
        }
    }
}