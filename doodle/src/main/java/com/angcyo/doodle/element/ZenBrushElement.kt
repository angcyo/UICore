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
 * 毛笔绘制元素, 使用贝塞尔曲线绘制
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
        val path = brushPath ?: return
        BaseBrush.computeLastPointSpeed(pointList)
        val zenPoint = ZenPoint(point)
        zenPointList.add(zenPoint)

        if (point.isFirst) {
            /*path.addCircle(
                point.eventX,
                point.eventY,
                selectPaintWidth(point.speed),
                Path.Direction.CW
            )*/
        } else {
            val zenBefore = zenPointList.before(zenPoint)!!
            val before = pointList.before(point)!! //前一个点

            val upAngle = 360f - (90 - point.angle)
            val downAngle = 90 + point.angle

            //计算当前点, 根据速度的不一样, 圆上2个点的坐标
            fillZenPoint(zenPoint, point.speed, upAngle, downAngle, point.eventX, point.eventY)

            val centerPoint = TouchPoint()
            centerPoint.angle = point.angle
            centerPoint.speed = (before.speed + point.speed) / 2
            centerPoint.eventX = (before.eventX + point.eventX) / 2
            centerPoint.eventY = (before.eventY + point.eventY) / 2
            val centerZenPoint = ZenPoint(centerPoint)
            zenPoint.centerZenPoint = centerZenPoint
            fillZenPoint(
                centerZenPoint,
                centerPoint.speed,
                upAngle,
                downAngle,
                centerPoint.eventX,
                centerPoint.eventY
            )

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

                path.addCircle(
                    centerPoint.eventX,
                    centerPoint.eventY,
                    selectPaintWidth(centerPoint.speed),
                    Path.Direction.CW
                )
            }

            zenBefore.centerZenPoint?.let { beforeCenterZenPoint ->
                path.moveTo(beforeCenterZenPoint.downPointX, beforeCenterZenPoint.downPointY)
                path.lineTo(beforeCenterZenPoint.upPointX, beforeCenterZenPoint.upPointY)
                /*var x = (beforeCenterZenPoint.downPointX + beforeCenterZenPoint.upPointX) / 2
                var y = (beforeCenterZenPoint.downPointY + beforeCenterZenPoint.upPointY) / 2
                path.bezier(x, y, beforeCenterZenPoint.upPointX, beforeCenterZenPoint.upPointY)*/

                path.bezier(
                    zenBefore.upPointX,
                    zenBefore.upPointY,
                    centerZenPoint.upPointX,
                    centerZenPoint.upPointY,
                )

                /*x = (centerZenPoint.downPointX + centerZenPoint.upPointX) / 2
                y = (centerZenPoint.downPointY + centerZenPoint.upPointY) / 2
                path.bezier(x, y, centerZenPoint.downPointX, centerZenPoint.downPointY)*/

                path.lineTo(centerZenPoint.downPointX, centerZenPoint.downPointY)

                path.bezier(
                    zenBefore.downPointX,
                    zenBefore.downPointY,
                    beforeCenterZenPoint.downPointX,
                    beforeCenterZenPoint.downPointY,
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