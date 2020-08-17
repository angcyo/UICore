package com.angcyo.core.component.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.TargetApi
import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF
import android.os.Build
import com.angcyo.library.*
import com.angcyo.library.ex.dpi
import java.util.concurrent.CountDownLatch
import kotlin.random.Random.Default.nextInt

/**
 * 无障碍手势执行dsl
 * 至少需要API 24/Android 7
 *
 * https://developer.android.google.cn/reference/kotlin/android/accessibilityservice/GestureDescription.Builder
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

typealias GestureResult = (gestureDescription: GestureDescription? /*执行的手势*/, dispatched: Boolean /*是否发送*/, canceled: Boolean /*是否被取消*/) -> Unit

@TargetApi(Build.VERSION_CODES.N)
class DslAccessibilityGesture {

    companion object {

        //开始时间
        const val DEFAULT_GESTURE_START_TIME = 16L

        //点击时长
        const val DEFAULT_GESTURE_CLICK_DURATION = 16L

        //双击间隔时长
        const val DEFAULT_GESTURE_DOUBLE_DURATION = 60L

        //如果Duration时间太短, 将会产生fling
        const val DEFAULT_GESTURE_MOVE_DURATION = 600L
        const val DEFAULT_GESTURE_FLING_DURATION = 30L //值太大, 将没有fling效果
    }

    /**执行回调*/
    var gestureResult: GestureResult? = null

    var startTime: Long = DEFAULT_GESTURE_START_TIME
    var duration: Long = DEFAULT_GESTURE_MOVE_DURATION
    var clickDuration: Long = DEFAULT_GESTURE_CLICK_DURATION
    var doubleDuration: Long = DEFAULT_GESTURE_DOUBLE_DURATION
    var willContinue: Boolean = false

    /**无障碍服务, 用于执行手势*/
    var service: AccessibilityService? = null

    /**
     * 用于构建手势, 支持多指触控
     * [android.accessibilityservice.GestureDescription.getMaxStrokeCount]
     * */
    var _gestureBuilder: GestureDescription.Builder? = null

    var _gestureResultCallback: AccessibilityService.GestureResultCallback? = null

    //是否发送了事件
    var _isDispatched: Boolean = false

    /**是否执行完成*/
    var _isCompleted: Boolean = false

    var _countDownLatch: CountDownLatch? = null

