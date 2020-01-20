package com.angcyo.behavior.refresh

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.angcyo.behavior.BaseDependsBehavior
import com.angcyo.behavior.BaseScrollBehavior
import com.angcyo.behavior.IContentBehavior
import com.angcyo.behavior.ITitleBarBehavior
import com.angcyo.widget.base.behavior
import com.angcyo.widget.layout.RCoordinatorLayout


/**
 * 下拉刷新行为处理类, UI效果处理代理给[IRefreshBehavior]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/31
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class RefreshBehavior(
    context: Context,
    attrs: AttributeSet? = null
) : BaseScrollBehavior<View>(context, attrs), IContentBehavior {

    companion object {
        //正常状态
        const val STATUS_NORMAL = 0
        //刷新状态
        const val STATUS_REFRESH = 1
        //刷新完成
        const val STATUS_FINISH = 10
    }

    /**标题栏的行为, 用于布局在标题栏bottom里面*/
    var titleBarPlaceholderBehavior: ITitleBarBehavior? = null

    /**[child]需要排除多少高度*/
    val excludeHeight get() = titleBarPlaceholderBehavior?.getContentExcludeHeight(this) ?: 0

    /**刷新行为界面处理*/
    var refreshBehaviorConfig: IRefreshBehavior? = RefreshEffectConfig()

    /**刷新触发的回调*/
    var onRefresh: (RefreshBehavior) -> Unit = {}

    /**未释放[TOUCH_EVENT]*/
    val _touchHold get() = (parentLayout as? RCoordinatorLayout)?._isTouch == true

    /**刷新状态*/
    var refreshStatus: Int = STATUS_NORMAL
        set(value) {
            val old = field
            field = value
            if (old != value) {
                refreshBehaviorConfig?.onRefreshStatusChange(this, old, value, _touchHold)
            }
        }

    init {
        showLog = false

        onScrollTo = { x, y ->
            refreshBehaviorConfig?.onContentScrollTo(this, x, y)
        }
    }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int
    ): Boolean {
        offsetTop = titleBarPlaceholderBehavior?.getContentOffsetTop(this) ?: 0
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        dependency.behavior()?.let {
            if (it is ITitleBarBehavior) {
                titleBarPlaceholderBehavior = it
            }
        }
        super.layoutDependsOn(parent, child, dependency)
        return enableDependsOn && dependency.behavior() is ITitleBarBehavior
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        offsetTop = dependency.bottom
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
        return axes.isVertical()
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

        if (scrollY != 0 && dy != 0) {
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
            refreshBehaviorConfig?.onContentOverScroll(this, dxUnconsumed, dyUnconsumed)
        }
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        type: Int
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
        refreshBehaviorConfig?.onContentStopScroll(this, _touchHold)
    }

    override fun getContentScrollY(behavior: BaseDependsBehavior<*>): Int {
        return scrollY
    }

    /**开始刷新*/
    fun startRefresh() {
        refreshStatus = STATUS_REFRESH
    }

    /**结束刷新*/
    fun finishRefresh() {
        refreshStatus = STATUS_FINISH
    }
}