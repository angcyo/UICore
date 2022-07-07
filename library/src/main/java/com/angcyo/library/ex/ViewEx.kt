package com.angcyo.library.ex

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.EditText
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils.clamp
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import com.angcyo.library.ex.ViewEx._tempArray
import com.angcyo.library.ex.ViewEx._tempRect

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

object ViewEx {
    val _tempRect = Rect()

    val _tempRectF = RectF()

    val _tempArray = intArrayOf(-1, -1)
}

//<editor-fold desc="基础扩展">

fun View.getColor(@ColorRes id: Int): Int {
    return ContextCompat.getColor(context, id)
}

fun View.loadDrawable(@DrawableRes id: Int): Drawable? {
    return context?.loadDrawable(id)
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
        !hasOnClickListeners()
    }
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
fun View.setWidthHeight(width: Int = undefined_size, height: Int = undefined_size) {
    val params = layoutParams ?: return
    if (width != undefined_size) {
        params.width = width
    }
    if (height != undefined_size) {
        params.height = height
    }
    layoutParams = params
}

/**长按震动反馈
 * 执行触觉反馈*/
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

/**移除自身*/
fun View.removeFromParent(): Boolean {
    var result = false
    parent?.let {
        if (it is ViewGroup) {
            it.removeView(this)
            result = true
        }
    }
    return result
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

fun View.viewRect(rect: Rect = _tempRect): Rect {
    rect.set(0, 0, measuredWidth, measuredHeight)
    return rect
}

fun View.viewRect(rect: RectF = _tempRectF): RectF {
    rect.set(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
    return rect
}

fun View.viewFrame(rect: Rect = _tempRect): Rect {
    rect.set(left, top, right, bottom)
    return rect
}

/**视图在父控件中的位置*/
fun View.viewFrameF(rect: RectF = _tempRectF): RectF {
    rect.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
    return rect
}

/**视图在屏幕中的位置*/
fun View.viewScreenFrameF(rect: RectF = _tempRectF): RectF {

    //视图左上角坐标, 相对于屏幕的坐标. (包含了状态的高度)
    getLocationOnScreen(_tempArray)

    val x = _tempArray[0]
    val y = _tempArray[1]
    rect.set(
        x.toFloat(), y.toFloat(),
        (x + measuredWidth).toFloat(), (y + measuredHeight).toFloat()
    )
    return rect
}

/**视图View 变灰, 灰度处理*/
fun View.grayscale(enable: Boolean = true) {
    if (enable) {
        //变灰, 界面灰度处理
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)//饱和度 0灰色 100过度彩色，50正常
        val filter = ColorMatrixColorFilter(matrix)
        val paint = Paint()
        paint.colorFilter = filter
        if (layerType != View.LAYER_TYPE_HARDWARE) {
            setLayerType(View.LAYER_TYPE_HARDWARE, paint)
        }
        //setLayerType(View.LAYER_TYPE_SOFTWARE, paint)
        ViewCompat.setLayerPaint(this, paint)
    } else {
        if (layerType != View.LAYER_TYPE_NONE) {
            setLayerType(View.LAYER_TYPE_NONE, null)
        } else {
            ViewCompat.setLayerPaint(this, null)
        }
    }
}

/**进入全屏模式, 切换全屏/非全屏, 需要使用同一个[View]对象,否则不生效, 当这个[View]被销毁后, 会自动恢复状态
 * https://www.jianshu.com/p/e9e443271c98*/
fun View?.fullscreen(full: Boolean = true) {
    /*
     * View.SYSTEM_UI_FLAG_LAYOUT_STABLE：全屏显示时保证尺寸不变。
     * View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN：Activity全屏显示，状态栏显示在Activity页面上面。
     * View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION：效果同View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
     * View.SYSTEM_UI_FLAG_HIDE_NAVIGATION：隐藏导航栏
     * View.SYSTEM_UI_FLAG_FULLSCREEN：Activity全屏显示，且状态栏被隐藏覆盖掉。
     * View.SYSTEM_UI_FLAG_VISIBLE：Activity非全屏显示，显示状态栏和导航栏。
     * View.INVISIBLE：Activity伸展全屏显示，隐藏状态栏。
     * View.SYSTEM_UI_LAYOUT_FLAGS：效果同View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
     * View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY：必须配合View.SYSTEM_UI_FLAG_FULLSCREEN和View.SYSTEM_UI_FLAG_HIDE_NAVIGATION组合使用，达到的效果是拉出状态栏和导航栏后显示一会儿消失。
     * */
    this?.run {
        systemUiVisibility = when {
            full -> {
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        //拉出状态栏和导航栏后显示一会儿消失。
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            }
            else -> {
                systemUiVisibility.remove(View.SYSTEM_UI_FLAG_FULLSCREEN)

                //以下2中方法都有效
                //systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

                //systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                //View.SYSTEM_UI_FLAG_IMMERSIVE or
                //View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            }
        }
    }
}

/**低调模式, 状态栏导航栏变暗, 只会显示电量*/
fun View?.lowProfile(lowProfile: Boolean = true) {
    this?.run {
        systemUiVisibility = when {
            lowProfile -> {
                //低调模式, 状态栏导航栏变暗, 只会显示电量
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        //拉出状态栏和导航栏后显示一会儿消失。
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            }
            else -> {
                systemUiVisibility.remove(View.SYSTEM_UI_FLAG_FULLSCREEN)
                    .remove(View.SYSTEM_UI_FLAG_LOW_PROFILE)
            }
        }
    }
}

fun View?.padding(p: Int) {
    this?.setPadding(p, p, p, p)
}

fun View?.paddingHorizontal(p: Int) {
    this?.setPadding(p, paddingTop, p, paddingBottom)
}

fun View?.paddingVertical(p: Int) {
    this?.setPadding(paddingLeft, p, paddingRight, p)
}

fun View?.paddingLeft(p: Int) {
    this?.setPadding(p, paddingTop, paddingRight, paddingBottom)
}

fun View?.paddingRight(p: Int) {
    this?.setPadding(paddingLeft, paddingTop, p, paddingBottom)
}

fun View?.paddingTop(p: Int) {
    this?.setPadding(paddingLeft, p, paddingRight, paddingBottom)
}

fun View?.paddingBottom(p: Int) {
    this?.setPadding(paddingLeft, paddingTop, paddingRight, p)
}

fun View?.mH(def: Int = 0): Int {
    return this?.measuredHeight ?: def
}

fun View?.mW(def: Int = 0): Int {
    return this?.measuredWidth ?: def
}

fun View?.l(def: Int = 0): Int {
    return this?.left ?: def
}

fun View?.t(def: Int = 0): Int {
    return this?.top ?: def
}

fun View?.r(def: Int = 0): Int {
    return this?.right ?: def
}

fun View?.b(def: Int = 0): Int {
    return this?.bottom ?: def
}

/**执行后移除runnable*/
fun View?.postAndRemove(action: () -> Unit) {
    this?.post(object : Runnable {
        override fun run() {
            removeCallbacks(this)
            action()
        }
    })
}

fun View?.postDelay(delayMillis: Long = 160, action: () -> Unit) {
    this?.postDelayed(object : Runnable {
        override fun run() {
            removeCallbacks(this)
            action()
        }
    }, delayMillis)
}

/**设置视图的背景
 * [android.view.View.setBackground]*/
fun View?.setBgDrawable(background: Drawable?) {
    this?.run { ViewCompat.setBackground(this, background) }
}

//</editor-fold desc="基础扩展">

//<editor-fold desc="offset扩展">

fun View.offsetTopBottom(offset: Int) {
    ViewCompat.offsetTopAndBottom(this, offset)
}

//top

/**限制滚动偏移的范围, 返回值表示 需要消耗的 距离*/
fun View.offsetTop(offset: Int, minTop: Int, maxTop: Int): Int {
    val offsetTop = top + offset
    val newTop = clamp(offsetTop, minTop, maxTop)

    offsetTopTo(newTop)

    return -(offset - (offsetTop - newTop))
}

fun View.offsetTopTo(newTop: Int) {
    offsetTopBottom(newTop - top)
}

fun View.offsetTopTo(newTop: Int, minTop: Int, maxTop: Int) {
    offsetTop(newTop - top, minTop, maxTop)
}

fun View.offsetLeftRight(offset: Int) {
    ViewCompat.offsetLeftAndRight(this, offset)
}

//bottom

fun View.offsetBottom(offset: Int, minBottom: Int, maxBottom: Int): Int {
    val offsetBottom = bottom + offset
    val newBottom = clamp(offsetBottom, minBottom, maxBottom)

    offsetBottomTo(newBottom)

    return -(offset - (offsetBottom - newBottom))
}

fun View.offsetBottomTo(newBottom: Int) {
    offsetTopBottom(newBottom - bottom)
}

fun View.offsetBottomTo(newBottom: Int, minBottom: Int, maxBottom: Int) {
    offsetBottom(newBottom - bottom, minBottom, maxBottom)
}

//left

/**限制滚动偏移的范围, 返回值表示 需要消耗的 距离*/
fun View.offsetLeft(offset: Int, minLeft: Int, maxLeft: Int): Int {
    val offsetLeft = left + offset
    val newLeft = clamp(offsetLeft, minLeft, maxLeft)

    offsetLeftTo(newLeft)

    return -(offset - (offsetLeft - newLeft))
}

fun View.offsetLeftTo(newLeft: Int) {
    offsetLeftAndRight(newLeft - left)
}

fun View.offsetLeftTo(newLeft: Int, minLeft: Int, maxLeft: Int) {
    offsetLeft(newLeft - left, minLeft, maxLeft)
}

//right

fun View.offsetRight(offset: Int, minRight: Int, maxRight: Int): Int {
    val offsetRight = right + offset
    val newRight = clamp(offsetRight, minRight, maxRight)

    offsetRightTo(newRight)

    return -(offset - (offsetRight - newRight))
}

fun View.offsetRightTo(newRight: Int) {
    offsetLeftRight(newRight - right)
}

fun View.offsetRightTo(newRight: Int, minRight: Int, maxRight: Int) {
    offsetRight(newRight - right, minRight, maxRight)
}

//</editor-fold desc="offset扩展">

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

fun View?.visible(value: Boolean = true) {
    this?.visibility = if (value) View.VISIBLE else View.GONE
}

fun View?.gone(value: Boolean = true) {
    this?.visibility = if (value) View.GONE else View.VISIBLE
}

fun View?.invisible(value: Boolean = true) {
    this?.visibility = if (value) View.INVISIBLE else View.VISIBLE
}

fun View.save(canvas: Canvas, paint: Paint? = null): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), paint)
    } else {
        canvas.saveLayer(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            paint,
            Canvas.ALL_SAVE_FLAG
        )
    }
}

