package com.angcyo.library.gesture

import android.content.Context
import android.view.MotionEvent
import com.angcyo.library.ex.abs

/**
 * 双击手势识别
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/06
 */
class DoubleGestureDetector2(context: Context, val action: (event: MotionEvent) -> Unit) {

    /**两次按下, 时长不能超过此值, 毫秒*/
    var doubleTapGap = 360f

    /**两次按下, 距离不能超过此值*/
    var doubleTapSlop = 100f

    var _touchX: Float = 0f
    var _touchY: Float = 0f
    var _isFirstTouch = true
    var _touchTime = 0L

    init {
        //val configuration = ViewConfiguration.get(context)
        //touchSlop = configuration.scaledTouchSlop
        //doubleTapSlop = configuration.scaledDoubleTapSlop.toFloat()
    }

    /**入口*/
    fun onTouchEvent(event: MotionEvent): Boolean {
        var isDoubleTouch = false
        val x = event.x
        val y = event.y
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val time = System.currentTimeMillis()
                _isFirstTouch = time - _touchTime > doubleTapGap ||
                        (_touchX - x).abs() > doubleTapSlop ||
                        (_touchY - y).abs() > doubleTapSlop

                if (_isFirstTouch) {
                    _touchTime = System.currentTimeMillis()
                    _touchX = x
                    _touchY = y
                }
            }
            MotionEvent.ACTION_UP -> {
                val time = System.currentTimeMillis()
                //L.w("${time - _touchTime} ${_touchX - x} ${_touchY - y} ${_isFirstTouch}")
                if (time - _touchTime > doubleTapGap ||
                    (_touchX - x).abs() > doubleTapSlop ||
                    (_touchY - y).abs() > doubleTapSlop
                ) {
                    _isFirstTouch = true
                } else if (!_isFirstTouch) {
                    //双击判断
                    //L.w("双击...")
                    isDoubleTouch = true
                    action(event)
                    _isFirstTouch = true
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                _isFirstTouch = true
            }
        }

        return isDoubleTouch
    }

}