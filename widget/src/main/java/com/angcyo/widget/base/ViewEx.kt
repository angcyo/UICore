package com.angcyo.widget.base

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.annotation.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.*
import androidx.core.widget.NestedScrollView
import androidx.core.widget.ScrollerCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.angcyo.component.shot
import com.angcyo.dsladapter.getViewRect
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.annotation.Implementation
import com.angcyo.library.component.pool.acquireTempRect
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.*
import com.angcyo.library.ex.ViewEx._tempArray
import com.angcyo.library.utils.getMember
import com.angcyo.widget.DslViewHolder
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

//---

/**[StaticLayout]*/
fun createStaticLayout(
    source: CharSequence,
    paint: TextPaint,
    width: Int,
    align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL // Layout.Alignment.ALIGN_OPPOSITE
): StaticLayout {
    val layout: StaticLayout
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        layout = StaticLayout.Builder.obtain(
            source,
            0,
            source.length,
            paint,
            width
        ).setAlignment(align).build()
    } else {
        layout = StaticLayout(
            source,
            0,
            source.length,
            paint,
            width,
            align,
            1f,
            0f,
            false
        )
    }
    return layout
}

//---

/**设置[Behavior]*/
fun View?.setBehavior(behavior: CoordinatorLayout.Behavior<*>?) {
    this?.layoutParams?.coordinatorParams {
        this.behavior = behavior
    }
}

fun View?.behavior(): CoordinatorLayout.Behavior<*>? {
    return (this?.layoutParams as? CoordinatorLayout.LayoutParams?)?.run { this.behavior }
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

fun View.mWOrMeasure(): Int {
    if (measuredWidth > 0) {
        return measuredWidth
    }
    measure(atMost(_screenWidth), atMost(_screenHeight))
    return measuredWidth
}

fun View.mHOrMeasure(): Int {
    if (measuredHeight > 0) {
        return measuredHeight
    }
    measure(atMost(_screenWidth), atMost(_screenHeight))
    return measuredHeight
}

fun Context.viewOf(
    @LayoutRes id: Int,
    parent: ViewGroup? = null,
    action: DslViewHolder.() -> Unit
): View {
    val view = LayoutInflater.from(this).inflate(id, parent)
    val viewHolder = view.dslViewHolder()
    action(viewHolder)
    return view
}

//<editor-fold desc="layoutParams扩展">

/**快速操作[LayoutParams]*/
fun View.updateMarginParams(config: ViewGroup.MarginLayoutParams.() -> Unit = {}): View {
    (this.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
        config()
        this@updateMarginParams.layoutParams = layoutParams
    }
    return this
}

fun View.updateFrameParams(config: FrameLayout.LayoutParams.() -> Unit = {}): View {
    (this.layoutParams as? FrameLayout.LayoutParams)?.apply {
        config()
        this@updateFrameParams.layoutParams = layoutParams
    }
    return this
}

fun View.updateCoordinatorParams(config: CoordinatorLayout.LayoutParams.() -> Unit = {}): View {
    (this.layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
        config()
        this@updateCoordinatorParams.layoutParams = layoutParams
    }
    return this
}

fun View.updateConstraintParams(config: ConstraintLayout.LayoutParams.() -> Unit = {}): View {
    (this.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
        config()
        this@updateConstraintParams.layoutParams = layoutParams
    }
    return this
}

//---

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
fun View?.clickIt(action: ((View) -> Unit)?) {
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

/**观察[View]短时间之内被点击的次数*/
fun View.watchClickCount(count: Int, action: () -> Unit) {
    var clickCount = 0
    var clickTime = System.currentTimeMillis()

    setOnClickListener {
        val nowTime = System.currentTimeMillis()
        if (nowTime - clickTime < 600) {
            clickCount++
        } else {
            clickCount = 1
        }
        clickTime = nowTime
        if (clickCount >= count) {
            clickCount = 1
            action()
        }
    }
}

//</editor-fold desc="事件扩展">

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

/**滚动到底部*/
fun NestedScrollView.scrollToEnd(end: Boolean = true) {
    post {
        if (end) {
            //滚到底部
            fullScroll(View.FOCUS_DOWN)
        } else {
            //滚到顶部
            fullScroll(ScrollView.FOCUS_UP)
        }
    }
}

fun HorizontalScrollView.scrollToEnd(end: Boolean = true) {
    post {
        if (end) {
            //滚到底部
            fullScroll(View.FOCUS_DOWN)
        } else {
            //滚到顶部
            fullScroll(ScrollView.FOCUS_UP)
        }
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

//<editor-fold desc="截图">

fun View.toBitmap(): Bitmap? = if (this is RecyclerView) {
    saveRecyclerViewBitmap()
} else {
    saveView()
}

/**
 * 保存View的截图
 */
fun View.saveView(): Bitmap? {
    if (!isLaidOut) {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        measure(measureSpec, measureSpec)
        layout(0, 0, measuredWidth, measuredHeight)
    }

    isDrawingCacheEnabled = true
    buildDrawingCache()
    val drawingCache = drawingCache
    val bitmap = drawingCache.copy(drawingCache.config ?: Bitmap.Config.ARGB_8888, false)
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
    cache = cache.copy(cache.config ?: Bitmap.Config.ARGB_8888, false)
    view.destroyDrawingCache()
    view.isDrawingCacheEnabled = false
    return cache
}

fun RecyclerView.saveRecyclerViewBitmap(bgColor: Int = Color.WHITE, fromIndex: Int = 0): Bitmap? {
    return saveRecyclerViewBitmap(bgColor, Int.MAX_VALUE, fromIndex)
}

fun RecyclerView.saveRecyclerViewBitmap(
    bgColor: Int = Color.WHITE,
    itemCount: Int/*需要截取多少个item*/,
    fromIndex: Int = 0
): Bitmap? {
    //        ImageUtils.save(bitmap, path, Bitmap.CompressFormat.PNG);
    return shotRecyclerView(bgColor, itemCount, fromIndex)
}

/**
 * RecyclerView截图
 * [bgColor] 背景颜色
 * [fromIndex] 从第一个item开始
 * [count] 多少个
 */
@Deprecated(message = "请使用DslRecyclerViewShot")
fun RecyclerView.shotRecyclerView(bgColor: Int, count: Int, fromIndex: Int = 0): Bitmap? {
    return shot {
        this.bgColor = bgColor
        this.fromIndex = fromIndex
        this.itemCount = count
    }
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

//<editor-fold desc="坐标">

@Implementation
private fun View.getVisibleRect() {
    val rect = acquireTempRect()
    //获取全局可见状态矩形坐标, 在浮窗中, 则以根视图大小为参考
    //浮窗中, 根视图获取到的矩形就是自身的[0,0 自身的宽高]
    getGlobalVisibleRect(rect)

    //去掉不可见位置时的矩形
    getLocalVisibleRect(rect)

    //视图左上角坐标, 相对于window的坐标, 全屏情况下会等于[getLocationOnScreen]
    getLocationInWindow(_tempArray)

    //视图左上角坐标, 相对于屏幕的坐标. (包含了状态的高度)
    getLocationOnScreen(_tempArray)

    rect.release()
}

/**获取[View]在屏幕中的矩形坐标*/
fun View.screenRect(rect: Rect = acquireTempRect()): Rect {
    getLocationOnScreen(_tempArray)
    val left = _tempArray[0]
    val top = _tempArray[1]
    rect.set(left, top, left + mW(), top + mH())
    return rect
}

//</editor-fold desc="坐标">
