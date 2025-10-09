package com.angcyo.acc2.action

import android.graphics.PointF
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.core.DslAccessibilityGesture
import com.angcyo.acc2.core.click
import com.angcyo.acc2.core.double
import com.angcyo.acc2.core.fling
import com.angcyo.acc2.core.move
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

    var gestureStartTime: Long? = DslAccessibilityGesture.DEFAULT_GESTURE_START_TIME

    var gestureDuration: Long? = DslAccessibilityGesture.DEFAULT_GESTURE_CLICK_DURATION

    var gestureDoubleInterval: Long? = DslAccessibilityGesture.DEFAULT_GESTURE_DOUBLE_INTERVAL_TIME

    var gestureMoveDuration: Long? = DslAccessibilityGesture.DEFAULT_GESTURE_MOVE_DURATION

    var gestureFlingDuration: Long? = DslAccessibilityGesture.DEFAULT_GESTURE_FLING_DURATION

    fun click(control: AccControl, x: Float, y: Float): Boolean {
        val gesture = control.accService()?.gesture
        control.accPrint.touch(x, y, null, null)
        return gesture?.click(
            x,
            y,
            gestureStartTime ?: DslAccessibilityGesture.DEFAULT_GESTURE_START_TIME,
            gestureDuration ?: DslAccessibilityGesture.DEFAULT_GESTURE_CLICK_DURATION
        ) == true
    }

    fun double(
        control: AccControl,
        x: Float,
        y: Float
    ): Boolean {
        val gesture = control.accService()?.gesture
        control.accPrint.touch(x, y, null, null)
        gesture?.doubleInterval =
            gestureDoubleInterval ?: DslAccessibilityGesture.DEFAULT_GESTURE_DOUBLE_INTERVAL_TIME
        return gesture?.double(
            x,
            y,
            gestureStartTime ?: DslAccessibilityGesture.DEFAULT_GESTURE_START_TIME,
            gestureDuration ?: DslAccessibilityGesture.DEFAULT_GESTURE_CLICK_DURATION
        ) == true
    }

    /**
     * - [DslAccessibilityGesture.DEFAULT_GESTURE_START_TIME]
     * - [DslAccessibilityGesture.DEFAULT_GESTURE_MOVE_DURATION]
     * */
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
            gestureStartTime ?: DslAccessibilityGesture.DEFAULT_GESTURE_START_TIME,
            gestureMoveDuration ?: DslAccessibilityGesture.DEFAULT_GESTURE_MOVE_DURATION
        ) == true
    }

    /**
     * - [DslAccessibilityGesture.DEFAULT_GESTURE_START_TIME]
     * - [DslAccessibilityGesture.DEFAULT_GESTURE_MOVE_DURATION]
     * */
    fun fling(
        control: AccControl,
        x1: Float, y1: Float,
        x2: Float, y2: Float,
        startTime: Long? = null,
        duration: Long? = null,
    ): Boolean {
        val gesture = control.accService()?.gesture
        control.accPrint.touch(x1, y1, x2, y2)
        return gesture?.fling(
            x1,
            y1,
            x2,
            y2,
            startTime ?: gestureStartTime,
            duration ?: gestureFlingDuration
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