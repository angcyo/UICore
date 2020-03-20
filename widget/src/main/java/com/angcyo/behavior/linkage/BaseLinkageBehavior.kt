package com.angcyo.behavior.linkage

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.NestedScrollingChild
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.behavior.BaseScrollBehavior
import com.angcyo.widget.base.*
import java.lang.ref.WeakReference

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

    companion object {
        //正在快速fling的布局
        var _flingScrollView: WeakReference<NestedScrollingChild>? = null
    }

    //联动相关布局
    var headerView: View? = null
    var footerView: View? = null
    var stickyView: View? = null

    //behavior 作用在的[RecyclerView], 通常会等于[headerRecyclerView] [footerRecyclerView]其中的一个
    val childScrollView: NestedScrollingChild?
        get() = childView?.findNestedScrollingChild()

    val headerScrollView: NestedScrollingChild?
        get() = headerView?.findNestedScrollingChild()

    val footerScrollView: NestedScrollingChild?
        get() = footerView?.findNestedScrollingChild()

    /**关联布局依赖*/
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

    /**接收内嵌滚动*/
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
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL &&
                (headerView.mH() + stickyView.mH() + footerView.mH()) > coordinatorLayout.mH()
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: View,
        ev: MotionEvent
    ): Boolean {
        if (ev.isTouchDown()) {
            _onTouchDown()
        } else if (ev.isTouchFinish()) {
            parent.requestDisallowInterceptTouchEvent(false)
        }
        return super.onInterceptTouchEvent(parent, child, ev)
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: View, ev: MotionEvent): Boolean {
        if (ev.isTouchDown()) {
            _onTouchDown()
        } else if (ev.isTouchFinish()) {
            parent.requestDisallowInterceptTouchEvent(false)
        }
        return super.onTouchEvent(parent, child, ev)
    }

    fun _onTouchDown() {
//        _handleTouch = true
//        _isFirstScroll = true
        _overScroller.abortAnimation()
        _nestedScrollView?.apply {

            //L.e("停止滚动...${this.simpleName()}")

            this.stopNestedScroll()
            if (this is RecyclerView) {
                this.stopScroll()
            }
            _nestedScrollView = null
        }

//        _bottomFlingRecyclerView?.stopNestedScroll()
//        _bottomFlingRecyclerView?.stopScroll()
//        _bottomFlingRecyclerView = null
//
//        _topFlingRecyclerView?.stopNestedScroll()
//        _topFlingRecyclerView?.stopScroll()
//        _topFlingRecyclerView = null

        //L.i("down $this")

        _flingScrollView?.get()?.apply {
            //L.i("down $this")
            stopNestedScroll()
            if (this is RecyclerView) {
                stopScroll()
            }
        }
        _flingScrollView?.clear()
        _flingScrollView = null

        _nestedPreFling = false
    }

    //Fling访问标识
    var _nestedPreFling = false

    //Fling的方向, >0 手指向上, <0 手指向下.
    var _nestedFlingDirection = 0

    override fun onNestedPreFling(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (childScrollView == target) {
            _nestedPreFling = true
            _nestedFlingDirection = velocityY.toInt()
        }
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
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

        if (dyUnconsumed != 0 /*处于over scroll的情况*/
            && _nestedPreFling /*Fling访问*/
            && childScrollView == target /*只处理自己内部的RecyclerView*/
            && _flingScrollView == null /*防止多次触发*/
        ) {

            //fling速度衰减
            val velocityY = (target.getLastVelocity() * 0.9).toInt()
            if (velocityY == 0) {
                return
            }

            val footerRV = footerScrollView
            val headerRV = headerScrollView
            if (target == footerRV) {
                //L.i("footer fling $velocityY")

                //来自Footer的Fling, 那么要传给Header
                headerRV?.apply {
                    _flingScrollView = WeakReference(this)

                    if (this is RecyclerView) {
                        fling(0, -velocityY)
                    } else {
                        //[NestedScrollView]的速度值貌似是和[RecyclerView]反向的
                        if (_nestedFlingDirection > 0) {
                            fling(0, velocityY)
                        } else {
                            fling(0, -velocityY)
                        }
                    }
                }
            } else if (target == headerRV) {
                //L.i("header fling $velocityY")

                //来自Header的Fling, 那么要传给Footer
                footerRV?.apply {
                    _flingScrollView = WeakReference(this)
                    if (this is RecyclerView) {
                        fling(0, velocityY)
                    } else {
                        if (_nestedFlingDirection > 0) {
                            fling(0, velocityY)
                        } else {
                            fling(0, -velocityY)
                        }
                    }
                }
            }
        }
    }
}