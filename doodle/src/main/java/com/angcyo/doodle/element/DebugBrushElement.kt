package com.angcyo.doodle.element

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.layer.BaseLayer
import com.angcyo.library.L
import com.angcyo.library.ex.textHeight
import com.angcyo.library.ex.textWidth

/**
 * 调试画笔绘制
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/30
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DebugBrushElement(brushElementData: BrushElementData) : BaseBrushElement(brushElementData) {

    override fun onUpdateElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>,
        point: TouchPoint
    ) {
        super.onUpdateElement(manager, pointList, point)
        brushPath?.apply {
            addCircle(point.eventX, point.eventY, brushElementData.paintWidth, Path.Direction.CW)
        }
    }

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        super.onDraw(layer, canvas)
        paint.strokeWidth = 1f
        paint.color = Color.RED

        //path
        brushPath?.apply {
            paint.style = Paint.Style.STROKE
            canvas.drawPath(this, paint)
        }

        //调试信息
        paint.style = Paint.Style.FILL
        brushElementData.pointList.forEach { point ->
            val text = "${point.angle}/${point.speed}"
            val cx = point.eventX - paint.textWidth(text) / 2
            val cy = point.eventY - paint.textHeight()
            canvas.drawText(text, cx, cy, paint)

            //
            L.i(text)
        }
    }
}