fun Canvas.clear(rect: Rect? = null) {
    if (rect == null) {
        drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        //canvas.drawColor(Color.TRANSPARENT, Mode.MULTIPLY)
    } else {
        val clearPaint = Paint()
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        drawRect(rect, clearPaint)
    }
}

//</editor-fold desc="draw相关扩展">

//<editor-fold desc="回调扩展">

/**确保[View]具有可视大小, 用于执行动画*/
fun View.doAnimate(action: View.() -> Unit) {
    if (measuredHeight <= 0 || measuredWidth <= 0) {
        doOnPreDraw {
            it.action()
        }
        postInvalidateOnAnimation()
    } else {
        action()
    }
}

/**[androidx/core/view/View.kt:79]
 *
 * [androidx.core.view.ViewKt.doOnPreDraw]
 * */
@Deprecated("比系统的要卡, 推荐使用系统的")
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

//<editor-fold desc="其他">

fun View.getChildOrNull(index: Int): View? {
    return if (this is ViewGroup) {
        this.getChildOrNull(index)
    } else {
        this
    }
}

fun CompoundButton.isCheckedAndEnable() = isChecked && isEnabled

/**API 17*/
fun View.isLayoutRtl() =
    ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL

/**[androidx.appcompat.widget.TooltipPopup.getAppRootView]*/
fun View.appRootView(): View {
    val rootView: View = rootView
    val lp = rootView.layoutParams
    if (lp is WindowManager.LayoutParams && (lp.type == WindowManager.LayoutParams.TYPE_APPLICATION)) {
        // This covers regular app windows and Dialog windows.
        return rootView
    }
    // For non-application window types (such as popup windows) try to find the main app window
    // through the context.
    var context: Context? = context
    while (context is ContextWrapper) {
        context = if (context is Activity) {
            return context.window.decorView
        } else {
            context.baseContext
        }
    }
    // Main app window not found, fall back to the anchor's root view. There is no guarantee
    // that the tooltip position will be computed correctly.
    return rootView
}

/**设置控件的长按提示文本
 * 可以通过[android.view.View.performLongClick]主动触发显示提示弹窗*/
fun View.tooltipText(text: CharSequence?) {
    TooltipCompat.setTooltipText(this, text)
    //performLongClick()
}

//</editor-fold desc="其他">