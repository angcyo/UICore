package com.angcyo.doodle.element

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.angcyo.doodle.brush.BaseBrush
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

    val debugPointList = mutableListOf<DebugPoint>()

    override fun onCreateElement(manager: DoodleTouchManager, pointList: List<TouchPoint>) {
        super.onCreateElement(manager, pointList)
        debugPointList.clear()
    }

    override fun onUpdateElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>,
        point: TouchPoint
    ) {
        super.onUpdateElement(manager, pointList, point)
        BaseBrush.computeLastPointSpeed(pointList)
        brushPath?.apply {
            addCircle(point.eventX, point.eventY, brushElementData.paintWidth, Path.Direction.CW)
        }
        debugPointList.add(DebugPoint(point).apply {
            text = "${point.angle}/${point.speed}"
            textDrawX = point.eventX - paint.textWidth(text) / 2
            textDrawY = point.eventY - paint.textHeight()
        })
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
        debugPointList.forEach { point ->
            canvas.drawText(point.text, point.textDrawX, point.textDrawY, paint)
            //
            L.i(point.text)
        }
    }

    data class DebugPoint(
        val point: TouchPoint,
        var text: String = "",
        var textDrawX: Float = 0f,
        var textDrawY: Float = 0f
    )
}