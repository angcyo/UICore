package com.angcyo.widget.base

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ListView
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils.clamp
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.library.ex.getStatusBarHeight
import com.angcyo.library.ex.undefined_res
import com.angcyo.widget.base.ViewEx._tempRect

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

object ViewEx {
    val _tempRect = Rect()
}

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

fun <T : View> View?.find(@IdRes id: Int): T? {
    return this?.findViewById(id)
}

fun View?.parentMeasuredHeight(): Int {
    return (this?.parent as? View?)?.measuredHeight ?: 0
}

fun View?.parentMeasuredWidth(): Int {
    return (this?.parent as? View?)?.measuredWidth ?: 0
}

fun View.setWidth(width: Int) {
    val params = layoutParams
    params.width = width
    layoutParams = params
}

fun View.setHeight(height: Int) {
    val params = layoutParams
    params.height = height
    layoutParams = params
}

/**
 * 设置视图的宽高
 * */
fun View.setWidthHeight(width: Int = undefined_res, height: Int = undefined_res) {
    val params = layoutParams
    if (width != undefined_res) {
        params.width = width
    }
    if (height != undefined_res) {
        params.height = height
    }
    layoutParams = params
}

fun View.setBgDrawable(drawable: Drawable?) {
    ViewCompat.setBackground(this, drawable)
}

/**长按震动反馈*/
fun View.longFeedback() {
    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
}

/**
 * 错误提示
 */
fun View.error() {
    //Anim.band(this)

    val mAnimatorSet = AnimatorSet()

    mAnimatorSet.playTogether(
        ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.25f, 0.75f, 1.15f, 1f),
        ObjectAnimator.ofFloat(this, "scaleY", 1f, 0.75f, 1.25f, 0.85f, 1f)
    )

    mAnimatorSet.interpolator = DecelerateInterpolator()
    mAnimatorSet.duration = 300
    mAnimatorSet.start()

    requestFocus()
}

/**将[View]重新添加到新的[ViewGroup]*/
fun View.addTo(parent: ViewGroup?, action: (View) -> Unit = {}) {
    val oldParent = getParent()
    if (oldParent is ViewGroup) {
        oldParent.removeView(this)
    }
    action(this)
    parent?.addView(this)
}

fun View.drawRect(rect: Rect) {
    rect.set(paddingLeft, paddingTop, measuredWidth - paddingRight, measuredHeight - paddingBottom)
}

fun View.drawRect(rect: RectF) {
    rect.set(
        paddingLeft.toFloat(), paddingTop.toFloat(),
        (measuredWidth - paddingRight).toFloat(),
        (measuredHeight - paddingBottom).toFloat()
    )
}

fun View.viewRect(rect: Rect) {
    rect.set(0, 0, measuredWidth, measuredHeight)
}

fun View.viewRect(rect: RectF) {
    rect.set(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
}


/**视图View 变灰*/
fun View.grayscale(enable: Boolean = true) {
    if (enable) {
        //变灰, 界面灰度处理
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)//饱和度 0灰色 100过度彩色，50正常
        val filter = ColorMatrixColorFilter(matrix)
        val paint = Paint()
        paint.colorFilter = filter

        setLayerType(View.LAYER_TYPE_SOFTWARE, paint)
    } else {
        setLayerType(View.LAYER_TYPE_NONE, null)
    }
}

//</editor-fold desc="基础扩展">


//<editor-fold desc="layoutParams扩展">

/**快速操作[LayoutParams]*/
fun View.marginParams(config: ViewGroup.MarginLayoutParams.() -> Unit = {}): View {
    (this.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
        config()
        this@marginParams.layoutParams = layoutParams
    }
    return this
}

fun View.frameParams(config: FrameLayout.LayoutParams.() -> Unit = {}): View {
    (this.layoutParams as? FrameLayout.LayoutParams)?.apply {
        config()
        this@frameParams.layoutParams = layoutParams
    }
    return this
}

fun View.coordinatorParams(config: CoordinatorLayout.LayoutParams.() -> Unit = {}): View {
    (this.layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
        config()
        this@coordinatorParams.layoutParams = layoutParams
    }
    return this
}

fun View.constraintParams(config: ConstraintLayout.LayoutParams.() -> Unit = {}): View {
    (this.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
        config()
        this@constraintParams.layoutParams = layoutParams
    }
    return this
}

/**将[LayoutParams]强转成指定对象*/
fun ViewGroup.LayoutParams.marginParams(config: ViewGroup.MarginLayoutParams.() -> Unit = {}): ViewGroup.MarginLayoutParams? {
    return (this as? ViewGroup.MarginLayoutParams)?.run {
        config()
        this
    }
}

fun ViewGroup.LayoutParams.frameParams(config: FrameLayout.LayoutParams.() -> Unit = {}): FrameLayout.LayoutParams? {
    return (this as? FrameLayout.LayoutParams)?.run {
        config()
        this
    }
}

