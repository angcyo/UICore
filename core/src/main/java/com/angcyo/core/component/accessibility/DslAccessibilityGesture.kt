package com.angcyo.core.component.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import com.angcyo.fragment.FragmentBridge
import com.angcyo.library.L
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
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

class DslAccessibilityGesture :
    HandlerThread("DslAccessibilityGesture-${FragmentBridge.generateCode()}") {

    companion object {
        const val DEFAULT_GESTURE_START_TIME = 60L

        //点击时长
        const val DEFAULT_GESTURE_CLICK_DURATION = 60L

        //双击间隔时长
        const val DEFAULT_GESTURE_DOUBLE_DURATION = 60L

        //如果Duration时间太短, 将会产生fling
        const val DEFAULT_GESTURE_MOVE_DURATION = 600L
        const val DEFAULT_GESTURE_FLING_DURATION = 30L //值太大, 将没有fling效果
    }

    /**执行回调*/
    var gestureResult: GestureResult? = null

    /**是否需要异步执行*/
    var async: Boolean = true

    /**是否需要等待异步结果返回, 如果在主线程中开启会anr*/
    var awaitResult: Boolean = false

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
        start()

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

    override fun onLooperPrepared() {
        super.onLooperPrepared()
    }

    override fun quit(): Boolean {
        clear()
        service = null
        return super.quit()
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
            _isDo = true
            _isDispatched = service.dispatchGesture(
                builder.build(),
                _gestureResultCallback,
                if (async) Handler(looper) else null
            )
            if (awaitResult) {
                try {
                    _countDownLatch = CountDownLatch(1)
                    _countDownLatch?.await(10, TimeUnit.SECONDS)
                } catch (e: Exception) {
                    L.e(e)
                } finally {
                    _countDownLatch = null
                }
            }
        } else {
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
        duration = clickDuration
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
fun AccessibilityService.dslGesture(action: DslAccessibilityGesture.() -> Unit): Boolean {
    val gesture = DslAccessibilityGesture().apply {
        service = this@dslGesture
        async = true
        awaitResult = false
        action()
        doIt()
    }
    gesture.quit()
    return gesture._isCompleted
}

fun DslAccessibilityGesture.move(
    fromX: Int,
    fromY: Int,
    toX: Int,
    toY: Int,
    result: GestureResult? = null
) {
    touch(fromX.toFloat(), fromY.toFloat(), toX.toFloat(), toY.toFloat(), result)
}

fun DslAccessibilityGesture.move(
    fromX: Float,
    fromY: Float,
    toX: Float,
    toY: Float,
    result: GestureResult? = null
) {
    moveDuration()
    touch(fromX, fromY, toX, toY, result)
}

fun DslAccessibilityGesture.fling(
    fromX: Float,
    fromY: Float,
    toX: Float,
    toY: Float,
    result: GestureResult? = null
) {
    flingDuration()
    touch(fromX, fromY, toX, toY, result)
}

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
    x: Float, y: Float,
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
    double(x, y)
    doIt()
    return _isDispatched
}