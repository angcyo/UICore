package com.angcyo.behavior.linkage

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.behavior.BaseScrollBehavior
import com.angcyo.library.L
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
        var _flingRecyclerView: WeakReference<RecyclerView>? = null
    }

    //联动相关布局
    var headerView: View? = null
    var footerView: View? = null
    var stickyView: View? = null

    //behavior 作用在的[RecyclerView], 通常会等于[headerRecyclerView] [footerRecyclerView]其中的一个
    val childRecyclerView: RecyclerView?
        get() = childView?.findRecyclerView()

    val headerRecyclerView: RecyclerView?
        get() = headerView?.findRecyclerView()

    val footerRecyclerView: RecyclerView?
        get() = footerView?.findRecyclerView()

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
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
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

        L.i("down $this")

        _flingRecyclerView?.get()?.apply {
            L.i("down $this")
            stopNestedScroll()
            stopScroll()
        }
        _flingRecyclerView?.clear()
        _flingRecyclerView = null

        _nestedPreFling = false
    }

    //Fling访问标识
    var _nestedPreFling = false

    override fun onNestedPreFling(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (childRecyclerView == target) {
            _nestedPreFling = true
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
            && childRecyclerView == target /*只处理自己内部的RecyclerView*/
            && _flingRecyclerView == null /*防止多次触发*/
        ) {

            val velocityY = target.getLastVelocity().toInt()
            if (velocityY <= 0) {
                return
            }

            val footerRV = footerRecyclerView
            if (target == footerRV) {
                //L.i("footer fling $velocityY")

                //来自Footer的Fling, 那么要传给Header
                headerRecyclerView?.apply {
                    _flingRecyclerView = WeakReference(this)
                    fling(0, (-velocityY * 0.9).toInt()) //fling速度衰减
                }
            } else {
                val headerRV = headerRecyclerView
                if (target == headerRV) {
                    //L.i("header fling $velocityY")

                    //来自Header的Fling, 那么要传给Footer
                    footerRV?.apply {
                        _flingRecyclerView = WeakReference(this)
                        fling(0, (velocityY * 0.9).toInt()) //fling速度衰减
                    }
                }
            }
        }

//        //fling 传递
//        if (dyUnconsumed > 0 && _bottomFlingRecyclerView == null && isStickClose()) {
//            val velocityY = topRecyclerView?.getLastVelocity()?.toInt() ?: 0
//            //L.e("lastVelocity1:.....${topRecyclerView?.simpleHash()} $velocityY")
//
//            if (velocityY != 0) {
//                bottomRecyclerView?.apply {
//                    _bottomFlingRecyclerView = this
//                    fling(0, (velocityY * 0.9).toInt())
//                }
//            }
//        } else if (dyUnconsumed < 0 && _topFlingRecyclerView == null) {
//            if (target == bottomRecyclerView) {
//                val velocityY = (target as? RecyclerView)?.getLastVelocity()?.toInt() ?: 0
//                //L.e("lastVelocity2:.....${target.simpleHash()} $velocityY")
//
//                if (velocityY != 0) {
//                    topRecyclerView?.apply {
//                        _topFlingRecyclerView = this
//                        fling(0, (-velocityY * 0.9).toInt())
//                    }
//                }
//            }
//        }
    }
}