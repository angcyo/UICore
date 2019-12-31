package com.angcyo.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.CallSuper
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.angcyo.widget.base.isTouchDown

/**
 * 必须有2个参数的构造方法
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/09/10
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
abstract class BaseDependsBehavior<T : View>(
    context: Context? = null,
    attrs: AttributeSet? = null
) :
    LogBehavior<T>(context, attrs) {

    /**依赖的视图, 用于触发[onDependentViewChanged]*/
    var dependsLayout: View? = null

    /**是否需要监听[dependsLayout]的改变*/
    var enableDependsOn = true

    @CallSuper
    override fun layoutDependsOn(parent: CoordinatorLayout, child: T, dependency: View): Boolean {
        return enableDependsOn && dependsLayout == dependency
    }

    @CallSuper
    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: T,
        dependency: View
    ): Boolean {
        return super.onDependentViewChanged(parent, child, dependency)
    }

    /**是否处于内嵌滚动中*/
    var _isNestedScrollAccepted = false

    var _nestedScrollView: View? = null

    override fun onNestedScrollAccepted(
        coordinatorLayout: CoordinatorLayout,
        child: T,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ) {
        super.onNestedScrollAccepted(
            coordinatorLayout,
            child,
            directTargetChild,
            target,
            axes,
            type
        )
        _isNestedScrollAccepted = true
        _nestedScrollView = target
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: T,
        target: View,
        type: Int
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
        _isNestedScrollAccepted = false
        _nestedScrollView = null
    }

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
        _nestedScrollView = target
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: T,
        ev: MotionEvent
    ): Boolean {

        if (ev.isTouchDown()) {
            _nestedScrollView = null
        }

        return super.onInterceptTouchEvent(parent, child, ev)
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: T, layoutDirection: Int): Boolean {
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    /**
     * 某一个 [child] 布局结束之后的回调, 可以用来恢复[offset]的值
     * [RCoordinatorLayout.onLayoutChild]
     * */
    open fun onLayoutChildAfter(parent: CoordinatorLayout, child: T, layoutDirection: Int) {

    }

    /**
     * 所有[child]布局结束之后回调
     * */
    open fun onLayoutAfter(parent: CoordinatorLayout, child: T, layoutDirection: Int) {

    }

    override fun onMeasureChild(
        parent: CoordinatorLayout,
        child: T,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ): Boolean {
        return super.onMeasureChild(
            parent,
            child,
            parentWidthMeasureSpec,
            widthUsed,
            parentHeightMeasureSpec,
            heightUsed
        )
    }

    /**
     * 某一个 [child] 测量结束之后的回调
     * [RCoordinatorLayout.onMeasureChild]
     * */
    open fun onMeasureChildAfter(
        parent: CoordinatorLayout,
        child: T,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ) {
    }

    /**
     * 所有[child]测量结束之后回调
     * */
    open fun onMeasureAfter(parent: CoordinatorLayout, child: T) {

    }
}