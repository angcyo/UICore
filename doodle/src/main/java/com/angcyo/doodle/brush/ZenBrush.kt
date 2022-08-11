package com.angcyo.doodle.brush

import android.graphics.Path
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.data.ZenPoint
import com.angcyo.doodle.element.BaseBrushElement
import com.angcyo.doodle.element.ZenBrushElement
import com.angcyo.library.ex.before
import com.angcyo.library.ex.bezier
import com.angcyo.library.ex.dotDegrees
import com.angcyo.library.ex.size

/**
 * 毛笔笔刷手势数据收集
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/26
 * Copyright (c) 2020 angcyo. All rights reserved.
 */

@Deprecated("效果差, 首尾会有割裂")
class ZenBrush : BaseBrush() {

    val zenPointList = mutableListOf<ZenPoint>()

    override fun onCreateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>
    ): BaseBrushElement {
        zenPointList.clear()
        return ZenBrushElement(BrushElementData())
    }

    override fun onUpdateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>,
        point: TouchPoint
    ) {
        val path = brushElement?.brushElementData?.brushPath ?: return
        computeLastPointSpeed(pointList)
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