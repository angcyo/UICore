package com.angcyo.widget.base

import android.graphics.drawable.Drawable
import android.view.*
import android.widget.FrameLayout
import android.widget.ListView
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils.clamp
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.drawable.getStatusBarHeight

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */


//<editor-fold desc="基础扩展">

fun View.getColor(@ColorRes id: Int): Int {
    return ContextCompat.getColor(context, id)
}

fun View.getDrawable(id: Int): Drawable? {
    if (id <= 0) {
        return null
    }
    return ContextCompat.getDrawable(context, id)
}

fun View.getStatusBarHeight(): Int {
    return context.getStatusBarHeight()
}

/**双击事件*/
fun View.onDoubleTap(action: (View) -> Boolean) {
    val view = this
    val gestureDetector = GestureDetectorCompat(context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                return action(view)
            }
        })
    setOnTouchListener { _, event ->
        gestureDetector.onTouchEvent(event)
    }
}

/**设置[Behavior]*/
fun View?.setBehavior(behavior: CoordinatorLayout.Behavior<*>?) {
    this?.layoutParams?.coordinatorParams {
        this.behavior = behavior
    }
}

fun View?.behavior(): CoordinatorLayout.Behavior<*>? {
    return (this?.layoutParams as? CoordinatorLayout.LayoutParams?)?.run { this.behavior }
}

//</editor-fold desc="基础扩展">


//<editor-fold desc="layoutParams扩展">

/**快速操作[LayoutParams]*/
public fun View.marginParams(config: ViewGroup.MarginLayoutParams.() -> Unit = {}): View {
    (this.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
        config()
        this@marginParams.layoutParams = layoutParams
    }
    return this
}

public fun View.frameParams(config: FrameLayout.LayoutParams.() -> Unit = {}): View {
    (this.layoutParams as? FrameLayout.LayoutParams)?.apply {
        config()
        this@frameParams.layoutParams = layoutParams
    }
    return this
}

public fun View.coordinatorParams(config: CoordinatorLayout.LayoutParams.() -> Unit = {}): View {
    (this.layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
        config()
        this@coordinatorParams.layoutParams = layoutParams
    }
    return this
}

public fun View.constraintParams(config: ConstraintLayout.LayoutParams.() -> Unit = {}): View {
    (this.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
        config()
        this@constraintParams.layoutParams = layoutParams
    }
    return this
}

/**将[LayoutParams]强转成指定对象*/
public fun ViewGroup.LayoutParams.marginParams(config: ViewGroup.MarginLayoutParams.() -> Unit = {}): ViewGroup.MarginLayoutParams? {
    return (this as? ViewGroup.MarginLayoutParams)?.run {
        config()
        this
    }
}

public fun ViewGroup.LayoutParams.frameParams(config: FrameLayout.LayoutParams.() -> Unit = {}): FrameLayout.LayoutParams? {
    return (this as? FrameLayout.LayoutParams)?.run {
        config()
        this
    }
}

public fun ViewGroup.LayoutParams.coordinatorParams(config: CoordinatorLayout.LayoutParams.() -> Unit = {}): CoordinatorLayout.LayoutParams? {
    return (this as? CoordinatorLayout.LayoutParams)?.run {
        config()
        this
    }
}

public fun ViewGroup.LayoutParams.constraintParams(config: ConstraintLayout.LayoutParams.() -> Unit = {}): ConstraintLayout.LayoutParams? {
    return (this as? ConstraintLayout.LayoutParams)?.run {
        config()
        this
    }
}

public fun ViewGroup.LayoutParams.recyclerParams(config: RecyclerView.LayoutParams.() -> Unit = {}): RecyclerView.LayoutParams? {
    return (this as? RecyclerView.LayoutParams)?.run {
        config()
        this
    }
}


//</editor-fold desc="layoutParams扩展">


//<editor-fold desc="offset扩展">


public fun View.offsetTop(offset: Int) {
    ViewCompat.offsetTopAndBottom(this, offset)
}

/**限制滚动偏移的范围, 返回值表示 需要消耗的 距离*/
public fun View.offsetTop(offset: Int, minTop: Int, maxTop: Int): Int {
    val offsetTop = top + offset
    val newTop = clamp(offsetTop, minTop, maxTop)

    offsetTopTo(newTop)

    return -(offset - (offsetTop - newTop))
}

public fun View.offsetTopTo(newTop: Int) {
    offsetTop(newTop - top)
}

public fun View.offsetTopTo(newTop: Int, minTop: Int, maxTop: Int) {
    offsetTop(newTop - top, minTop, maxTop)
}

public fun View.offsetLeft(offset: Int) {
    ViewCompat.offsetLeftAndRight(this, offset)
}

/**限制滚动偏移的范围, 返回值表示 需要消耗的 距离*/
public fun View.offsetLeft(offset: Int, minLeft: Int, maxLeft: Int): Int {
    val offsetLeft = left + offset
    val newLeft = clamp(offsetLeft, minLeft, maxLeft)

    offsetTopTo(newLeft)

    return -(offset - (offsetLeft - newLeft))
}

public fun View.offsetLeftTo(newLeft: Int) {
    offsetLeft(newLeft - left)
}

//</editor-fold desc="offset扩展">


//<editor-fold desc="scroll扩展">

/** View 顶部是否还有可滚动的距离 */
fun View?.topCanScroll(): Boolean {
    return canChildScroll(-1)
}

/** View 底部是否还有可滚动的距离 */
fun View?.bottomCanScroll(): Boolean {
    return canChildScroll(1)
}

fun View?.canChildScroll(direction: Int, depth: Int = 5): Boolean {
    if (this == null || depth < 0) {
        return false
    }
    if (this is RecyclerView || this is ListView) {
        //no op
    } else if (this is ViewGroup) {
        val group = this
        var child: View?
        var result: Boolean
        for (i in 0 until group.childCount) {
            child = group.getChildAt(i)
            result = when (child) {
                is RecyclerView -> child.canScrollVertically(direction)
                is ListView -> child.canScrollVertically(direction)
                is ViewGroup -> child.canChildScroll(direction, depth - 1)
                else -> (child?.canScrollVertically(direction) ?: child.canChildScroll(direction))
            }
            if (result) {
                return true
            }
        }
    }
    return this.canScrollVertically(direction)
}

fun View?.clickIt(action: (View) -> Unit) {
    this?.setOnClickListener(action)
}

fun View?.throttleClickIt(action: (View) -> Unit) {
    this?.setOnClickListener(ThrottleClickListener(action = action))
}

//</editor-fold desc="scroll扩展">

//<editor-fold desc="draw相关扩展">

val View.drawLeft get() = paddingLeft
val View.drawTop get() = paddingTop
val View.drawRight get() = right - paddingRight
val View.drawBottom get() = bottom - paddingBottom
val View.drawWidth get() = drawRight - drawLeft
val View.drawHeight get() = drawBottom - drawTop
val View.drawCenterX get() = drawLeft + drawWidth / 2
val View.drawCenterY get() = drawTop + drawHeight / 2

fun View?.isVisible() = this?.visibility == View.VISIBLE
fun View?.isGone() = this?.visibility == View.GONE

//</editor-fold desc="draw相关扩展">

//<editor-fold desc="回调扩展">

fun View.doOnPreDraw(action: (View) -> Unit) {
    viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            action(this@doOnPreDraw)
            viewTreeObserver.removeOnPreDrawListener(this)
            return false
        }
    })
}

//</editor-fold desc="draw相关扩展">
