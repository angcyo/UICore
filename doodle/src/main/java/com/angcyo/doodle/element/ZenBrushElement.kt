package com.angcyo.doodle.element

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
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
class ZenBrushElement(brushElementData: BrushElementData) : BaseBrushElement(brushElementData) {

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
        val zenPoint = ZenPoint(point)
        zenPointList.add(zenPoint)

        if (point.isFirst) {
            brushPath?.addCircle(
                point.eventX,
                point.eventY,
                selectPaintWidth(point.speed),
                Path.Direction.CW
            )
        } else {
            val zenBefore = zenPointList.before(zenPoint)!!
            if (point.isLast) {
                //最后一个点
                brushPath?.addCircle(
                    point.eventX,
                    point.eventY,
                    zenBefore.paintWidth,
                    Path.Direction.CW
                )
                return
            }

            val before = pointList.before(point)!! //前一个点

            val upAngle = 360f - (90 - point.angle)
            val downAngle = 90 + point.angle

            //计算当前点, 根据速度的不一样, 圆上2个点的坐标
            fillZenPoint(zenPoint, point.speed, upAngle, downAngle, point.eventX, point.eventY)

            if (pointList.size() == 2) {
                //计算前一个点, 根据速度的不一样, 圆上2个点的坐标
                fillZenPoint(
                    zenBefore,
                    before.speed,
                    upAngle,
                    downAngle,
                    before.eventX,
                    before.eventY
                )
            }

            val centerZenPoint = ZenPoint(point)
            fillZenPoint(
                centerZenPoint,
                (before.speed + point.speed) / 2,
                upAngle,
                downAngle,
                (before.eventX + point.eventX) / 2,
                (before.eventY + point.eventY) / 2
            )

            brushPath?.apply {
                moveTo(zenBefore.downPointX, zenBefore.downPointY)
                lineTo(zenBefore.upPointX, zenBefore.upPointY)

                bezier(
                    centerZenPoint.upPointX,
                    centerZenPoint.upPointY,
                    zenPoint.upPointX,
                    zenPoint.upPointY
                )

                lineTo(zenPoint.downPointX, zenPoint.downPointY)

                bezier(
                    centerZenPoint.downPointX,
                    centerZenPoint.downPointY,
                    zenBefore.downPointX,
                    zenBefore.downPointY
                )
            }
        }
    }

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        brushPath?.let {
            paint.color = brushElementData.paintColor
            paint.strokeWidth = 1f//brushElementData.paintWidth
            paint.style = Paint.Style.FILL
            canvas.drawPath(it, paint)
        }
    }

    /**根据滑动速度, 返回应该绘制的宽度.
     * 速度越快, 宽度越细
     * */
    fun selectPaintWidth(speed: Float): Float {
        val minSpeed = 0f
        val maxSpeed = 10f
        val currentSpeed = clamp(speed, minSpeed, maxSpeed)

        val speedRatio = (currentSpeed - minSpeed) / (maxSpeed - minSpeed)

        val minWidth = 4
        val maxWidth = brushElementData.paintWidth

        return minWidth + (1 - speedRatio) * (maxWidth - minWidth)
    }

    /**在指定原点, 根据速度的不一样, 圆上2个点的坐标*/
    fun fillZenPoint(
        zenPoint: ZenPoint,
        speed: Float,
        upAngle: Float,
        downAngle: Float,
        pivotX: Float,
        pivotY: Float
    ) {
        zenPoint.paintWidth = selectPaintWidth(speed)
        dotDegrees(
            zenPoint.paintWidth,
            upAngle,
            pivotX,
            pivotY
        ).apply {
            zenPoint.upPointX = x
            zenPoint.upPointY = y
        }
        dotDegrees(
            zenPoint.paintWidth,
            downAngle,
            pivotX,
            pivotY
        ).apply {
            zenPoint.downPointX = x
            zenPoint.downPointY = y
        }
    }
}