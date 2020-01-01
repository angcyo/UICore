package com.angcyo.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.angcyo.tablayout.clamp

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

    //fling 速率阈值
    var minFlingVelocity = 0
    var maxFlingVelocity = 0

    //记录当前滚动量
    var scrollX: Int = 0
    var scrollY: Int = 0

    init {
        val vc = ViewConfiguration.get(context)
        minFlingVelocity = vc.scaledMinimumFlingVelocity
        maxFlingVelocity = vc.scaledMaximumFlingVelocity
    }

    open fun onComputeScroll(parent: CoordinatorLayout, child: T) {
        if (_overScroller.computeScrollOffset()) {
            onScrollTo(_overScroller.currX, _overScroller.currY)
            invalidate()
        }
    }

    /**滚动到*/
    open fun onScrollTo(x: Int, y: Int) {
        scrollX = x
        scrollY = y
    }

    /**滚动多少*/
    open fun onScrollBy(x: Int, y: Int) {
        onScrollTo(scrollX + x, scrollY + y)
    }

    /**开始滚动到位置*/
    fun startScrollTo(x: Int, y: Int) {
        startScroll(x - scrollX, y - scrollY)
    }

    /**开始滚动偏移量*/
    fun startScroll(dx: Int, dy: Int) {
        _overScroller.abortAnimation()
        _overScroller.startScroll(scrollX, scrollY, dx, dy)
        postInvalidate()
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