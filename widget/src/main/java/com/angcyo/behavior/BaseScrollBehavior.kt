package com.angcyo.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.angcyo.tablayout.clamp
import com.angcyo.widget.base.offsetLeftTo
import com.angcyo.widget.base.offsetTopTo
import kotlin.math.absoluteValue

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/01
 */

open class BaseScrollBehavior<T : View>(
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

    //记录当前滚动量
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
        return super.consumedScrollVertical(dy, current, min, max, consumed).apply {
            consumed?.let {
                it[1] = this
                scrollBy(0, -this)
            }
        }
    }

    override fun onLayoutChildAfter(parent: CoordinatorLayout, child: T, layoutDirection: Int) {
        super.onLayoutChildAfter(parent, child, layoutDirection)
        scrollTo(0, scrollY)
    }

    open fun onComputeScroll(parent: CoordinatorLayout, child: T) {
        if (_overScroller.computeScrollOffset()) {
            scrollTo(_overScroller.currX, _overScroller.currY)
            postInvalidateOnAnimation()
        }
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: T,
        target: View,
        type: Int
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
        _overScroller.abortAnimation()
    }

    /**滚动到*/
    open fun scrollTo(x: Int, y: Int) {
        scrollX = x
        scrollY = y

        onScrollTo(x, y)
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

        postInvalidate()
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

        postInvalidate()
    }
}