package com.angcyo.doodle.element

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
open class PenBrushElement(brushElementData: BrushElementData) :
    BaseBrushElement(brushElementData) {

    init {
        paint.style = Paint.Style.STROKE
    }

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        brushElementData.brushPath?.let {
            paint.color = brushElementData.paintColor
            paint.strokeWidth = brushElementData.paintWidth
            canvas.drawPath(it, paint)
        }
    }
}