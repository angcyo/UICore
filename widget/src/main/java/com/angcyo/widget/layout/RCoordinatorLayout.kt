package com.angcyo.widget.layout

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.angcyo.behavior.BaseDependsBehavior
import com.angcyo.behavior.BaseScrollBehavior
import com.angcyo.widget.R
import com.angcyo.widget.base.*

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/09/10
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class RCoordinatorLayout(
    context: Context,
    attributeSet: AttributeSet? = null
) : CoordinatorLayout(context, attributeSet) {

    var bDrawable: Drawable? by InvalidateProperty(null)

    init {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.RCoordinatorLayout)
        bDrawable = typedArray.getDrawable(R.styleable.RCoordinatorLayout_r_background)
        typedArray.recycle()
    }


    /**是否还在touch中*/
    var _isTouch = false

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        if (ev.isTouchDown()) {
            _isTouch = true
        } else if (ev.isTouchFinish()) {
            _isTouch = false
        }

        return super.dispatchTouchEvent(ev)
    }

    override fun draw(canvas: Canvas) {
        bDrawable?.run {
            canvas.getClipBounds(bounds)
            draw(canvas)
        }
        super.draw(canvas)
    }

    override fun computeScroll() {
        eachChildVisibility { _, child ->
            (child.layoutParams.coordinatorParams()?.behavior as? BaseScrollBehavior)?.onComputeScroll(
                this,
                child
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        eachChildVisibility { _, child ->
            (child.layoutParams.coordinatorParams()?.behavior as? BaseDependsBehavior)?.onMeasureAfter(
                this,
                child
            )
        }
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
}