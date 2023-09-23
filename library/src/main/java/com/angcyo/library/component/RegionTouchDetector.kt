package com.angcyo.library.component

import android.graphics.Rect
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.nowTime
import kotlin.math.absoluteValue

/**
 * 在指定的区域内探测手势, 包含点击和长按的手势事件
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/23
 */
open class RegionTouchDetector(
    /**当前事件是否在区域内*/
    val isInRegionAction: (event: MotionEvent) -> Boolean,
    /**事件回调*/
    val onRegionTouchAction: (touchType: Int) -> Unit
) {

    companion object {
        /**事件类型, 点击*/
        const val TOUCH_TYPE_CLICK = 1

        /**事件类型, 长按*/
        const val TOUCH_TYPE_LONG_PRESS = 2
    }

    /**手势移动了阈值判断*/
    var touchSlop = 0

    //---

    private var _downX = 0f
    private var _downY = 0f
    private var _isTouchDownInRegion = false
    private var _touchDownTime: Long = -1

    /**长按检测时间*/
    private val _longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()//400

    /**长按事件*/
    private var _longRunnable: Runnable? = null

    init {
        touchSlop = ViewConfiguration.get(lastContext).scaledTouchSlop
    }

    /**入口事件*/
    @CallPoint
    fun onTouchEvent(v: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                _downX = event.x
                _downY = event.y
                _isTouchDownInRegion = isInRegionAction(event)
                _touchDownTime = if (_isTouchDownInRegion) {
                    _longRunnable = Runnable {
                        if (_isTouchDownInRegion) {
                            //长按
                            _isTouchDownInRegion = false
                            onLongPress()
                        }
                    }
                    MainExecutor.delay(_longRunnable!!, _longPressTimeout)
                    nowTime()
                } else {
                    -1
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (_isTouchDownInRegion) {
                    _isTouchDownInRegion = isInRegionAction(event)
                }
                if (_isTouchDownInRegion) {
                    val x = event.x
                    val y = event.y
                    if ((x - _downX).absoluteValue >= touchSlop ||
                        (y - _downY).absoluteValue >= touchSlop
                    ) {
                        //移动了
                        _isTouchDownInRegion = false
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                _longRunnable?.let {
                    MainExecutor.remove(it)
                    _longRunnable = null
                }
                if (_isTouchDownInRegion) {
                    if (isInRegionAction(event)) {
                        onClick()
                    }
                }
            }
        }
        return _isTouchDownInRegion
    }

    /**点击事件*/
    protected open fun onClick(): Boolean {
        onRegionTouchAction(TOUCH_TYPE_CLICK)
        return true
    }

    /**长按事件*/
    protected open fun onLongPress(): Boolean {
        onRegionTouchAction(TOUCH_TYPE_LONG_PRESS)
        return true
    }
}

class RectRegionTouchDetector(
    val rect: Rect,
    /**事件回调*/
    regionTouchAction: (type: Int) -> Unit
) : RegionTouchDetector({
    rect.contains(it.x.toInt(), it.y.toInt())
}, regionTouchAction)

class RectFRegionTouchDetector(
    val rect: RectF,
    /**事件回调*/
    regionTouchAction: (type: Int) -> Unit
) : RegionTouchDetector({
    rect.contains(it.x, it.y)
}, regionTouchAction)