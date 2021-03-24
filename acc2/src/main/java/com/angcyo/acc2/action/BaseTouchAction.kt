package com.angcyo.acc2.action

import android.graphics.PointF
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.core.*
import com.angcyo.library.ex.size
import kotlin.random.Random.Default.nextInt

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/24
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseTouchAction : BaseAction() {

    fun getGestureStartTime() = DslAccessibilityGesture.DEFAULT_GESTURE_START_TIME

    fun click(control: AccControl, x: Float, y: Float): Boolean {
        val gesture = control.accService()?.gesture
        control.accPrint.touch(x, y, null, null)
        return gesture?.click(x, y, getGestureStartTime()) == true
    }

    fun double(
        control: AccControl,
        x: Float,
        y: Float
    ): Boolean {
        val gesture = control.accService()?.gesture
        control.accPrint.touch(x, y, null, null)
        return gesture?.double(x, y, getGestureStartTime()) == true
    }

    fun move(
        control: AccControl,
        x1: Float, y1: Float,
        x2: Float, y2: Float
    ): Boolean {
        val gesture = control.accService()?.gesture
        control.accPrint.touch(x1, y1, x2, y2)
        return gesture?.move(
            x1,
            y1,
            x2,
            y2,
            getGestureStartTime()
        ) == true
    }

    fun fling(
        control: AccControl,
        x1: Float, y1: Float,
        x2: Float, y2: Float
    ): Boolean {
        val gesture = control.accService()?.gesture
        control.accPrint.touch(x1, y1, x2, y2)
        return gesture?.fling(
            x1,
            y1,
            x2,
            y2,
            getGestureStartTime()
        ) == true
    }

    fun randomPoint(pointList: List<PointF>): PointF {
        return if (pointList.size() >= 2) {
            val p1 = pointList[0]
            val p2 = pointList[1]
            val x = nextInt(p1.x.toInt(), p2.x.toInt())
            val y = nextInt(p1.y.toInt(), p2.y.toInt())
            PointF(x.toFloat(), y.toFloat())
        } else {
            pointList[0]
        }
    }
}