fun ViewGroup.LayoutParams.coordinatorParams(config: CoordinatorLayout.LayoutParams.() -> Unit = {}): CoordinatorLayout.LayoutParams? {
    return (this as? CoordinatorLayout.LayoutParams)?.run {
        config()
        this
    }
}

fun ViewGroup.LayoutParams.constraintParams(config: ConstraintLayout.LayoutParams.() -> Unit = {}): ConstraintLayout.LayoutParams? {
    return (this as? ConstraintLayout.LayoutParams)?.run {
        config()
        this
    }
}

fun ViewGroup.LayoutParams.recyclerParams(config: RecyclerView.LayoutParams.() -> Unit = {}): RecyclerView.LayoutParams? {
    return (this as? RecyclerView.LayoutParams)?.run {
        config()
        this
    }
}


//</editor-fold desc="layoutParams扩展">


//<editor-fold desc="offset扩展">


fun View.offsetTop(offset: Int) {
    ViewCompat.offsetTopAndBottom(this, offset)
}

/**限制滚动偏移的范围, 返回值表示 需要消耗的 距离*/
fun View.offsetTop(offset: Int, minTop: Int, maxTop: Int): Int {
    val offsetTop = top + offset
    val newTop = clamp(offsetTop, minTop, maxTop)

    offsetTopTo(newTop)

    return -(offset - (offsetTop - newTop))
}

fun View.offsetTopTo(newTop: Int) {
    offsetTop(newTop - top)
}

fun View.offsetTopTo(newTop: Int, minTop: Int, maxTop: Int) {
    offsetTop(newTop - top, minTop, maxTop)
}

fun View.offsetLeft(offset: Int) {
    ViewCompat.offsetLeftAndRight(this, offset)
}

/**限制滚动偏移的范围, 返回值表示 需要消耗的 距离*/
fun View.offsetLeft(offset: Int, minLeft: Int, maxLeft: Int): Int {
    val offsetLeft = left + offset
    val newLeft = clamp(offsetLeft, minLeft, maxLeft)

    offsetTopTo(newLeft)

    return -(offset - (offsetLeft - newLeft))
}

fun View.offsetLeftTo(newLeft: Int) {
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

/**点击事件*/
fun View?.clickIt(action: (View) -> Unit) {
    this?.setOnClickListener(action)
}

/**点击事件节流处理*/
fun View?.throttleClickIt(action: (View) -> Unit) {
    this?.setOnClickListener(ThrottleClickListener(action = action))
}

/**长按事件*/
fun View?.longClick(action: (View) -> Boolean) {
    this?.setOnLongClickListener { action(it) }
}

//</editor-fold desc="scroll扩展">

//<editor-fold desc="draw相关扩展">

val View.drawLeft get() = paddingLeft
val View.drawTop get() = paddingTop
val View.drawRight get() = measuredWidth - paddingRight
val View.drawBottom get() = measuredHeight - paddingBottom
val View.drawWidth get() = drawRight - drawLeft
val View.drawHeight get() = drawBottom - drawTop
val View.drawCenterX get() = drawLeft + drawWidth / 2
val View.drawCenterY get() = drawTop + drawHeight / 2

fun View?.isVisible() = this?.visibility == View.VISIBLE
fun View?.isGone() = this?.visibility == View.GONE

//</editor-fold desc="draw相关扩展">

//<editor-fold desc="回调扩展">


/**[androidx/core/view/View.kt:79]*/
fun View.doOnPreDraw(action: (View) -> Unit) {
    val view = this
    viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            action(view)
            viewTreeObserver.removeOnPreDrawListener(this)
            return false
        }
    })
}

//</editor-fold desc="draw相关扩展">

//<editor-fold desc="软键盘相关">

/**隐藏软键盘*/
fun View.hideSoftInput() {
    val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    manager.hideSoftInputFromWindow(windowToken, 0)
}

/**显示软键盘*/
fun View.showSoftInput() {
    if (this is EditText) {
        requestFocus()
        val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.showSoftInput(this, 0)
    }
}

/**
 * 获取键盘的高度
 */
fun View.getSoftKeyboardHeight(): Int {
    val screenHeight = getScreenHeightPixels()
    getWindowVisibleDisplayFrame(_tempRect)
    val visibleBottom = _tempRect.bottom
    return screenHeight - visibleBottom
}

/**
 * 屏幕高度(不包含虚拟导航键盘的高度)
 */
fun View.getScreenHeightPixels(): Int {
    return resources.displayMetrics.heightPixels
}

/**
 * 判断键盘是否显示
 */
fun View.isSoftKeyboardShow(minHeight: Int = 100): Boolean {
    val screenHeight = getScreenHeightPixels()
    val keyboardHeight = getSoftKeyboardHeight()
    return screenHeight != keyboardHeight && keyboardHeight > minHeight
}

//</editor-fold desc="软键盘相关">