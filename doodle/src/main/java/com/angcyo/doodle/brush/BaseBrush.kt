package com.angcyo.doodle.brush

import android.view.MotionEvent
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.core.ITouchRecognize
import com.angcyo.doodle.core.Strategy
import com.angcyo.doodle.data.TouchPoint
import com.angcyo.doodle.data.toTouchPoint
import com.angcyo.doodle.element.BaseElement

/**
 * 基础笔刷, 用来收集点位数据
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
abstract class BaseBrush : ITouchRecognize {

    /**点位信息*/
    var collectPointList: MutableList<TouchPoint>? = null

    /**创建的绘制[collectPointList]元素*/
    var brushElement: BaseElement? = null

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
                collectPointList?.add(event.toTouchPoint().apply {
                    isFirst = true
                })
                brushElement = onCreateBrushElement()?.apply {
                    manager.doodleDelegate.addElement(this, Strategy.Preview())
                }
            }
            MotionEvent.ACTION_MOVE -> {
                handle = true
                collectPointList?.add(event.toTouchPoint().apply {
                    isFirst = true
                })
                manager.doodleDelegate.refresh()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handle = true
                collectPointList?.apply {
                    add(event.toTouchPoint().apply {
                        isLast = true
                    })

                    //finish
                    if (isNotEmpty()) {
                        onCollectFinish(manager, this)
                    }
                }
                brushElement?.let {
                    manager.doodleDelegate.addElement(it, Strategy.Normal())
                }
                brushElement = null
            }
        }

        return handle
    }

    /**点位收集完成
     * [pointList] 不为空的数据集合*/
    open fun onCollectFinish(manager: DoodleTouchManager, pointList: List<TouchPoint>) {

    }

    /**开始绘制时, 创建画刷绘制元素*/
    open fun onCreateBrushElement(): BaseElement? = null
}