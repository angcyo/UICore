package com.angcyo.drawable.loading

import android.graphics.Canvas
import android.graphics.Paint
import com.angcyo.library.ex.toColorInt
import kotlin.math.min

/**
 * 模仿Telegram 加载动画
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/16
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class TGStrokeLoadingDrawable : BaseTGLoadingDrawable() {

    /**背景的宽度*/
    var bgStrokeWidth: Float = 0f

    init {
        loadingBgColor = "#10000000".toColorInt()
        paint.style = Paint.Style.STROKE
        bgStrokeWidth = loadingWidth
    }

    override fun draw(canvas: Canvas) {
        val x = bounds.centerX().toFloat()
        val y = bounds.centerY().toFloat()
        val r = min(bounds.width(), bounds.height()) / 2f

        _loadingRectF.set(x - r, y - r, x + r, y + r)
        val inset = loadingWidth / 2
        _loadingRectF.inset(inset, inset)

        //绘制背景
        paint.strokeWidth = bgStrokeWidth
        paint.color = loadingBgColor
        canvas.drawArc(
            _loadingRectF.left,
            _loadingRectF.top,
            _loadingRectF.right,
            _loadingRectF.bottom,
            _angle,
            360f,
            false,
            paint
        )

        //绘制进度
        paint.color = loadingColor
        val sweepAngle = if (isIndeterminate) {
            indeterminateSweepAngle
        } else {
            progress / 100f * 360
        }
        paint.strokeWidth = loadingWidth
        _loadingRectF.inset(loadingOffset, loadingOffset)
        canvas.drawArc(
            _loadingRectF.left,
            _loadingRectF.top,
            _loadingRectF.right,
            _loadingRectF.bottom,
            _angle,
            sweepAngle,
            false,
            paint
        )

        doAngle()
    }

}