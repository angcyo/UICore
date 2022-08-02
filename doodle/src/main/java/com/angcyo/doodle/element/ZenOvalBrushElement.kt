package com.angcyo.doodle.element

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.angcyo.doodle.brush.BaseBrush
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.data.ZenPoint
import com.angcyo.doodle.layer.BaseLayer
import com.angcyo.library.ex.before
import com.angcyo.library.ex.clamp

/**
 * 毛笔绘制元素, 使用椭圆绘制
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022-8-1
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class ZenOvalBrushElement(brushElementData: BrushElementData) : BaseBrushElement(brushElementData) {

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
        if (point.isLast) {
            return
        }
        brushPath?.apply {
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

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        brushPath?.let {
            paint.color = brushElementData.paintColor
            paint.strokeWidth = 1f//brushElementData.paintWidth
            paint.style = Paint.Style.FILL
            paint.strokeMiter = 10f
            canvas.drawPath(it, paint)
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