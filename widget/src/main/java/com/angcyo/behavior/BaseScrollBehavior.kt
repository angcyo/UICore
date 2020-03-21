package com.angcyo.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.angcyo.library.L
import com.angcyo.tablayout.clamp
import com.angcyo.widget.base.isTouchDown
import com.angcyo.widget.base.offsetLeftTo
import com.angcyo.widget.base.offsetTopTo
import kotlin.math.absoluteValue

/**
 * 支持[OverScroller]处理
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/01
 */

abstract class BaseScrollBehavior<T : View>(
    context: Context,
    attributeSet: AttributeSet? = null
) : BaseDependsBehavior<T>(context, attributeSet) {

    var _overScroller: OverScroller = OverScroller(context)

    /**布局top偏移*/
    var offsetTop = 0
        set(value) {
            field = value
            onScrollTo(scrollX, scrollY)
        }

    var offsetLeft = 0
        set(value) {
            field = value
            onScrollTo(scrollX, scrollY)
        }

    //fling 速率阈值
    var minFlingVelocity = 0
    var maxFlingVelocity = 0

    //记录当前滚动量, 只是记录值, ui效果, 还需另行处理.
    var scrollX: Int = 0
    var scrollY: Int = 0

    /**滚动值响应界面的处理*/
    var onScrollTo: (x: Int, y: Int) -> Unit = { x, y ->
        childView?.offsetLeftTo(x + offsetLeft)
        childView?.offsetTopTo(y + offsetTop)
    }

    init {
        val vc = ViewConfiguration.get(context)
        minFlingVelocity = vc.scaledMinimumFlingVelocity
        maxFlingVelocity = vc.scaledMaximumFlingVelocity
    }

    fun consumedScrollVertical(dy: Int, consumed: IntArray, constraint: Boolean = true): Int {
        if (dy == 0) {
            return 0
        }
        return if (constraint) {
            //0值约束
            if (dy > 0) {
                consumedScrollVertical(dy, scrollY, 0, scrollY, consumed)
            } else {
                consumedScrollVertical(dy, scrollY, scrollY, 0, consumed)
            }
        } else {
            val absScrollY = scrollY.absoluteValue
            consumedScrollVertical(dy, scrollY, -absScrollY, absScrollY, consumed)
        }
    }

    /**在滚动范围内, 消耗滚动, 并触发自身滚动*/
    override fun consumedScrollVertical(
        dy: Int,
        current: Int,
        min: Int,
        max: Int,
        consumed: IntArray?
    ): Int {
        //计算在范围内,需要消耗的真实dy
        val consumedDy = super.consumedScrollVertical(dy, current, min, max, consumed)
        consumed?.let {
            it[1] = consumedDy
            scrollBy(0, -consumedDy)
        }
        return consumedDy
    }

    override fun onLayoutChildAfter(parent: CoordinatorLayout, child: T, layoutDirection: Int) {
        super.onLayoutChildAfter(parent, child, layoutDirection)
        //调用requestLayout之后, 重新恢复布局状态. 如offsetTop
        scrollTo(0, scrollY)
    }

    open fun onComputeScroll(parent: CoordinatorLayout, child: T) {
        if (_overScroller.computeScrollOffset()) {
            scrollTo(_overScroller.currX, _overScroller.currY)
            //L.e("scrollTo: ${_overScroller.currY}")
            postInvalidateOnAnimation()
        }
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: T,
        ev: MotionEvent
    ): Boolean {
        if (ev.isTouchDown()) {
            _overScroller.abortAnimation()
        }
        return super.onInterceptTouchEvent(parent, child, ev)
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: T,
        target: View,
        type: Int
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
        //_overScroller.abortAnimation()
    }

    /**滚动到*/
    open fun scrollTo(x: Int, y: Int) {
        scrollX = x
        scrollY = y

        if (showLog) {
            L.v("scrollTo: x:$x y:$y")
        }
        onScrollTo(x, y)
        listeners.forEach {
            it.onBehaviorScrollTo(x, y)
        }
    }

    /**滚动多少*/
    open fun scrollBy(x: Int, y: Int) {
        scrollTo(scrollX + x, scrollY + y)
    }

    /**开始滚动到位置*/
    fun startScrollTo(x: Int, y: Int) {
        if (x == scrollX && y == scrollY) {
            return
        }
        startScroll(x - scrollX, y - scrollY)
    }

    /**开始滚动偏移量*/
    fun startScroll(dx: Int, dy: Int) {
        _overScroller.abortAnimation()
        _overScroller.startScroll(scrollX, scrollY, dx, dy)
        postInvalidateOnAnimation()
        //invalidate()
    }

    /**速率限制*/
    fun velocity(velocity: Int): Int {
        return if (velocity > 0) {
            clamp(velocity, minFlingVelocity, maxFlingVelocity)
        } else {
            clamp(velocity, -maxFlingVelocity, -minFlingVelocity)
        }
    }

    fun startFlingX(velocityX: Int, maxX: Int) {

        val vX = velocity(velocityX)

        _overScroller.abortAnimation()
        _overScroller.fling(
            scrollX,
            scrollY,
            vX,
            0,
            0,
            maxX,
            0,
            0,
            0,
            0
        )

        postInvalidateOnAnimation()
    }

    fun startFlingY(velocityY: Int, maxY: Int) {
        val vY = velocity(velocityY)
        _overScroller.abortAnimation()
        _overScroller.fling(
            scrollX,
            scrollY,
            0,
            vY,
            0,
            0,
            0,
            maxY,
            0,
            0
        )

        postInvalidateOnAnimation()
    }

    override fun onNestedFling(
        coordinatorLayout: CoordinatorLayout,
        child: T,
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed)
    }

    override fun onNestedPreFling(
        coordinatorLayout: CoordinatorLayout,
        child: T,
        target: View,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return super.onNestedPreFling(
            coordinatorLayout,
            child,
            target,
            velocityX,
            velocityY
        ) || !_overScroller.isFinished
    }

    val listeners = mutableListOf<ScrollBehaviorListener>()
    fun addScrollListener(listener: ScrollBehaviorListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeScrollListener(listener: ScrollBehaviorListener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener)
        }
    }
}

interface ScrollBehaviorListener {
    fun onBehaviorScrollTo(x: Int, y: Int)

//    //滚动状态, 无法在基类中捕捉. 自行回调
//    fun onBehaviorScrollStateChanged(state: Int) {
//
//    }
}