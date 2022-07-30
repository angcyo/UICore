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
import com.angcyo.library.L
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
        super.onCreateElement(manager, pointList)
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
            brushPath?.moveTo(point.eventX, point.eventY)
        } else {
            //有速度
            val before = pointList.before(point)//前一个点

            //速度的变化
            val fromSpeed = before!!.speed
            val toSpeed = point.speed

            val angle1 = 360f - (90 - point.angle)
            val angle2 = 90 + point.angle
            val w1 = selectWidth(before.speed)
            val w2 = selectWidth(point.speed)
            val b1 = dotDegrees(
                w1,
                angle1,
                before.eventX,
                before.eventY
            )
            brushPath?.moveTo(b1.x, b1.y)

            val a1 = dotDegrees(
                w2,
                angle1,
                point.eventX,
                point.eventY
            )
            brushPath?.lineTo(a1.x, a1.y)

            val b2 = dotDegrees(
                w1,
                angle2,
                before.eventX,
                before.eventY
            )
            brushPath?.moveTo(b2.x, b2.y)

            val a2 = dotDegrees(
                w2,
                angle2,
                point.eventX,
                point.eventY
            )
            brushPath?.lineTo(a2.x, a2.y)

            /*brushPath?.addCircle(
                point.eventX,
                point.eventY,
                brushElementData.paintWidth,
                Path.Direction.CW
            )*/
        }
    }

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        brushPath?.let {
            paint.color = brushElementData.paintColor
            paint.strokeWidth = 1f//brushElementData.paintWidth
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

                //
                L.i(text)
            }
        }
    }

    /**根据滑动速度, 返回应该绘制的宽度.
     * 速度越快, 宽度越细
     * */
    fun selectWidth(speed: Float): Float {
        val minSpeed = 0f
        val maxSpeed = 20f
        val currentSpeed = clamp(speed, minSpeed, maxSpeed)

        val speedRatio = (currentSpeed - minSpeed) / (maxSpeed - minSpeed)

        val minWidth = 4
        val maxWidth = brushElementData.paintWidth

        return minWidth + (1 - speedRatio) * (maxWidth - minWidth)
    }

}