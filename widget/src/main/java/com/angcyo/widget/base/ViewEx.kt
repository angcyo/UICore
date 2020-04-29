package com.angcyo.widget.base

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.LruCache
import android.util.TypedValue
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils.clamp
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChild2
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.core.widget.NestedScrollView
import androidx.core.widget.ScrollerCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.angcyo.dsladapter.getViewRect
import com.angcyo.library.ex.getStatusBarHeight
import com.angcyo.library.ex.loadDrawable
import com.angcyo.library.ex.remove
import com.angcyo.library.ex.undefined_res
import com.angcyo.library.utils.getMember
import com.angcyo.widget.base.ViewEx._tempRect
import com.angcyo.widget.edit.IEditDelegate
import com.angcyo.widget.edit.REditDelegate
import com.angcyo.widget.layout.ILayoutDelegate
import com.angcyo.widget.layout.RLayoutDelegate
import com.angcyo.widget.recycler.getLastVelocity

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

/**设置系统背景*/
fun View.setBgDrawable(drawable: Drawable?) {
    ViewCompat.setBackground(this, drawable)
}

/**设置r背景*/
fun View.setRBgDrawable(drawable: Drawable?) {
    layoutDelegate {
        bDrawable = drawable
    }
}

/**[RLayoutDelegate]*/
fun View?.layoutDelegate(action: RLayoutDelegate.() -> Unit) {
    if (this is ILayoutDelegate) {
        this.getCustomLayoutDelegate().action()
    }
}

/**[REditDelegate]*/
fun View?.editDelegate(action: REditDelegate.() -> Unit) {
    if (this is IEditDelegate) {
        this.getCustomEditDelegate().action()
    }
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
fun Any?.topCanScroll(): Boolean {
    return canChildScroll(-1)
}

/** View 底部是否还有可滚动的距离 */
fun Any?.bottomCanScroll(): Boolean {
    return canChildScroll(1)
}

fun Any?.canChildScroll(direction: Int, depth: Int = 5): Boolean {
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
    return this is View && this.canScrollVertically(direction)
}

//</editor-fold desc="scroll扩展">

//<editor-fold desc="事件扩展">

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

/**模拟点击事件,和直接[performClick]不同的是有效背景效果*/
fun View?.simulateClick(delay: Long = Anim.ANIM_DURATION) {
    this?.run {
        performClick()
        isPressed = true
        invalidate()
        postDelayed({
            invalidate()
            isPressed = false
        }, delay)
    }
}

//</editor-fold desc="事件扩展">

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

//<editor-fold desc="其他">

/**查找[RecyclerView]*/
fun View.findRecyclerView(
    predicate: (View) -> Boolean = {
        it is RecyclerView &&
                it.measuredWidth > this.measuredWidth / 2 &&
                it.measuredHeight > this.measuredHeight / 2
    }
): RecyclerView? {
    return findView(predicate) as? RecyclerView
}

/**[NestedScrollingChild]*/
fun View.findNestedScrollingChild(
    predicate: (View) -> Boolean = {
        it is NestedScrollingChild &&
                it.measuredWidth > this.measuredWidth / 2 &&
                it.measuredHeight > this.measuredHeight / 2
    }
): NestedScrollingChild? {
    return findView(predicate) as? NestedScrollingChild
}

/**查找指定的[View]*/
fun View.findView(isIt: (View) -> Boolean): View? {
    return when {
        isIt(this) -> this
        this is ViewPager -> {
            var result: View? = null
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child.left >= scrollX &&
                    child.top >= scrollY &&
                    child.right <= scrollX + measuredWidth &&
                    child.bottom <= scrollY + measuredHeight
                ) {
                    result = child
                }
            }

            if (result != null) {
                result.findView(isIt)
            } else {
                if (isIt(this)) {
                    this
                } else {
                    null
                }
            }
        }
        this is ViewGroup -> {
            var result: View? = null
            for (i in 0 until childCount) {
                val childAt = getChildAt(i)
                result = childAt.findView(isIt)
                if (result != null) {
                    break
                }
            }
            result
        }
        else -> null
    }
}

