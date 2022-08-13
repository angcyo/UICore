package com.angcyo.library.component

import android.view.MotionEvent
import com.angcyo.library.annotation.CallPoint
import kotlin.math.atan2

/**
 * 旋转手势识别器
 *
 * [android.view.ScaleGestureDetector]
 * [android.view.GestureDetector]
 * [androidx.core.view.GestureDetectorCompat]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/08/13
 * Copyright (c) 2020 angcyo. All rights reserved.
 */

class RotationGestureDetector(val listener: OnRotationGestureListener?) {

    companion object {
        private val INVALID_POINTER_INDEX = -1
    }

    /**获取当前旋转的角度*/
    var angle = 0f

    //第一个手指坐标
    private var fX = 0f
    private var fY = 0f

    //第二个手指坐标
    private var sX = 0f
    private var sY = 0f

    //手指的索引
    private var mPointerIndex1 = 0
    private var mPointerIndex2 = 0

    //
    private var mIsFirstTouch = false

    init {
        mPointerIndex1 = INVALID_POINTER_INDEX
        mPointerIndex2 = INVALID_POINTER_INDEX
    }

    @CallPoint
    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                sX = event.x
                sY = event.y
                mPointerIndex1 = event.findPointerIndex(event.getPointerId(0))
                angle = 0f
                mIsFirstTouch = true
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                fX = event.x
                fY = event.y
                mPointerIndex2 = event.findPointerIndex(event.getPointerId(event.actionIndex))
                angle = 0f
                mIsFirstTouch = true
            }
            MotionEvent.ACTION_MOVE -> if (mPointerIndex1 != INVALID_POINTER_INDEX &&
                mPointerIndex2 != INVALID_POINTER_INDEX &&
                event.pointerCount > mPointerIndex2
            ) {
                val nsX: Float = event.getX(mPointerIndex1)
                val nsY: Float = event.getY(mPointerIndex1)
                val nfX: Float = event.getX(mPointerIndex2)
                val nfY: Float = event.getY(mPointerIndex2)
                if (mIsFirstTouch) {
                    angle = 0f
                    mIsFirstTouch = false
                } else {
                    calculateAngleBetweenLines(fX, fY, sX, sY, nfX, nfY, nsX, nsY)
                }
                listener?.onRotation(this)
                fX = nfX
                fY = nfY
                sX = nsX
                sY = nsY
            }
            MotionEvent.ACTION_UP -> mPointerIndex1 = INVALID_POINTER_INDEX
            MotionEvent.ACTION_POINTER_UP -> mPointerIndex2 = INVALID_POINTER_INDEX
        }
        return true
    }

    private fun calculateAngleBetweenLines(
        //第一根线
        fx1: Float, fy1: Float, fx2: Float, fy2: Float,
        //第二根线
        sx1: Float, sy1: Float, sx2: Float, sy2: Float
    ): Float {
        return calculateAngleDelta(
            //第一根线的角度
            Math.toDegrees(
                atan2((fy1 - fy2).toDouble(), (fx1 - fx2).toDouble()).toFloat().toDouble()
            ).toFloat(),
            //第二根线的角度
            Math.toDegrees(
                atan2((sy1 - sy2).toDouble(), (sx1 - sx2).toDouble()).toFloat().toDouble()
            ).toFloat()
        )
    }

    private fun calculateAngleDelta(angleFrom: Float, angleTo: Float): Float {
        angle = angleTo % 360.0f - angleFrom % 360.0f
        if (angle < -180.0f) {
            angle += 360.0f
        } else if (angle > 180.0f) {
            angle -= 360.0f
        }
        return angle
    }

    /**回调*/
    open class SimpleOnRotationGestureListener : OnRotationGestureListener {
        override fun onRotation(rotationDetector: RotationGestureDetector): Boolean {
            return false
        }
    }

    /**回调*/
    interface OnRotationGestureListener {
        fun onRotation(rotationDetector: RotationGestureDetector): Boolean
    }

}