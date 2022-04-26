package com.angcyo.behavior.effect

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat
import com.angcyo.behavior.BaseGestureBehavior
import com.angcyo.library.ex.abs
import com.angcyo.library.ex.mH
import com.angcyo.library.ex.mW
import com.angcyo.library.ex.toRSize
import com.angcyo.widget.R
import com.angcyo.widget.layout.isEnableCoordinator

/**
 * 支持竖直方向移动[move]的[Behavior], 通过[addScrollListener]监听滚动, 实现想要的效果.
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/10
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class MoveBehavior(
    context: Context,
    attributeSet: AttributeSet? = null
) : BaseGestureBehavior<View>(context, attributeSet) {

    /**首次布局时, 需要滚动的偏移距离. 实现半屏效果*/
    var defaultOffsetY: String = "0dp"

    var _scaledTouchSlop: Int = 0

    /**当需要重置滚动位置时触发, 通常是手指抬起.调用[resetScrollTo]方法滚动到想要的目标*/
    var moveBehaviorReset: (moveBehavior: MoveBehavior, behaviorScrollY: Int) -> Unit = { _, _ -> }

    /**滚动距离约束, [y]需要滚动至的距离, 返回修正后的滚动距离*/
    var moveScrollY: ((y: Int) -> Int)? = {
        //滚动距离约束
        val min = 0
        val max = childView.mH()
        val targetY = MathUtils.clamp(it, min, max)
        targetY
    }

    init {

        val array =
            context.obtainStyledAttributes(attributeSet, R.styleable.MoveBehavior_Layout)

        defaultOffsetY =
            array.getString(R.styleable.MoveBehavior_Layout_layout_default_offset_y)
                ?: defaultOffsetY

        array.recycle()

        _scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    override fun onMeasureAfter(parent: CoordinatorLayout, child: View) {
        super.onMeasureAfter(parent, child)
        if (!ViewCompat.isLaidOut(child)) {
            behaviorScrollY = defaultOffsetY.toRSize(
                childView.mW(),
                childView.mH(),
                def = 0,
                context = parent.context
            )
        }
    }

    override fun scrollTo(x: Int, y: Int, scrollType: Int) {
        val clampX = x
        val clampY = moveScrollY?.invoke(y) ?: y
        super.scrollTo(clampX, clampY, scrollType)
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
        return axes.isAxisVertical() && coordinatorLayout.isEnableCoordinator
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
            scrollBy(0, -dyUnconsumed, SCROLL_TYPE_NESTED)
        }
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (_nestedScrollView == null && distanceY.abs() > distanceX.abs()) {
            scrollBy(0, -distanceY.toInt(), SCROLL_TYPE_GESTURE)
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
            _resetScroll()
        }
    }

    override fun onTouchFinish(parent: CoordinatorLayout, child: View, ev: MotionEvent) {
        super.onTouchFinish(parent, child, ev)
        if (!isTouchHold && _nestedScrollView == null && ViewCompat.isLaidOut(child)) {
            //在非nested scroll 视图上滚动过
            _resetScroll()
        }
    }

    /**重置滚动状态*/
    fun _resetScroll() {
        moveBehaviorReset(this, behaviorScrollY)
    }

    /**重置滚动到指定位置*/
    fun resetScrollTo(y: Int) {
        startScrollTo(0, y)
    }
}