fun View?.getLastVelocity(): Float {
    var currVelocity = 0f
    try {
        when (this) {
            is RecyclerView -> currVelocity = this.getLastVelocity()
            is NestedScrollView -> {
                val mScroller = this.getMember(NestedScrollView::class.java, "mScroller")
                currVelocity = mScroller.getCurrVelocity()
            }
            is ScrollView -> {
                val mScroller = this.getMember(ScrollView::class.java, "mScroller")
                currVelocity = mScroller.getCurrVelocity()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return currVelocity
}

fun Any?.getCurrVelocity(): Float {
    return when (this) {
        is OverScroller -> currVelocity
        is ScrollerCompat -> currVelocity
        else -> {
            0f
        }
    }
}

fun Any?.fling(velocityX: Int, velocityY: Int) {
    when (this) {
        is RecyclerView -> fling(velocityX, velocityY)
        is NestedScrollView -> fling(velocityY)
        is ScrollView -> fling(velocityY)
    }
}

fun Any?.stopScroll() {
    if (this is NestedScrollingChild2) {
        this.stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
    } else if (this is NestedScrollingChild) {
        this.stopNestedScroll()
    }

    if (this is RecyclerView) {
        this.stopScroll()
    } else if (this is NestedScrollView) {
        val mScroller = this.getMember(NestedScrollView::class.java, "mScroller")
        if (mScroller is OverScroller) {
            mScroller.abortAnimation()
        } else if (mScroller is ScrollerCompat) {
            mScroller.abortAnimation()
        }
    }
}

fun View.getChildOrNull(index: Int): View? {
    return if (this is ViewGroup) {
        this.getChildOrNull(index)
    } else {
        this
    }
}

/**获取[View]在指定[parent]中的矩形坐标*/
fun View.getLocationInParent(parentView: View? = null, result: Rect = Rect()): Rect {
    val parent: View? = parentView ?: (parent as? View)

    if (parent == null) {
        getViewRect(result)
    } else {
        result.set(0, 0, 0, 0)
        if (this != parent) {
            fun doIt(view: View, parent: View, rect: Rect) {
                val viewParent = view.parent
                if (viewParent is View) {
                    rect.left += view.left
                    rect.top += view.top
                    if (viewParent != parent) {
                        doIt(viewParent, parent, rect)
                    }
                }
            }
            doIt(this, parent, result)
        }
        result.right = result.left + this.measuredWidth
        result.bottom = result.top + this.measuredHeight
    }

    return result
}

/**
 * Pad this view with the insets provided by the device cutout (i.e. notch)
 * 用缺口的大小, 填充视图
 *  */
@RequiresApi(Build.VERSION_CODES.P)
fun View.padWithDisplayCutout() {

    /** Helper method that applies padding from cutout's safe insets */
    fun doPadding(cutout: DisplayCutout) = setPadding(
        cutout.safeInsetLeft,
        cutout.safeInsetTop,
        cutout.safeInsetRight,
        cutout.safeInsetBottom
    )

    // Apply padding using the display cutout designated "safe area"
    rootWindowInsets?.displayCutout?.let { doPadding(it) }

    // Set a listener for window insets since view.rootWindowInsets may not be ready yet
    setOnApplyWindowInsetsListener { _, insets ->
        insets.displayCutout?.let { doPadding(it) }
        insets
    }
}

//</editor-fold desc="其他">

//<editor-fold desc="截图">

/**
 * 保存View的截图
 */
fun View.saveView(): Bitmap? {
    isDrawingCacheEnabled = true
    buildDrawingCache()
    val drawingCache = drawingCache
    val bitmap = drawingCache.copy(drawingCache.config, false)
    destroyDrawingCache()
    isDrawingCacheEnabled = false
    return bitmap
}

/**
 * 保存xml对应的截图
 */
fun Context.saveView(@LayoutRes layoutId: Int, init: (View) -> Unit): Bitmap? {
    val view = LayoutInflater.from(this).inflate(layoutId, FrameLayout(this), false)
    init(view)
    val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    view.measure(measureSpec, measureSpec)
    view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    view.isDrawingCacheEnabled = true
    view.buildDrawingCache()
    var cache = view.drawingCache
    cache = cache.copy(cache.config, false)
    view.destroyDrawingCache()
    view.isDrawingCacheEnabled = false
    return cache
}

fun RecyclerView.saveRecyclerViewBitmap(bgColor: Int): Bitmap? {
    return saveRecyclerViewBitmap(bgColor, Int.MAX_VALUE)
}

fun RecyclerView.saveRecyclerViewBitmap(bgColor: Int, itemCount: Int/*需要截取的前几个item*/): Bitmap? {
    //        ImageUtils.save(bitmap, path, Bitmap.CompressFormat.PNG);
    return shotRecyclerView(bgColor, itemCount)
}

/**
 * RecyclerView截图
 */
fun RecyclerView.shotRecyclerView(bgColor: Int, count: Int): Bitmap? {
    val adapter = adapter
    var bigBitmap: Bitmap? = null
    if (adapter != null) {
        val size = Math.min(count, adapter.itemCount)
        var height = 0
        val paint = Paint()
        var iHeight = 0
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

        // Use 1/8th of the available memory for this memory cache.
        val cacheSize = maxMemory / 8
        val bitmapCache =
            LruCache<String, Bitmap>(cacheSize)
        for (i in 0 until size) {
            val holder =
                adapter.createViewHolder(this, adapter.getItemViewType(i))
            adapter.onBindViewHolder(holder, i)
            holder.itemView.measure(
                View.MeasureSpec.makeMeasureSpec(
                    width,
                    View.MeasureSpec.EXACTLY
                ), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            holder.itemView.layout(
                0, 0, holder.itemView.measuredWidth,
                holder.itemView.measuredHeight
            )
            holder.itemView.isDrawingCacheEnabled = true
            holder.itemView.buildDrawingCache()
            val drawingCache = holder.itemView.drawingCache
            if (drawingCache != null) {
                bitmapCache.put(i.toString(), drawingCache)
            }
            height += holder.itemView.measuredHeight
        }
        bigBitmap = Bitmap.createBitmap(measuredWidth, height, Bitmap.Config.ARGB_8888)
        val bigCanvas = Canvas(bigBitmap)
        bigCanvas.drawColor(bgColor)
        val lBackground = background
        if (lBackground is ColorDrawable) {
            val lColor = lBackground.color
            bigCanvas.drawColor(lColor)
        }
        for (i in 0 until size) {
            val bitmap = bitmapCache[i.toString()]
            bigCanvas.drawBitmap(bitmap, 0f, iHeight.toFloat(), paint)
            iHeight += bitmap.height
            bitmap.recycle()
        }
    }
    return bigBitmap
}

/**
 * 将文本转成图片
 */
fun String.textToBitmap(context: Context): Bitmap? {
    val metrics = context.resources.displayMetrics
    val padding = (metrics.density * 4).toInt()
    val frameLayout = FrameLayout(context)
    frameLayout.setBackgroundColor(Color.WHITE)
    val textView = TextView(context)
    textView.text = this
    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8f)
    frameLayout.setPadding(padding, padding, padding, padding)
    frameLayout.addView(textView, ViewGroup.LayoutParams(-2, -2))
    frameLayout.measure(
        View.MeasureSpec.makeMeasureSpec(
            metrics.widthPixels,
            View.MeasureSpec.AT_MOST
        ),
        View.MeasureSpec.makeMeasureSpec(
            metrics.heightPixels,
            View.MeasureSpec.AT_MOST
        )
    )
    frameLayout.layout(0, 0, frameLayout.measuredWidth, frameLayout.measuredHeight)
    val bitmap = Bitmap.createBitmap(
        frameLayout.measuredWidth,
        frameLayout.measuredHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    frameLayout.draw(canvas)
    return bitmap
}

//</editor-fold desc="截图">
