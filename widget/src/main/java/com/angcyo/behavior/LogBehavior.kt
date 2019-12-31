package com.angcyo.behavior

import android.content.Context
import android.graphics.Rect
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.angcyo.library.L
import com.angcyo.library.ex.simpleHash
import com.angcyo.widget.base.actionToString

/**
 * 打印相应方法日志
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/06/28
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class LogBehavior<T : View>(context: Context? = null, attrs: AttributeSet? = null) :
    CoordinatorLayout.Behavior<T>(context, attrs) {
    var showLog = false

    init {
        //behavior需要在xml使用layout_behavior属性声明, 才能有效
        //并且属性是声明在child上的
//        val array = context.obtainStyledAttributes(attrs, R.styleable.LogBehavior_Layout)
//        val test =
//            array.getDimensionPixelOffset(R.styleable.LogBehavior_Layout_layout_behavior_test, -1)
//        w("this...自定义属性...$test")
//        array.recycle()
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
        d("${target.simpleHash()}....dx:$dx dy:$dy type:$type")
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: T,
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
        d("${target.simpleHash()}....dxC:$dxConsumed dyC:$dyConsumed dxUC:$dxUnconsumed dyUC:$dyUnconsumed type:$type")
    }

    override fun onSaveInstanceState(
        parent: CoordinatorLayout,
        child: T
    ): Parcelable? {
        w("${child.simpleHash()}....")
        return super.onSaveInstanceState(parent, child)
    }

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
        i("${target.simpleHash()}...axes:$axes type:$type")
    }

    /**
     * @param axes 事件的方向:  1:[ViewCompat.SCROLL_AXIS_HORIZONTAL] 2:[ViewCompat.SCROLL_AXIS_VERTICAL]
     * @param type 滚动事件类型: 0:scroll 1:fling
     * @return true 接收内嵌滚动事件
     * */
    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: T,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        i("this...${target.simpleHash()} axes:$axes type:$type")
        return super.onStartNestedScroll(
            coordinatorLayout,
            child,
            directTargetChild,
            target,
            axes,
            type
        )
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: T,
        target: View,
        type: Int
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
        i("${target.simpleHash()}....")
    }

    override fun getScrimColor(
        parent: CoordinatorLayout,
        child: T
    ): Int {
        w("this....")
        return super.getScrimColor(parent, child)
    }

    override fun onNestedFling(
        coordinatorLayout: CoordinatorLayout,
        child: T,
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        d("${target.simpleHash()}....onNestedFling")
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed)
    }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: T,
        layoutDirection: Int
    ): Boolean {
        w("this...${child.simpleHash()}...ld:$layoutDirection")
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onNestedPreFling(
        coordinatorLayout: CoordinatorLayout,
        child: T,
        target: View,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        d("${target.simpleHash()}....onNestedPreFling")
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
    }

    override fun getInsetDodgeRect(
        parent: CoordinatorLayout,
        child: T,
        rect: Rect
    ): Boolean {
        w("this....")
        return super.getInsetDodgeRect(parent, child, rect)
    }

    override fun onDetachedFromLayoutParams() {
        super.onDetachedFromLayoutParams()
        w("this....")
    }

    override fun onRestoreInstanceState(
        parent: CoordinatorLayout,
        child: T,
        state: Parcelable
    ) {
        super.onRestoreInstanceState(parent, child, state)
        w("this....")
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: T,
        dependency: View
    ): Boolean {
        w("this....${child.simpleHash()}...${dependency.simpleHash()}")
        return super.layoutDependsOn(parent, child, dependency)
    }

    /**如果在此方法中, 改变了[child]的size, 需要return true*/
    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: T,
        dependency: View
    ): Boolean {
        w("this....")
        return super.onDependentViewChanged(parent, child, dependency)
    }

    override fun onDependentViewRemoved(
        parent: CoordinatorLayout,
        child: T,
        dependency: View
    ) {
        super.onDependentViewRemoved(parent, child, dependency)
        w("this....")
    }

    override fun onRequestChildRectangleOnScreen(
        coordinatorLayout: CoordinatorLayout,
        child: T,
        rectangle: Rect,
        immediate: Boolean
    ): Boolean {
        w("this....")
        return super.onRequestChildRectangleOnScreen(coordinatorLayout, child, rectangle, immediate)
    }

    override fun onApplyWindowInsets(
        coordinatorLayout: CoordinatorLayout,
        child: T,
        insets: WindowInsetsCompat
    ): WindowInsetsCompat {
        w("this...$insets")
        return super.onApplyWindowInsets(coordinatorLayout, child, insets)
    }

    override fun blocksInteractionBelow(
        parent: CoordinatorLayout,
        child: T
    ): Boolean {
        i("this....")
        return super.blocksInteractionBelow(parent, child)
    }

    /**[blocksInteractionBelow]*/
    override fun getScrimOpacity(
        parent: CoordinatorLayout,
        child: T
    ): Float {
        return super.getScrimOpacity(parent, child).apply {
            d("this....scrimOpacity:$this")
        }
    }

    override fun onTouchEvent(
        parent: CoordinatorLayout,
        child: T,
        ev: MotionEvent
    ): Boolean {
        i("this...${child.simpleHash()}...${ev.actionToString()}")
        return super.onTouchEvent(parent, child, ev)
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: T,
        ev: MotionEvent
    ): Boolean {
        i("this...${child.simpleHash()}...${ev.actionToString()}")
        return super.onInterceptTouchEvent(parent, child, ev)
    }

    override fun onAttachedToLayoutParams(params: CoordinatorLayout.LayoutParams) {
        super.onAttachedToLayoutParams(params)
        w("this...")
    }

    override fun onMeasureChild(
        parent: CoordinatorLayout,
        child: T,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ): Boolean {
        w("this...widthUsed:$widthUsed heightUsed:$heightUsed")
        return super.onMeasureChild(
            parent,
            child,
            parentWidthMeasureSpec,
            widthUsed,
            parentHeightMeasureSpec,
            heightUsed
        )
    }

    fun w(msg: String? = null) {
        if (showLog) {
            L.w(msg ?: "")
        }
    }

    fun i(msg: String? = null) {
        if (showLog) {
            L.i(msg ?: "")
        }
    }

    fun d(msg: String? = null) {
        if (showLog) {
            L.d(msg ?: "")
        }
    }
}