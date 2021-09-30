package com.angcyo.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.angcyo.library.L
import com.angcyo.library.ex.abs
import com.angcyo.library.ex.orDef
import com.angcyo.library.ex.toRSize
import com.angcyo.tablayout.clamp
import com.angcyo.widget.R
import com.angcyo.widget.base.*
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

/**
 * 支持[OverScroller]处理.
 *
 * behaviorScrollY>0 表示手指向下滚动的距离
 * behaviorScrollY<0 表示手指向上滚动的距离
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/01
 */

abstract class BaseScrollBehavior<T : View>(
    context: Context,
    attributeSet: AttributeSet? = null
) : BaseDependsBehavior<T>(context, attributeSet) {

    companion object {

        /**滚动所需时长*/
        const val DEFAULT_DURATION = 360

        /**由什么方式触发的滚动*/
        const val SCROLL_TYPE_CALL = 1 //主动调用触发的滚动
        const val SCROLL_TYPE_GESTURE = 2 //手势触发的滚动
        const val SCROLL_TYPE_NESTED = 3 //内嵌触发的滚动
        const val SCROLL_TYPE_SCROLLER = 4 //滚动器触发的滚动
    }

    var scrollDuration = DEFAULT_DURATION

    var _overScroller: OverScroller = OverScroller(context)

    /**布局top偏移*/
    var behaviorOffsetTop = 0
        set(value) {
            field = value
            behaviorScrollTo(behaviorScrollX, behaviorScrollY, SCROLL_TYPE_CALL)
        }

    //属性
    var behaviorOffsetTopAttr: String? = null
    var behaviorOffsetLeftAttr: String? = null

    var behaviorOffsetLeft = 0
        set(value) {
            field = value
            behaviorScrollTo(behaviorScrollX, behaviorScrollY, SCROLL_TYPE_CALL)
        }

    //fling 速率阈值
    var minFlingVelocity = 0
    var maxFlingVelocity = 0

    //记录当前滚动量, 只是记录值, ui效果, 还需另行处理.
    var behaviorScrollX: Int = 0
    var behaviorScrollY: Int = 0

    /**滚动值响应界面的处理*/
    var behaviorScrollTo: (x: Int, y: Int, scrollType: Int) -> Unit = { x, y, _ ->
        childView?.offsetLeftTo(_childFrame?.left.orDef(0) + x + behaviorOffsetLeft)
        childView?.offsetTopTo(_childFrame?.top.orDef(0) + y + behaviorOffsetTop)
    }

    init {
        val vc = ViewConfiguration.get(context)
        minFlingVelocity = vc.scaledMinimumFlingVelocity
        maxFlingVelocity = vc.scaledMaximumFlingVelocity

        val array =
            context.obtainStyledAttributes(attributeSet, R.styleable.BaseScrollBehavior_Layout)
        scrollDuration =
            array.getInt(
                R.styleable.BaseScrollBehavior_Layout_layout_scroll_duration,
                scrollDuration
            )
        behaviorOffsetTopAttr =
            array.getString(R.styleable.BaseScrollBehavior_Layout_layout_scroll_offset_top)
                ?: behaviorOffsetTopAttr
        behaviorOffsetLeftAttr =
            array.getString(R.styleable.BaseScrollBehavior_Layout_layout_scroll_offset_left)
                ?: behaviorOffsetLeftAttr

        array.recycle()
    }

    /**限制消耗滚动的y值
     * 限制手指向上消耗滑动时, 允许滚动的最小Y值*/
    var minConsumedScrollY: Int = 0
    var minConsumedScrollX: Int = 0

    /**限制消耗滚动的y值
     * 限制手指向下消耗滑动时, 允许滚动的最大Y值*/
    var maxConsumedScrollY: Int = 0
    var maxConsumedScrollX: Int = 0

    fun consumedScrollVertical(dy: Int, consumed: IntArray, constraint: Boolean = true): Int {
        if (dy == 0) {
            return 0
        }
        return if (constraint) {
            //0值约束
            if (dy > 0) {
                consumedScrollVertical(
                    dy,
                    behaviorScrollY,
                    minConsumedScrollY,
                    max(behaviorScrollY, maxConsumedScrollY),
                    consumed
                )
            } else {
                consumedScrollVertical(
                    dy,
                    min(behaviorScrollY, minConsumedScrollY),
                    behaviorScrollY,
                    maxConsumedScrollY,
                    consumed
                )
            }
        } else {
            val absScrollY = behaviorScrollY.absoluteValue
            consumedScrollVertical(dy, behaviorScrollY, -absScrollY, absScrollY, consumed)
        }
    }

    fun consumedScrollHorizontal(dx: Int, consumed: IntArray, constraint: Boolean = true): Int {
        if (dx == 0) {
            return 0
        }
        return if (constraint) {
            //0值约束
            if (dx > 0) {
                consumedScrollHorizontal(
                    dx,
                    behaviorScrollX,
                    minConsumedScrollX,
                    max(behaviorScrollX, maxConsumedScrollX),
                    consumed
                )
            } else {
                consumedScrollHorizontal(
                    dx,
                    min(behaviorScrollX, minConsumedScrollX),
                    behaviorScrollX,
                    maxConsumedScrollX,
                    consumed
                )
            }
        } else {
            val absScrollX = behaviorScrollX.absoluteValue
            consumedScrollHorizontal(dx, behaviorScrollX, -absScrollX, absScrollX, consumed)
        }
    }

    override fun consumedScrollHorizontal(
        dx: Int,
        current: Int,
        min: Int,
        max: Int,
        consumed: IntArray?
    ): Int {
        //计算在范围内,需要消耗的真实dx
        val consumedDx = super.consumedScrollHorizontal(dx, current, min, max, consumed)
        consumed?.let {
            it[0] = consumedDx
            scrollBy(-consumedDx, 0, SCROLL_TYPE_NESTED)
        }
        return consumedDx
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
            scrollBy(0, -consumedDy, SCROLL_TYPE_NESTED)
        }
        return consumedDy
    }

    override fun onMeasureAfter(parent: CoordinatorLayout, child: T) {
        super.onMeasureAfter(parent, child)
        if (!ViewCompat.isLaidOut(child)) {
            behaviorOffsetTopAttr?.apply {
                behaviorOffsetTop = toRSize(
                    childView.mW(),
                    childView.mH(),
                    def = behaviorOffsetTop,
                    context = parent.context
                )
            }
            behaviorOffsetLeftAttr?.apply {
                behaviorOffsetLeft = toRSize(
                    childView.mW(),
                    childView.mH(),
                    def = behaviorOffsetLeft,
                    context = parent.context
                )
            }
        }
    }

    override fun onLayoutChildAfter(parent: CoordinatorLayout, child: T, layoutDirection: Int) {
        super.onLayoutChildAfter(parent, child, layoutDirection)
        //调用requestLayout之后, 重新恢复布局状态. 如offsetTop
        scrollTo(behaviorScrollX, behaviorScrollY, SCROLL_TYPE_CALL)
    }

    open fun onComputeScroll(parent: CoordinatorLayout, child: T) {
        if (_overScroller.computeScrollOffset()) {
            scrollTo(_overScroller.currX, _overScroller.currY, SCROLL_TYPE_SCROLLER)
            //L.w("${this.simpleHash()} scrollTo:x:${_overScroller.currX} y:${_overScroller.currY}")
            invalidate()
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

    /**最后一次滚动差值
     * 差值<0 手指向右滑动*/
    var _lastScrollDx: Int = 0

    /**最后一次滚动差值
     * 差值<0 手指向下滑动*/
    var _lastScrollDy: Int = 0

    /**滚动到*/
    open fun scrollTo(x: Int, y: Int, scrollType: Int) {
        _lastScrollDx = behaviorScrollX - x
        _lastScrollDy = behaviorScrollY - y

        behaviorScrollX = x
        behaviorScrollY = y

        if (showLog) {
            L.v("scrollTo: x:$x y:$y")
        }
        behaviorScrollTo(x, y, scrollType)
        onScrollTo(x, y, scrollType)
        listeners.forEach {
            it.onBehaviorScrollTo(this, x, y, scrollType)
        }
    }

    open fun onScrollTo(x: Int, y: Int, scrollType: Int) {

    }

    /**滚动是否完成*/
    fun isOverScrollFinish(): Boolean = _overScroller.isFinished

    /**滚动多少*/
    open fun scrollBy(x: Int, y: Int, scrollType: Int) {
        scrollTo(behaviorScrollX + x, behaviorScrollY + y, scrollType)
    }

    /**开始滚动到位置*/
    open fun startScrollTo(x: Int, y: Int) {
        if (x == behaviorScrollX && y == behaviorScrollY) {
            return
        }
        startScroll(x - behaviorScrollX, y - behaviorScrollY)
    }

    /**开始滚动偏移量*/
    open fun startScroll(dx: Int, dy: Int) {
        //_overScroller.abortAnimation()
        _overScroller.startScroll(behaviorScrollX, behaviorScrollY, dx, dy, scrollDuration)
        //postInvalidateOnAnimation()
        invalidate()
    }

    /**速率限制*/
    fun velocity(velocity: Int): Int {
        return if (velocity > 0) {
            clamp(velocity, minFlingVelocity, maxFlingVelocity)
        } else {
            clamp(velocity, -maxFlingVelocity, -minFlingVelocity)
        }
    }

    fun startFlingX(
        velocityX: Int,
        minX: Int,
        maxX: Int,
        overX: Int = max(minX.abs(), maxX.abs()) / 5
    ) {
        val vX = velocity(velocityX)
        //_overScroller.abortAnimation()
        _overScroller.fling(
            behaviorScrollX,
            behaviorScrollY,
            vX,
            0,
            minX,
            maxX,
            0,
            0,
            overX,
            0
        )
        postInvalidateOnAnimation()
    }

    /**
     * [velocityY] 速率 >0手指向下fling <0手指向上fling
     * [minY] 滚动最小值的边界
     * [maxY] 滚动最大值的边界
     * [overY] 是否开启over scroll. >0 表示开启, 值表示over的滚动最大距离
     * */
    fun startFlingY(
        velocityY: Int,
        minY: Int,
        maxY: Int,
        overY: Int = max(minY.abs(), maxY.abs()) / 5
    ) {
        val vY = velocity(velocityY)
        //_overScroller.abortAnimation()
        _overScroller.fling(
            behaviorScrollX,
            behaviorScrollY,
            0,
            vY,
            0,
            0,
            minY,
            maxY,
            0,
            overY
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

    /**拦截内嵌滚动*/
    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: T,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return super.onStartNestedScroll(
            coordinatorLayout,
            child,
            directTargetChild,
            target,
            axes,
            type
        )
    }

    /**内嵌滚动之前, 检查滚动状态*/
    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: T,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
    }

    /**内嵌滚动, 开始滚动效果*/
    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: T,
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
    }

    /**停止滚动, 恢复到初始状态*/
    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: T,
        target: View,
        type: Int
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
        //_overScroller.abortAnimation()
        if (!isTouchHold) {
            listeners.forEach {
                it.onBehaviorScrollStop(this, SCROLL_TYPE_NESTED, SCROLL_TYPE_NESTED)
            }
        }
    }

    val listeners = mutableListOf<IScrollBehaviorListener>()
    fun addScrollListener(listener: IScrollBehaviorListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeScrollListener(listener: IScrollBehaviorListener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener)
        }
    }
}

fun <T : View> BaseScrollBehavior<T>.scrollToY(y: Int) {
    scrollTo(0, y, BaseScrollBehavior.SCROLL_TYPE_CALL)
}

fun <T : View> BaseScrollBehavior<T>.scrollToX(x: Int) {
    scrollTo(x, 0, BaseScrollBehavior.SCROLL_TYPE_CALL)
}