package com.angcyo.doodle.element

import android.graphics.Canvas
import android.graphics.Paint
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.layer.BaseLayer

/**
 * 毛笔绘制元素, 使用贝塞尔曲线绘制
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/26
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class ZenBrushElement(brushElementData: BrushElementData) : BaseBrushElement(brushElementData) {

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        brushElementData.brushPath?.let {
            paint.color = brushElementData.paintColor
            paint.strokeWidth = 1f//brushElementData.paintWidth
            paint.style = Paint.Style.FILL
            canvas.drawPath(it, paint)
        }
    }
}