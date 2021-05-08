package com.angcyo.drawable.loading

import android.graphics.*
import androidx.core.graphics.toRectF
import com.angcyo.drawable.base.BaseSectionDrawable
import com.angcyo.library.ex.dp

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/05/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class PathLoadingDrawable : BaseSectionDrawable() {

    val path = Path()
    val pathMeasure = PathMeasure()
    val _drawPath = Path()

    var radius: Float = 40f

    var strokeWidth = 3 * dp

    init {
        sections = floatArrayOf(0.5f, 0.5f)

        textPaint.style = Paint.Style.STROKE
        textPaint.strokeWidth = strokeWidth
        textPaint.color = Color.RED
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)

        bounds?.let {
            path.reset()
            val rectF = it.toRectF().apply {
                inset(strokeWidth / 2, strokeWidth / 2)
            }
            //path.addRoundRect(rectF, radius, radius, Path.Direction.CW)
            //path.addRoundRect(rectF, radius, radius, Path.Direction.CW)
            //path.close()

            path.moveTo(rectF.left, rectF.bottom)
            path.lineTo(rectF.left, rectF.top)
            path.lineTo(rectF.top, rectF.right)
            path.lineTo(rectF.right, rectF.bottom)
            path.lineTo(rectF.bottom, rectF.left)
            path.lineTo(rectF.left, rectF.top)
            path.lineTo(rectF.top, rectF.right)
            path.lineTo(rectF.right, rectF.bottom)
            path.lineTo(rectF.bottom, rectF.left)
            path.close()
            pathMeasure.setPath(path, false)
        }
    }

    override fun onDrawProgressSection(
        canvas: Canvas,
        index: Int,
        startProgress: Float,
        endProgress: Float,
        totalProgress: Float,
        sectionProgress: Float
    ) {
        super.onDrawProgressSection(
            canvas,
            index,
            startProgress,
            endProgress,
            totalProgress,
            sectionProgress
        )

        if (index == 0) {
            /*pathMeasure.getSegment(
                sectionProgress * pathMeasure.length,
                (sectionProgress + sectionProgress * 2) * pathMeasure.length,
                _drawPath,
                true
            )*/
            pathMeasure.getSegment(
                0.2f * pathMeasure.length,
                0.7f * pathMeasure.length,
                _drawPath,
                true
            )
            canvas.drawPath(_drawPath, textPaint)
        }
    }

    override fun onDrawSection(
        canvas: Canvas,
        maxSection: Int,
        index: Int,
        totalProgress: Float,
        progress: Float
    ) {
        super.onDrawSection(canvas, maxSection, index, totalProgress, progress)
//        canvas.drawPath(path, textPaint)
    }
}