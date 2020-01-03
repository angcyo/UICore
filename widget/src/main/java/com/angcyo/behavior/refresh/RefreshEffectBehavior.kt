package com.angcyo.behavior.refresh

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.angcyo.behavior.BaseScrollBehavior
import com.angcyo.behavior.BehaviorInterpolator
import com.angcyo.behavior.HideTitleBarBehavior
import com.angcyo.behavior.placeholder.ITitleBarPlaceholderBehavior
import com.angcyo.widget.base.behavior
import com.angcyo.widget.base.bottomCanScroll
import com.angcyo.widget.base.offsetTopTo
import com.angcyo.widget.base.topCanScroll
import kotlin.math.absoluteValue


/**
 * 下拉刷新效果的行为
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/31
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class RefreshEffectBehavior(
    context: Context,
    attrs: AttributeSet? = null
) : BaseScrollBehavior<View>(context, attrs) {

    /**标题栏的行为*/
    var titleBarPlaceholderBehavior: ITitleBarPlaceholderBehavior? = null

    /**[child]需要排除多少高度*/
    val excludeHeight get() = titleBarPlaceholderBehavior?.getContentExcludeHeight(this) ?: 0
    /**至少需要从什么位置开始布局*/
    val layoutTop get() = titleBarPlaceholderBehavior?.getContentOffsetTop(this) ?: 0

    /**输入dy, 输出修正后的dy*/
    var behaviorInterpolator: BehaviorInterpolator = object : BehaviorInterpolator {
        override fun getInterpolation(input: Int, max: Int): Int {
            val f = (scrollY - layoutTop).absoluteValue * 1f / max
            return when {
                f < 0.1f -> input
                f < 0.2f -> (input * 0.7f).toInt()
                f < 0.3f -> (input * 0.3f).toInt()
                f < 0.4f -> (input * 0.1f).toInt()
                else -> 0
            }
        }
    }

    init {
        showLog = false
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        dependency.behavior()?.let {
            if (it is ITitleBarPlaceholderBehavior) {
                titleBarPlaceholderBehavior = it
            }
        }
        super.layoutDependsOn(parent, child, dependency)

        return dependency.behavior() is HideTitleBarBehavior
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        child.offsetTopTo(dependency.bottom)
        return super.onDependentViewChanged(parent, child, dependency)
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
        _overScroller.abortAnimation()
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    var _isFirstLayout = true
    override fun onLayoutAfter(parent: CoordinatorLayout, child: View, layoutDirection: Int) {
        super.onLayoutAfter(parent, child, layoutDirection)
        if (_isFirstLayout) {
            onScrollTo(0, 0)
        } else {
            onScrollTo(0, scrollY)
        }
        _isFirstLayout = false
    }

    override fun onMeasureChild(
        parent: CoordinatorLayout,
        child: View,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ): Boolean {

        super.onMeasureChild(
            parent,
            child,
            parentWidthMeasureSpec,
            widthUsed,
            parentHeightMeasureSpec,
            heightUsed
        )

        parent.onMeasureChild(
            child,
            parentWidthMeasureSpec,
            widthUsed,
            parentHeightMeasureSpec,
            heightUsed + excludeHeight
        )

        return true
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

        if (scrollY > 0 && dy > 0) {
            if (target.topCanScroll()) {
                onConsumedVertical(dy, scrollY, 0, scrollY).apply {
                    consumed[1] = this
                    onScrollBy(0, -this)
                }
            }
        } else if (scrollY < 0 && dy < 0) {
            if (target.bottomCanScroll()) {
                onConsumedVertical(dy, scrollY, scrollY, 0).apply {
                    consumed[1] = this
                    onScrollBy(0, -this)
                }
            }
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

        if (dyConsumed == 0 && type == ViewCompat.TYPE_TOUCH) {
            //内嵌滚动视图已经不需要消耗滚动值了, 通常是到达了首尾两端

            val dy: Int
            if (scrollY > 0) {
                dy = if (dyUnconsumed < 0) {
                    //继续下拉, 才需要阻尼, 反向不需要
                    behaviorInterpolator.getInterpolation(
                        -dyUnconsumed,
                        childView.measuredHeight
                    )
                } else {
                    -dyUnconsumed
                }
            } else {
                dy = if (dyUnconsumed > 0) {
                    //继续上拉, 才需要阻尼, 反向不需要
                    behaviorInterpolator.getInterpolation(
                        -dyUnconsumed,
                        childView.measuredHeight
                    )
                } else {
                    -dyUnconsumed
                }
            }

            onScrollBy(0, dy)
        }
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        type: Int
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)

        if (scrollY != 0) {
            startScrollTo(0, 0)
        }
    }

    override fun onScrollTo(x: Int, y: Int) {
        //L.e("y:$y")
        super.onScrollTo(0, y)
        childView.offsetTopTo(y + layoutTop)
    }
}