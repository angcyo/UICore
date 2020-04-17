package com.angcyo.behavior.effect

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat
import com.angcyo.behavior.BaseGestureBehavior
import com.angcyo.widget.base.mH
import com.angcyo.widget.base.offsetTopTo

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/17
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class TouchBackBehavior(
    context: Context,
    attributeSet: AttributeSet? = null
) : BaseGestureBehavior<View>(context, attributeSet) {

    init {

    }

    override fun scrollTo(x: Int, y: Int) {
        val min = 0
        val max = childView.mH()
        val targetY = MathUtils.clamp(y, min, max)
        super.scrollTo(x, targetY)
        childView?.offsetTopTo(targetY + behaviorOffsetTop)
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        super.onStartNestedScroll(
            coordinatorLayout,
            child,
            directTargetChild,
            target,
            axes,
            type
        )
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)

        if (behaviorScrollY != 0 && dy != 0) {
            //内容产生过偏移, 那么此次的内嵌滚动肯定是需要消耗的
            consumedScrollVertical(dy, consumed)
        }
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        super.onNestedScroll(
            coordinatorLayout,
            child,
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type,
            consumed
        )

        if (dyConsumed == 0 && type.isTouch()) {
            //内嵌滚动视图已经不需要消耗滚动值了, 通常是到达了首尾两端
            scrollBy(0, -dyUnconsumed)
        }
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (_nestedScrollView == null) {
            scrollBy(0, -distanceY.toInt())
            return true
        }
        return false
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        type: Int
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
        if (!isTouchHold) {
            resetScroll()
        }
    }

    override fun onTouchFinish(parent: CoordinatorLayout, child: View, ev: MotionEvent) {
        super.onTouchFinish(parent, child, ev)
        if (!isTouchHold && _nestedScrollView == null) {
            //在非nested scroll 视图上滚动过
            resetScroll()
        }
    }

    /**重置滚动状态*/
    fun resetScroll() {
        if (behaviorScrollY > childView.mH() / 5) {
            //滑动大于child的5分之一
            scrollToClose()
        } else {
            scrollToNormal()
        }
    }

    fun scrollToNormal() {
        startScrollTo(0, 0)
    }

    fun scrollToClose() {
        startScrollTo(0, childView.mH())
    }
}