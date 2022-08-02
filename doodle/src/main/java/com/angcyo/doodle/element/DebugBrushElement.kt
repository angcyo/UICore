package com.angcyo.doodle.element

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.layer.BaseLayer
import com.angcyo.library.L

/**
 * 调试画笔绘制
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/30
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DebugBrushElement(val brushData: DebugBrushElementData) :
    BaseBrushElement(brushData) {

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        super.onDraw(layer, canvas)
        paint.strokeWidth = 1f
        paint.color = Color.RED

        //path
        brushElementData.brushPath?.apply {
            paint.style = Paint.Style.STROKE
            canvas.drawPath(this, paint)
        }

        //调试信息
        paint.style = Paint.Style.FILL
        brushData.debugPointList.forEach { point ->
            canvas.drawText(point.text, point.textDrawX, point.textDrawY, paint)
            //
            L.i(point.text)
        }
    }

    class DebugBrushElementData : BrushElementData() {
        val debugPointList = mutableListOf<DebugPoint>()
    }

    data class DebugPoint(
        val point: TouchPoint,
        var text: String = "",
        var textDrawX: Float = 0f,
        var textDrawY: Float = 0f
    )
}