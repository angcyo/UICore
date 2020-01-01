package com.angcyo.behavior.refresh

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.angcyo.behavior.BaseScrollBehavior
import com.angcyo.behavior.placeholder.ITitleBarPlaceholderBehavior
import com.angcyo.widget.base.coordinatorParams
import com.angcyo.widget.base.offsetTopTo

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
    val excludeHeight get() = titleBarPlaceholderBehavior?.getTitleBarHeight(this) ?: 0

    init {
        showLog = true
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        dependency.layoutParams.coordinatorParams()?.behavior?.let {
            if (it is ITitleBarPlaceholderBehavior) {
                titleBarPlaceholderBehavior = it
            }
        }
        return super.layoutDependsOn(parent, child, dependency)
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
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL && type == ViewCompat.TYPE_TOUCH
    }

    var _isFirstLayout = true
    override fun onLayoutAfter(parent: CoordinatorLayout, child: View, layoutDirection: Int) {
        super.onLayoutAfter(parent, child, layoutDirection)
        if (_isFirstLayout) {
            onScrollTo(0, excludeHeight)
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
        onScrollBy(0, -consumed[1])
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        type: Int
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)

        if (scrollY != excludeHeight) {
            startScrollTo(0, excludeHeight)
        }
    }

    override fun onScrollTo(x: Int, y: Int) {
        super.onScrollTo(x, y)
        childView.offsetTopTo(y)
    }
}