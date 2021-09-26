package com.angcyo.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.angcyo.widget.base.isTouchDown
import com.angcyo.widget.base.isTouchFinish
import com.angcyo.widget.base.isTouchIn

/**
 * 支持[GestureDetector]的处理.
 *
 * 使用时, 请注意[CoordinatorLayout]具有消耗事件的能力, 否则[TOUCH_DOWN]之后, 就无法收到其他事件了.
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/20
 */
abstract class BaseGestureBehavior<T : View>(
    context: Context,
    attributeSet: AttributeSet? = null
) : BaseScrollBehavior<T>(context, attributeSet) {

    /**touch scroll 阈值*/
    var touchSlop = 0

    /**是否开启touch捕捉*/
    var enableGesture = true

    /**是否只处理child上的touch事件*/
    var enableGestureTouchIn = false

    /**手指松开时的横向速率, 小于0 手指向左fling*/
    var lastVelocityX = 0f

    /**手指松开时的纵向速率, 小于0 手指向上fling */
    var lastVelocityY = 0f

    //手势检测
    val _gestureDetector: GestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                _nestedFlingView = childView
                lastVelocityX = velocityX
                lastVelocityY = velocityY
                nestedFlingVelocityX = -velocityX
                nestedFlingVelocityY = -velocityY
                return onGestureFling(e1, e2, velocityX, velocityY)
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                return onGestureScroll(e1, e2, distanceX, distanceY)
            }
        })
    }

    /**手势捕捉*/
    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: T,
        ev: MotionEvent
    ): Boolean {
        return super.onInterceptTouchEvent(parent, child, ev) || handleTouchEvent(parent, child, ev)
    }

    /**手势捕捉*/
    override fun onTouchEvent(parent: CoordinatorLayout, child: T, ev: MotionEvent): Boolean {
        return super.onTouchEvent(parent, child, ev) || handleTouchEvent(parent, child, ev)
    }

    //是否需要touch event, 当子view调用了[requestDisallowInterceptTouchEvent]后, 还是能收到MOVE事件.
    //典型的案例就是ViewPager
    var _needHandleTouch = true

    //首次滚动检查, 如果首次滚动不需要处理事件, 那么之后都收不到.
    var _isFirstScroll = true

    /**统一手势处理*/
    open fun handleTouchEvent(parent: CoordinatorLayout, child: T, ev: MotionEvent): Boolean {
        var result = false
        if (ev.isTouchFinish()) {
            parent.requestDisallowInterceptTouchEvent(false)
            onTouchFinish(parent, child, ev)
        } else if (ev.isTouchDown()) {
            _needHandleTouch = true
            _isFirstScroll = true
            lastVelocityX = 0f
            lastVelocityY = 0f
            onTouchDown(parent, child, ev)
        }
        if (enableGesture && _needHandleTouch) {
            if (enableGestureTouchIn) {
                if (ev.isTouchIn(child)) {
                    result = _gestureDetector.onTouchEvent(ev)
                }
            } else {
                result = _gestureDetector.onTouchEvent(ev)
            }
        }
        return result
    }

    open fun onTouchDown(parent: CoordinatorLayout, child: T, ev: MotionEvent) {

    }

    open fun onTouchFinish(parent: CoordinatorLayout, child: T, ev: MotionEvent) {
        if (!isTouchHold && _nestedScrollView == null && ViewCompat.isLaidOut(child)) {
            //在非nested scroll 视图上滚动过
            listeners.forEach {
                it.onBehaviorScrollStop(this, SCROLL_TYPE_GESTURE, SCROLL_TYPE_GESTURE)
            }
        }
    }

    open fun onGestureFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return onFling(e1, e2, velocityX, velocityY)
    }

    open fun onGestureScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        val result = onScroll(e1, e2, distanceX, distanceY)

        if (_isFirstScroll) {
            _needHandleTouch = result
            _isFirstScroll = false
        }

        return result
    }

    /**手势Fling处理*/
    open fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }

    /**手势Scroll处理*/
    open fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }
}