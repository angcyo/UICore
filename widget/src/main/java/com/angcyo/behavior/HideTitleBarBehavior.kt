package com.angcyo.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.angcyo.widget.base.*

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/03
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class HideTitleBarBehavior(
    context: Context,
    attrs: AttributeSet? = null
) : BaseScrollBehavior<View>(context, attrs), ITitleBarBehavior {

    /**忽略状态栏的高度*/
    var ignoreStatusBar = false
    /**只在边界的时候, 才开始滚动*/
    var scrollEdge = false

    var contentBehavior: IContentBehavior? = null

    init {
        showLog = false
        onScrollTo = { _, y ->
            childView.offsetTopTo(y)
        }
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        if (dependency.behavior() is IContentBehavior) {
            contentBehavior = dependency.behavior() as IContentBehavior
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

        val contentScrollY = contentBehavior?.getContentScrollY(this) ?: 0

        var handle = false
        if (scrollEdge) {
            if (!target.topCanScroll() && !target.bottomCanScroll()) {
                handle = scrollY != 0
            } else if (!target.topCanScroll()) {
                if (contentScrollY == 0) {
                    handle = true
                }
            }
        } else {
            if (target.topCanScroll() || target.bottomCanScroll()) {
                if (contentScrollY == 0) {
                    handle = true
                }
            } else {
                handle = scrollY != 0
            }
        }

        if (handle) {
            consumedScrollVertical(
                dy,
                scrollY,
                getContentExcludeHeight(this) - child.measuredHeight,
                0,
                consumed
            )
        }
    }

    override fun getContentExcludeHeight(behavior: BaseDependsBehavior<*>): Int {
        return if (ignoreStatusBar) 0 else childView.getStatusBarHeight()
    }

    override fun getContentOffsetTop(behavior: BaseDependsBehavior<*>): Int {
        return if (ViewCompat.isLaidOut(childView)) {
            childView.bottom
        } else {
            childView.measuredHeight + scrollY
        }
    }
}