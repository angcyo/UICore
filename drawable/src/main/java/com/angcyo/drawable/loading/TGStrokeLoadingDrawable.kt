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

    init {
        loadingBgColor = "#20FFFFFF".toColorInt()
        paint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        val x = bounds.centerX().toFloat()
        val y = bounds.centerY().toFloat()
        val r = min(bounds.width(), bounds.height()) / 2f

        _loadingRectF.set(x - r, y - r, x + r, y + r)
        val inset = loadingWidth / 2
        _loadingRectF.inset(inset, inset)

        //绘制背景
        paint.strokeWidth = loadingWidth
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