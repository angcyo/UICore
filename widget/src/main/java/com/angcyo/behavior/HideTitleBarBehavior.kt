package com.angcyo.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.angcyo.behavior.placeholder.ITitleBarPlaceholderBehavior
import com.angcyo.behavior.refresh.RefreshEffectBehavior
import com.angcyo.library.L
import com.angcyo.library.ex.isDebug
import com.angcyo.widget.base.behavior
import com.angcyo.widget.base.getStatusBarHeight
import com.angcyo.widget.base.offsetTop
import com.angcyo.widget.base.offsetTopTo

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/03
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class HideTitleBarBehavior(
    context: Context? = null,
    attrs: AttributeSet? = null
) : BaseDependsBehavior<View>(context, attrs), ITitleBarPlaceholderBehavior {

    var _layoutTop = 0

    var refreshEffectBehavior: RefreshEffectBehavior? = null

    init {
        showLog = isDebug()
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        if (dependency.behavior() is RefreshEffectBehavior) {
            refreshEffectBehavior = dependency.behavior() as RefreshEffectBehavior
        }
        return super.layoutDependsOn(parent, child, dependency)
    }

    override fun onLayoutChildAfter(parent: CoordinatorLayout, child: View, layoutDirection: Int) {
        super.onLayoutChildAfter(parent, child, layoutDirection)
        child.offsetTopTo(_layoutTop)
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
        return axes.isVertical()
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

        if (refreshEffectBehavior?.scrollY ?: 0 == 0) {
            consumed[1] = onConsumedVertical(
                dy,
                _layoutTop,
                getContentExcludeHeight(this) - child.measuredHeight,
                0
            )

            child.offsetTop(-consumed[1])
            _layoutTop = child.top
        }
    }

    override fun getContentExcludeHeight(behavior: BaseDependsBehavior<*>): Int {
        return childView.getStatusBarHeight()
    }

    override fun getContentOffsetTop(behavior: BaseDependsBehavior<*>): Int {
        return childView.measuredHeight + _layoutTop
    }
}