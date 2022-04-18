package com.angcyo.drawable.loading

import android.graphics.Canvas
import android.graphics.Paint
import com.angcyo.library.ex.dp
import kotlin.math.min

/**
 * 模仿Telegram 加载动画
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/16
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class TGSolidLoadingDrawable : BaseTGLoadingDrawable() {

    init {
        loadingOffset = 2 * dp
    }

    override fun draw(canvas: Canvas) {
        val x = bounds.centerX().toFloat()
        val y = bounds.centerY().toFloat()
        val r = min(bounds.width(), bounds.height()) / 2f

        //绘制背景
        paint.color = loadingBgColor
        paint.style = Paint.Style.FILL
        canvas.drawCircle(x, y, r, paint)

        //绘制进度
        _loadingRectF.set(x - r, y - r, x + r, y + r)
        val inset = loadingOffset + loadingWidth / 2
        _loadingRectF.inset(inset, inset)

        paint.color = loadingColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = loadingWidth
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