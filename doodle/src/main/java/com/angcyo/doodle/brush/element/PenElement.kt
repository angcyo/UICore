package com.angcyo.doodle.brush.element

import android.graphics.Canvas
import android.graphics.Paint
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.layer.BaseLayer

/**
 * 钢笔绘制元素
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class PenElement(val brushElementData: BrushElementData) : BaseBrushElement() {
    
    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        brushPath?.let {
            paint.color = brushElementData.paintColor
            paint.strokeWidth = brushElementData.paintWidth
            paint.style = Paint.Style.STROKE
            canvas.drawPath(it, paint)
        }
    }
}