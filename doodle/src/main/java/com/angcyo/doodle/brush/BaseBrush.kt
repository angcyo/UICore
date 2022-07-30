package com.angcyo.doodle.brush

import android.view.MotionEvent
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.core.ITouchRecognize
import com.angcyo.doodle.core.Strategy
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.data.toTouchPoint
import com.angcyo.doodle.element.BaseBrushElement
import com.angcyo.library.ex.c
import com.angcyo.library.ex.degrees
import kotlin.math.absoluteValue

/**
 * 基础笔刷, 用来收集点位数据
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
abstract class BaseBrush : ITouchRecognize {

    companion object {

        /**计算最后一个点的速度*/
        fun computeLastPointSpeed(pointList: List<TouchPoint>) {
            if (pointList.size > 1) {
                val prevPoint: TouchPoint = pointList[pointList.lastIndex - 1]
                val lastPoint: TouchPoint = pointList[pointList.lastIndex]
                computePointSpeed(lastPoint, prevPoint)
            }
        }

        /**计算点[point]的速度, 相对于[from]*/
        fun computePointSpeed(point: TouchPoint, from: TouchPoint) {
            val s = c(from.eventX, from.eventY, point.eventX, point.eventY)
            val t = (point.timestamp - from.timestamp).absoluteValue
            point.distance = s.toFloat()
            point.speed = (s / t).toFloat()
            point.angle = degrees(point.eventX, point.eventY, from.eventX, from.eventY).toFloat()
        }

        /**计算每个点的速度*/
        fun computePointSpeed(pointList: List<TouchPoint>) {
            var lastPoint: TouchPoint? = null
            for (point in pointList) {
                if (point.isFirst) {
                    point.speed = 0f
                    lastPoint = point
                    continue
                }
                lastPoint?.let {
                    computePointSpeed(point, it)
                }
                lastPoint = point
            }
        }
    }

    /**点位信息*/
    var collectPointList: MutableList<TouchPoint>? = null

    /**创建的绘制[collectPointList]元素*/
    var brushElement: BaseBrushElement? = null

    override fun onTouchRecognize(manager: DoodleTouchManager, event: MotionEvent): Boolean {
        return onCollectPoint(manager, event)
    }

    /**收集点位信息*/
    open fun onCollectPoint(manager: DoodleTouchManager, event: MotionEvent): Boolean {
        var handle = false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                handle = true
                collectPointList = mutableListOf()
                collectPointList?.apply {
                    val touchPoint = event.toTouchPoint().apply {
                        isFirst = true
                    }
                    add(touchPoint)
                    //create
                    brushElement = onCreateBrushElement(manager, this)?.apply {
                        manager.doodleDelegate.doodleConfig.updateToElementData(brushElementData)
                        manager.doodleDelegate.addElement(this, Strategy.Preview())
                    }
                    brushElement?.onCreateElement(manager, this)
                    //update
                    onUpdateBrushElement(manager, this, touchPoint)
                    brushElement?.onUpdateElement(manager, this, touchPoint)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                handle = true
                collectPointList?.apply {
                    val touchPoint = event.toTouchPoint()
                    add(touchPoint)
                    //update
                    onUpdateBrushElement(manager, this, touchPoint)
                    brushElement?.onUpdateElement(manager, this, touchPoint)
                    manager.doodleDelegate.refresh()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handle = true
                collectPointList?.apply {
                    val touchPoint = event.toTouchPoint().apply {
                        isLast = true
                    }
                    add(touchPoint)
                    //update
                    onUpdateBrushElement(manager, this, touchPoint)
                    brushElement?.onUpdateElement(manager, this, touchPoint)
                    //finish
                    if (isNotEmpty()) {
                        onFinishBrushElement(manager, this)
                        brushElement?.onFinishElement(manager, this)
                    }
                    brushElement?.let {
                        manager.doodleDelegate.addElement(it, Strategy.Normal())
                    }
                    brushElement = null
                }
            }
        }

        return handle
    }

    /**开始绘制时, 创建画刷绘制元素*/
    open fun onCreateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>
    ): BaseBrushElement? = null

    /**实时更新画笔元素*/
    open fun onUpdateBrushElement(
        manager: DoodleTouchManager,
        pointList: List<TouchPoint>,
        point: TouchPoint
    ) {

    }

    /**点位收集完成
     * [pointList] 不为空的数据集合*/
    open fun onFinishBrushElement(manager: DoodleTouchManager, pointList: List<TouchPoint>) {

    }
}