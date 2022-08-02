package com.angcyo.doodle.element

import android.graphics.Canvas
import android.graphics.Paint
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.layer.BaseLayer

/**
 * 毛笔绘制元素, 使用椭圆绘制
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022-8-1
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class ZenOvalBrushElement(brushElementData: BrushElementData) : BaseBrushElement(brushElementData) {

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        brushElementData.brushPath?.let {
            paint.color = brushElementData.paintColor
            paint.strokeWidth = 1f//brushElementData.paintWidth
            paint.style = Paint.Style.FILL
            paint.strokeMiter = 10f
            canvas.drawPath(it, paint)
        }
    }
}