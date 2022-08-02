package com.angcyo.doodle.brush

import android.graphics.Path
import android.graphics.RectF
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.data.ZenPoint
import com.angcyo.doodle.element.BaseBrushElement
import com.angcyo.doodle.element.ZenOvalBrushElement
import com.angcyo.library.ex.before

/**
 * 毛笔笔刷手势数据收集
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022-8-1
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class ZenOvalBrush : BaseBrush() {

    val zenPointList = mutableListOf<ZenPoint>()

    override fun onCreateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>
    ): BaseBrushElement {
        zenPointList.clear()
        return ZenOvalBrushElement(BrushElementData())
    }

    override fun onUpdateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>,
        point: TouchPoint
    ) {
        if (point.isLast) {
            return
        }
        brushElement?.brushElementData?.brushPath?.apply {
            BaseBrush.computeLastPointSpeed(pointList)
            val zenPoint = ZenPoint(point)
            zenPointList.add(zenPoint)
            if (point.isFirst) {
                val width = selectPaintWidth(point.speed)
                fillOvalRect(zenPoint.ovalRect, width, point.eventX, point.eventY)
                addOval(zenPoint.ovalRect, Path.Direction.CW)
            } else {
                //从上一个点, 移动到下一个点
                val before = pointList.before(point)!! //前一个点

                val distance = if (!point.isLast && point.speed < before.speed) {
                    val width = selectPaintWidth(before.speed)
                    //相对于上一个速度, 在减速, 则加上惯性
                    point.distance + (before.speed - point.speed) * width
                } else {
                    point.distance
                }

                var current = 0f
                val step = 1f //步长
                while (current <= distance) {
                    val ratio = current / distance
                    val px = before.eventX + (point.eventX - before.eventX) * ratio
                    val py = before.eventY + (point.eventY - before.eventY) * ratio
                    val speed = before.speed + (point.speed - before.speed) * ratio
                    val width = selectPaintWidth(speed)
                    fillOvalRect(zenPoint.ovalRect, width, px, py)
                    addOval(zenPoint.ovalRect, Path.Direction.CW)

                    if (current == distance) {
                        break
                    } else {
                        current += step
                        if (current > distance) {
                            current = distance
                        }
                    }
                }
            }
        }
    }

    /**填充椭圆信息*/
    fun fillOvalRect(rect: RectF, width: Float, px: Float, py: Float) {
        val w = width / 2

        val l = px - w / 2f
        val t = py - w
        val r = px + w / 2f
        val b = py + w

        rect.set(l, t, r, b)
    }

}