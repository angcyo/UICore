package com.angcyo.widget.layout

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.angcyo.behavior.BaseDependsBehavior
import com.angcyo.behavior.BaseScrollBehavior
import com.angcyo.library.L
import com.angcyo.library.ex.hash
import com.angcyo.widget.R
import com.angcyo.widget.base.*

/**
 * 协调布局
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/09/10
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class RCoordinatorLayout(
    context: Context,
    attributeSet: AttributeSet? = null
) : CoordinatorLayout(context, attributeSet), ILayoutDelegate, ITouchHold, ITouchDelegate {

    val layoutDelegate = RLayoutDelegate()

    val _touchDelegate = TouchActionDelegate()

    init {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.RCoordinatorLayout)

        getCustomLayoutDelegate().initAttribute(this, attributeSet)

        typedArray.recycle()
    }

    override fun dispatchDraw(canvas: Canvas) {
        try {
            super.dispatchDraw(canvas)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.isTouchDown()) {
            this.isTouchHold = true
        } else if (ev.isTouchFinish()) {
            this.isTouchHold = false
        }
        getTouchActionDelegate().dispatchTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        getTouchActionDelegate().onInterceptTouchEvent(ev)
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        getTouchActionDelegate().onTouchEvent(ev)
        return super.onTouchEvent(ev)
    }

    var _disallowIntercept: Boolean = false

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
        _disallowIntercept = disallowIntercept
    }

    override fun requestLayout() {
        super.requestLayout()
        if (!isInEditMode) {
            L.d("${hash()} requestLayout...")
        }
    }

    override fun computeScroll() {
        eachChildVisibility { _, child ->
            (child.layoutParams.coordinatorParams()?.behavior as? BaseScrollBehavior)?.onComputeScroll(
                this,
                child
            )
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //当[CoordinatorLayout]只有一个child时, layoutDependsOn方法不会回调, 有点尴尬
        each { child ->
            (child.layoutParams.coordinatorParams()?.behavior as? BaseDependsBehavior)?.run {
                childView = child
                parentLayout = this@RCoordinatorLayout
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val layoutDelegate = getCustomLayoutDelegate()
        val layoutWidthHeightSpec =
            layoutDelegate.layoutWidthHeightSpec(widthMeasureSpec, heightMeasureSpec)
        val layoutDimensionRatioSpec = layoutDelegate.layoutDimensionRatioSpec(
            layoutWidthHeightSpec[0],
            layoutWidthHeightSpec[1]
        )
        super.onMeasure(layoutDimensionRatioSpec[0], layoutDimensionRatioSpec[1])
        layoutDelegate.onMeasure(layoutDimensionRatioSpec[0], layoutDimensionRatioSpec[1])

        eachChildVisibility { _, child ->
            (child.layoutParams.coordinatorParams()?.behavior as? BaseDependsBehavior)?.onMeasureAfter(
                this,
                child
            )
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val layoutDelegate = getCustomLayoutDelegate()
        layoutDelegate.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onMeasureChild(
        child: View,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ) {
        super.onMeasureChild(
            child,
            parentWidthMeasureSpec,
            widthUsed,
            parentHeightMeasureSpec,
            heightUsed
        )

        (child.layoutParams.coordinatorParams()?.behavior as? BaseDependsBehavior)?.onMeasureChildAfter(
            this,
            child,
            parentWidthMeasureSpec,
            widthUsed,
            parentHeightMeasureSpec,
            heightUsed
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        val layoutDelegate = getCustomLayoutDelegate()
        layoutDelegate.onLayout(changed, l, t, r, b)

        val layoutDirection = ViewCompat.getLayoutDirection(this)

        eachChildVisibility { _, child ->
            (child.layoutParams.coordinatorParams()?.behavior as? BaseDependsBehavior)?.onLayoutAfter(
                this,
                child,
                layoutDirection
            )
        }
    }

    override fun onLayoutChild(child: View, layoutDirection: Int) {
        super.onLayoutChild(child, layoutDirection)
        (child.layoutParams.coordinatorParams()?.behavior as? BaseDependsBehavior)?.onLayoutChildAfter(
            this,
            child,
            layoutDirection
        )
    }

    override fun getCustomLayoutDelegate(): RLayoutDelegate {
        return layoutDelegate
    }

    override fun getTouchActionDelegate(): TouchActionDelegate {
        return _touchDelegate
    }

    /**是否还在touch中*/
    override var isTouchHold: Boolean = false
}

/**是否激活了协调功能*/
var CoordinatorLayout.isEnableCoordinator: Boolean
    get() = isEnabled
    set(value) {
        isEnabled = value
    }