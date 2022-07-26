package com.angcyo.doodle.data

import android.view.MotionEvent

/**
 * 手势触发的点位信息
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
data class TouchPoint(
    //是否是第一个点
    var isFirst: Boolean = false,
    //是否是最后一个点, UP or CANCEL 产生的点
    var isLast: Boolean = false,
    /**
     * 手势的时间戳, 毫秒
     * [android.view.MotionEvent.getEventTime]
     * [android.os.SystemClock.uptimeMillis]
     * */
    var timestamp: Long = -1,
    //手势的点位信息
    var eventX: Float = 0f,
    var eventY: Float = 0f,
    /**较上一个点的移动速度*/
    var speed: Float = 0f,
    /**较上一个点的距离, 像素*/
    var distance: Float = 0f,
    /**较上一个点的角度*/
    var angle: Float = 0f,
    //压力, 需要硬件支持
    var pressure: Float = 0f,
    //设备类型, 手写笔/鼠标/手写板等
    var toolType: Int = MotionEvent.TOOL_TYPE_UNKNOWN
)

/**转换*/
fun MotionEvent.toTouchPoint() = TouchPoint().apply {
    isFirst = false
    isLast = false
    eventX = x
    eventY = y
    timestamp = eventTime
    pressure = this@toTouchPoint.pressure
    toolType = getToolType(0)
}