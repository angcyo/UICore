package com.angcyo.doodle.element

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.angcyo.doodle.brush.BaseBrush
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.data.ZenPoint
import com.angcyo.doodle.layer.BaseLayer
import com.angcyo.library.ex.*

/**
 * 毛笔绘制元素
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/26
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class ZenElement(brushElementData: BrushElementData) : BaseBrushElement(brushElementData) {

    val zenPointList = mutableListOf<ZenPoint>()

    override fun onCreateElement(manager: DoodleTouchManager, pointList: List<TouchPoint>) {
        //super.onCreateElement(manager, pointList)
        zenPointList.clear()
    }

    override fun onUpdateElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>,
        point: TouchPoint
    ) {
        BaseBrush.computeLastPointSpeed(pointList)
        if (point.speed <= 0f) {
            //没速度, 可能还没开始拖拽
        } else {
            //有速度
            val before = pointList.before(point)//前一个点

            //速度的变化
            val fromSpeed = before!!.speed
            val toSpeed = point.speed


        }
    }

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        brushPath?.let {
            paint.color = brushElementData.paintColor
            paint.strokeWidth = brushElementData.paintWidth
            paint.style = Paint.Style.STROKE
            canvas.drawPath(it, paint)
        }
        if (isDebugType()) {
            paint.textSize = 9 * dp
            paint.style = Paint.Style.FILL
            paint.color = Color.RED
            brushElementData.pointList.forEach { point ->
                val text = "${point.angle}/${point.speed}"
                val cx = point.eventX - paint.textWidth(text) / 2
                val cy = point.eventY - paint.textHeight()
                canvas.drawText(text, cx, cy, paint)
            }
        }
    }

}