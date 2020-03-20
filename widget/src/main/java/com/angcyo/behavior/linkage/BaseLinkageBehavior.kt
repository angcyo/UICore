package com.angcyo.behavior.linkage

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.behavior.BaseScrollBehavior
import com.angcyo.widget.base.behavior
import com.angcyo.widget.base.findRecyclerView

/**
 * 不支持 margin 属性
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
abstract class BaseLinkageBehavior(
    context: Context,
    attributeSet: AttributeSet? = null
) : BaseScrollBehavior<View>(context, attributeSet) {

    //联动相关布局

    var headerView: View? = null
    var footerView: View? = null
    var stickyView: View? = null

    val headerRecyclerView: RecyclerView?
        get() = headerView?.findRecyclerView()

    val footerRecyclerView: RecyclerView?
        get() = footerView?.findRecyclerView()

    //正在快速fling的布局

    var _headerFlingRecyclerView: RecyclerView? = null
    var _footerFlingRecyclerView: RecyclerView? = null

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        val result = super.layoutDependsOn(parent, child, dependency)

        when (dependency.behavior()) {
            is LinkageHeaderBehavior -> headerView = dependency
            is LinkageFooterBehavior -> footerView = dependency
            is LinkageStickyBehavior -> stickyView = dependency
        }

        return result
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

}