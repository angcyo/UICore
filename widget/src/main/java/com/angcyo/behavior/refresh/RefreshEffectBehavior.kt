package com.angcyo.behavior.refresh

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.angcyo.behavior.BaseDependsBehavior
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
    context: Context? = null,
    attrs: AttributeSet? = null
) : BaseDependsBehavior<View>(context, attrs) {

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
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onLayoutAfter(parent: CoordinatorLayout, child: View, layoutDirection: Int) {
        super.onLayoutAfter(parent, child, layoutDirection)
        child.offsetTopTo(excludeHeight)
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
}