package com.angcyo.library.component

import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.dp
import kotlin.math.max
import kotlin.math.min

/**
 * 多指操作助手
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/03
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
object MultiFingeredHelper {

    /**获取所有手指的点位信息*/
    fun obtainPointList(
        event: MotionEvent,
        list: MutableList<PointF>,
        maxCount: Int = Int.MAX_VALUE
    ) {
        list.clear()
        for (i in 0 until min(event.pointerCount, maxCount)) {
            list.add(PointF(event.getX(i), event.getY(i)))
        }
    }

    abstract class BaseMultiFingeredGestureDetector {

        /**手指数量*/
        var gesturePointerCount: Int = 4

        protected val _touchPointList: MutableList<PointF> = mutableListOf()
        protected val _movePointList: MutableList<PointF> = mutableListOf()

        @CallPoint
        fun onTouchEvent(ev: MotionEvent) {
            when (ev.actionMasked) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    //>1个手指按下时触发
                    //L.w("ACTION_POINTER_DOWN ${ev.pointerCount}")
                    obtainPointList(ev, _touchPointList, gesturePointerCount)
                }
                MotionEvent.ACTION_MOVE -> {
                    //L.w("ACTION_MOVE ${ev.pointerCount}")
                    obtainPointList(ev, _movePointList, gesturePointerCount)
                    if (_movePointList.size >= _touchPointList.size &&
                        _touchPointList.size == gesturePointerCount
                    ) {
                        checkGestureDetector()
                    }
                }
            }
        }

        protected val _touchRect = RectF()
        protected val _moveRect = RectF()

        open fun checkGestureDetector() {
            //开始检测
            _touchRect.setEmpty()
            _touchPointList.forEach {
                _touchRect.left = min(it.x, _touchRect.left)
                _touchRect.right = max(it.x, _touchRect.right)
                _touchRect.top = min(it.y, _touchRect.top)
                _touchRect.bottom = max(it.y, _touchRect.bottom)
            }

            _moveRect.setEmpty()
            _movePointList.forEach {
                _moveRect.left = min(it.x, _moveRect.left)
                _moveRect.right = max(it.x, _moveRect.right)
                _moveRect.top = min(it.y, _moveRect.top)
                _moveRect.bottom = max(it.y, _moveRect.bottom)
            }
        }
    }

    /**多指捏合手势探测*/
    class PinchGestureDetector : BaseMultiFingeredGestureDetector() {

        /**当宽高变化超过这个值时, 视为捏合*/
        var pinchThreshold: Float = 150f * dp

        var onPinchAction: () -> Unit = {}

        /**检查是否触发捏合*/
        override fun checkGestureDetector() {
            super.checkGestureDetector()
            if ((_touchRect.width() - _moveRect.width()) >= pinchThreshold ||
                (_touchRect.height() - _moveRect.height()) >= pinchThreshold
            ) {
                onPinchAction()
                _touchPointList.clear()
            }
        }
    }

    /**多指散开手势探测*/
    class ExpandGestureDetector : BaseMultiFingeredGestureDetector() {

        /**当宽高变化超过这个值时, 视为散开*/
        var expandThreshold: Float = 150f * dp

        var onExpandAction: () -> Unit = {}

        /**检查是否触发捏合*/
        override fun checkGestureDetector() {
            super.checkGestureDetector()
            if ((_moveRect.width() - _touchRect.width()) >= expandThreshold ||
                (_moveRect.height() - _touchRect.height()) >= expandThreshold
            ) {
                onExpandAction()
                _touchPointList.clear()
            }
        }
    }

}