    //是否已经有手势在执行
    var _isDo: Boolean = false

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            _gestureResultCallback = object : AccessibilityService.GestureResultCallback() {
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    L.d("手势取消:$gestureDescription ${gestureDescription?.strokeCount ?: 0}")
                    _isCompleted = true
                    gestureResult?.invoke(gestureDescription, true, true)
                    _countDownLatch?.countDown()
                    clear()
                }

                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    L.d("手势完成:$gestureDescription ${gestureDescription?.strokeCount ?: 0}")
                    _isCompleted = false
                    gestureResult?.invoke(gestureDescription, true, false)
                    _countDownLatch?.countDown()
                    clear()
                }
            }
        }
    }

    fun clear() {
        _isDo = false
        _isDispatched = false
        _isCompleted = false
        _gestureBuilder = null
        gestureResult = null
    }

    /**开始执行手势*/
    fun doIt() {
        if (_isDo) {
            return
        }
        _isDispatched = false
        _isCompleted = false
        val service = service
        val builder = _gestureBuilder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && service != null && builder != null) {
            //设备支持手势
            _isDo = true
            _isDispatched = service.dispatchGesture(
                builder.build(),
                _gestureResultCallback,
                null
            )
        } else {
            //设备不支持手势
            gestureResult?.invoke(null, false, true)
        }
    }

    fun ensureBuilder(action: GestureDescription.Builder.() -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (_gestureBuilder == null) {
                _gestureBuilder = GestureDescription.Builder()
            }
            _gestureBuilder?.action()
        }
    }

    //<editor-fold desc="操作方法">

    fun moveDuration(
        startTime: Long = DEFAULT_GESTURE_START_TIME,
        duration: Long = DEFAULT_GESTURE_MOVE_DURATION
    ) {
        this.startTime = startTime
        this.duration = duration
    }

    fun flingDuration(
        startTime: Long = DEFAULT_GESTURE_START_TIME,
        duration: Long = DEFAULT_GESTURE_FLING_DURATION
    ) {
        this.startTime = startTime
        this.duration = duration
    }

    fun doubleDuration(
        startTime: Long = DEFAULT_GESTURE_START_TIME,
        duration: Long = DEFAULT_GESTURE_CLICK_DURATION,
        doubleDuration: Long = DEFAULT_GESTURE_DOUBLE_DURATION
    ) {
        this.startTime = startTime
        this.duration = duration
        this.doubleDuration = doubleDuration
    }

    /**点击*/
    fun touch(fromX: Float, fromY: Float) {
        touch(PointF(fromX, fromY))
    }

    /**点击*/
    fun touch(fromX: Int, fromY: Int) {
        touch(Point(fromX, fromY))
    }

    /**点击*/
    fun touch(point: PointF) {
        duration = clickDuration
        touch(Path().apply {
            moveTo(point.x, point.y)
        })
    }

    /**点击*/
    fun touch(point: Point) {
        touch(PointF(point))
    }

    /**移动*/
    fun touch(fromX: Float, fromY: Float, toX: Float, toY: Float) {
        touch(Path().apply { moveTo(fromX, fromY);lineTo(toX, toY) })
    }

    /**移动*/
    fun touch(fromX: Int, fromY: Int, toX: Int, toY: Int) {
        touch(fromX.toFloat(), fromY.toFloat(), toX.toFloat(), toY.toFloat())
    }

    /**双击*/
    fun double(fromX: Float, fromY: Float) {
        double(PointF(fromX, fromY))
    }

    /**双击*/
    fun double(fromX: Int, fromY: Int) {
        double(fromX.toFloat(), fromY.toFloat())
    }

    /**双击*/
    fun double(point: PointF) {
        //双击, 需要伴随MOVE事件, 才能生效
        touch(
            point.x,
            point.y,
            point.x - nextInt(5, 10),
            point.y + nextInt(5, 10)
        )
        startTime += duration + doubleDuration
        touch(
            point.x,
            point.y,
            point.x + nextInt(5, 10),
            point.y - nextInt(5, 10)
        )
    }

    /**手势操作核心*/
    fun touch(
        path: Path,
        startTime: Long = this.startTime,
        duration: Long = this.duration,
        willContinue: Boolean = this.willContinue
    ) {
        if (_isDo) {
            L.w("ignore touch stroke.")
            return
        }
        ensureBuilder {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    addStroke(
                        GestureDescription.StrokeDescription(
                            path,
                            startTime,
                            duration,
                            willContinue
                        )
                    )
                } else {
                    addStroke(
                        GestureDescription.StrokeDescription(
                            path,
                            startTime,
                            duration
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //</editor-fold desc="操作方法">
}

/**DSL*/
fun AccessibilityService.dslGesture(action: DslAccessibilityGesture.() -> Unit = {}): Boolean {
    val gesture = DslAccessibilityGesture().apply {
        service = this@dslGesture
        action()
        doIt()
    }
    return gesture._isDispatched
}

fun AccessibilityService.gesture(): DslAccessibilityGesture? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        DslAccessibilityGesture().apply {
            service = this@gesture
        }
    } else {
        null
    }

//<editor-fold desc="move">

fun DslAccessibilityGesture.move(
    fromX: Int,
    fromY: Int,
    toX: Int,
    toY: Int,
    result: GestureResult? = null
): Boolean {
    return touch(fromX.toFloat(), fromY.toFloat(), toX.toFloat(), toY.toFloat(), result)
}

fun DslAccessibilityGesture.move(
    fromX: Float,
    fromY: Float,
    toX: Float,
    toY: Float,
    result: GestureResult? = null
): Boolean {
    moveDuration()
    return touch(fromX, fromY, toX, toY, result)
}

fun DslAccessibilityGesture.moveUp(result: GestureResult? = null): Boolean {
    val screenWidth = _screenWidth
    val screenHeight = _screenHeight

    val fX = screenWidth / 2 * 1f + nextInt(5, 10)
    val fY = screenHeight * 3 / 5 * 1f - nextInt(5, 10)
    val tY = screenHeight * 2 / 5 * 1f + nextInt(5, 10)

    return move(fX, fY, fX, tY, result)
}

fun DslAccessibilityGesture.moveDown(result: GestureResult? = null): Boolean {
    val screenWidth = _screenWidth
    val screenHeight = _screenHeight

    val fX = screenWidth / 2 * 1f + nextInt(5, 10)
    val fY = screenHeight * 3 / 5 * 1f - nextInt(5, 10)
    val tY = screenHeight * 2 / 5 * 1f + nextInt(5, 10)

    return move(fX, tY, fX, fY, result)
}

fun DslAccessibilityGesture.moveLeft(result: GestureResult? = null): Boolean {
    val screenWidth = _screenWidth
    val screenHeight = _screenHeight

    val fY = screenHeight / 2 * 1f + nextInt(5, 10)
    val fX = screenWidth * 3 / 5 * 1f + nextInt(5, 10)
    val tX = screenWidth * 2 / 5 * 1f - nextInt(5, 10)

    return move(fX, fY, tX, fY, result)
}

fun DslAccessibilityGesture.moveRight(result: GestureResult? = null): Boolean {
    val screenWidth = _screenWidth
    val screenHeight = _screenHeight

    val fY = screenHeight / 2 * 1f + nextInt(5, 10)
    val fX = screenWidth * 3 / 5 * 1f + nextInt(5, 10)
    val tX = screenWidth * 2 / 5 * 1f - nextInt(5, 10)

    return move(tX, fY, fX, fY, result)
}

//</editor-fold desc="move">

//<editor-fold desc="fling">

fun DslAccessibilityGesture.fling(
    fromX: Float,
    fromY: Float,
    toX: Float,
    toY: Float,
    result: GestureResult? = null
): Boolean {
    flingDuration()
    return touch(fromX, fromY, toX, toY, result)
}

/**手指往上[fling] ↑*/
fun DslAccessibilityGesture.flingUp(result: GestureResult? = null): Boolean {
    val screenWidth = _screenWidth
    val screenHeight = _screenHeight

    val fX = screenWidth / 2 * 1f + nextInt(5, 10)
    val fY = screenHeight * 3 / 5 * 1f - nextInt(5, 10)
    val tY = screenHeight * 2 / 5 * 1f + nextInt(5, 10)

    return fling(fX, fY, fX, tY, result)
}

/**手指往下[fling] ↓*/
fun DslAccessibilityGesture.flingDown(result: GestureResult? = null): Boolean {
    val screenWidth = _screenWidth
    val screenHeight = _screenHeight

    val fX = screenWidth / 2 * 1f + nextInt(5, 10)
    val fY = screenHeight * 3 / 5 * 1f - nextInt(5, 10)
    val tY = screenHeight * 2 / 5 * 1f + nextInt(5, 10)

    return fling(fX, tY, fX, fY, result)
}

fun DslAccessibilityGesture.flingLeft(result: GestureResult? = null): Boolean {
    val screenWidth = _screenWidth
    val screenHeight = _screenHeight

    val fY = screenHeight / 2 * 1f + nextInt(5, 10)
    val fX = screenWidth * 3 / 5 * 1f + nextInt(5, 10)
    val tX = screenWidth * 2 / 5 * 1f - nextInt(5, 10)

    return fling(fX, fY, tX, fY, result)
}

fun DslAccessibilityGesture.flingRight(result: GestureResult? = null): Boolean {
    val screenWidth = _screenWidth
    val screenHeight = _screenHeight

    val fY = screenHeight / 2 * 1f + nextInt(5, 10)
    val fX = screenWidth * 3 / 5 * 1f + nextInt(5, 10)
    val tX = screenWidth * 2 / 5 * 1f - nextInt(5, 10)

    return fling(tX, fY, fX, fY, result)
}

//</editor-fold desc="fling">

//<editor-fold desc="other">

fun DslAccessibilityGesture.touch(
    fromX: Float,
    fromY: Float,
    toX: Float,
    toY: Float,
    result: GestureResult? = null
): Boolean {
    gestureResult = result
    touch(fromX, fromY, toX, toY)
    doIt()
    return _isDispatched
}

fun DslAccessibilityGesture.click(
    x: Float = _screenWidth / 2f, y: Float = _screenHeight / 2f,
    result: GestureResult? = null
): Boolean {
    gestureResult = result
    touch(x, y)
    doIt()
    return _isDispatched
}

fun DslAccessibilityGesture.double(
    x: Float = _screenWidth / 2f, y: Float = _screenHeight / 2f,
    result: GestureResult? = null
): Boolean {
    gestureResult = result
    doubleDuration()
    double(x, y)
    doIt()
    return _isDispatched
}

/**随机在屏幕中产生一个点位信息*/
fun randomPoint(
    offsetLeft: Int = 10 * dpi,
    offsetTop: Int = _satusBarHeight,
    offsetRight: Int = 10 * dpi,
    offsetBottom: Int = _navBarHeight
): Point {
    val screenWidth = _screenWidth
    val screenHeight = _screenHeight

    val x: Int = nextInt(offsetLeft, screenWidth - offsetRight)
    val y: Int = nextInt(offsetTop, screenHeight - offsetBottom)

    return Point(x, y)
}

fun nextDp(from: Int, until: Int) = nextInt(from * dpi, until * dpi)

//</editor-fold desc="other">

/**随机操作, 返回随机操作名称*/
fun DslAccessibilityGesture.randomization(): Pair<Boolean, String> {
    val p1 = PointF(randomPoint())
    val p2 = PointF(randomPoint())
    return when (nextInt(10)) {
        0 -> fling(p1.x, p1.y, p2.x, p2.y) to "fling ${p1}->${p2}"
        1 -> move(p1.x, p1.y, p2.x, p2.y) to "move ${p1}->${p2}"
        2 -> click(p1.x, p1.y) to "click $p1"
        3 -> double(p1.x, p1.y, null) to "double $p1"
        4 -> fling(p1.x, p1.y, p2.x, p2.y) to "fling ${p1}->${p2}"
        5 -> fling(p1.x, p1.y, p2.x, p2.y) to "fling ${p1}->${p2}"
        6 -> fling(p1.x, p1.y, p2.x, p2.y) to "fling ${p1}->${p2}"
        7 -> fling(p1.x, p1.y, p2.x, p2.y) to "fling ${p1}->${p2}"
        8 -> click(p1.x, p1.y) to "click $p1"
        9 -> double(p1.x, p1.y, null) to "double $p1"
        else -> true to "pass"
    }
}

