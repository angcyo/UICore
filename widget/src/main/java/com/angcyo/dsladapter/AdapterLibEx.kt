package com.angcyo.dsladapter

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.Window
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/10/16
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
fun RecyclerView.eachChildRViewHolder(
    targetView: View? = null,/*指定目标, 则只回调目标前后的ViewHolder*/
    callback: (
        beforeViewHolder: DslViewHolder?,
        viewHolder: DslViewHolder,
        afterViewHolder: DslViewHolder?
    ) -> Unit
) {

    val childCount = childCount
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        val childViewHolder = findContainingViewHolder(child)

        childViewHolder?.let {

            //前一个child
            var beforeViewHolder: DslViewHolder? = null
            //后一个child
            var afterViewHolder: DslViewHolder? = null

            if (i >= 1) {
                beforeViewHolder = findContainingViewHolder(getChildAt(i - 1)) as DslViewHolder?
            }
            if (i < childCount - 1) {
                afterViewHolder = findContainingViewHolder(getChildAt(i + 1)) as DslViewHolder?
            }

            if (targetView != null) {
                if (targetView == child) {
                    callback.invoke(beforeViewHolder, it as DslViewHolder, afterViewHolder)
                    return
                }
            } else {
                callback.invoke(beforeViewHolder, it as DslViewHolder, afterViewHolder)
            }
        }
    }
}

/**
 * 获取View, 相对于手机屏幕的矩形
 * */
fun View.getViewRect(result: Rect = Rect()): Rect {
    var offsetX = 0
    var offsetY = 0

    //横屏, 并且显示了虚拟导航栏的时候. 需要左边偏移
    //只计算一次
    (context as? Activity)?.let {
        it.window.decorView.getGlobalVisibleRect(result)
        if (result.width() > result.height()) {
            //横屏了
            offsetX = navBarHeight(it)
        }
    }

    return getViewRect(offsetX, offsetY, result)
}

/**
 * 获取View, 相对于手机屏幕的矩形, 带皮阿尼一
 * */
fun View.getViewRect(offsetX: Int, offsetY: Int, result: Rect = Rect()): Rect {
    //可见位置的坐标, 超出屏幕的距离会被剃掉
    //getGlobalVisibleRect(r)
    val r2 = IntArray(2)
    //val r3 = IntArray(2)
    //相对于屏幕的坐标
    getLocationOnScreen(r2)
    //相对于窗口的坐标
    //getLocationInWindow(r3)

    val left = r2[0] + offsetX
    val top = r2[1] + offsetY

    result.set(left, top, left + measuredWidth, top + measuredHeight)
    return result
}

/**
 * 导航栏的高度(如果显示了)
 */
fun navBarHeight(context: Context): Int {
    var result = 0

    if (context is Activity) {
        val decorRect = Rect()
        val windowRect = Rect()

        context.window.decorView.getGlobalVisibleRect(decorRect)
        context.window.findViewById<View>(Window.ID_ANDROID_CONTENT)
            .getGlobalVisibleRect(windowRect)

        if (decorRect.width() > decorRect.height()) {
            //横屏
            result = decorRect.width() - windowRect.width()
        } else {
            //竖屏
            result = decorRect.bottom - windowRect.bottom
        }
    }

    return result
}

fun notNull(vararg anys: Any?, doIt: (Array<Any>) -> Unit) {
    var haveNull = false

    for (any in anys) {
        if (any == null) {
            haveNull = true
            break
        }
    }

    if (!haveNull) {
        doIt(anys as Array<Any>)
    }
}

fun Rect.clear() {
    set(0, 0, 0, 0)
}

fun Rect.set(rectF: RectF): Rect {
    set(rectF.left.toInt(), rectF.top.toInt(), rectF.right.toInt(), rectF.bottom.toInt())
    return this
}

/**快速创建网格布局*/
fun gridLayout(
    context: Context,
    dslAdapter: DslAdapter,
    spanCount: Int = 4,
    orientation: Int = RecyclerView.VERTICAL,
    reverseLayout: Boolean = false
): GridLayoutManager {
    return GridLayoutManager(
        context,
        spanCount,
        orientation,
        reverseLayout
    ).apply {
        dslSpanSizeLookup(dslAdapter)
    }
}

/**SpanSizeLookup*/
fun GridLayoutManager.dslSpanSizeLookup(recyclerView: RecyclerView): GridLayoutManager.SpanSizeLookup {
    //设置span size
    val spanCount = spanCount
    val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            val dslAdapter = recyclerView.adapter as? DslAdapter
            return when {
                dslAdapter?.isAdapterStatus() == true -> spanCount
                else -> dslAdapter?.getItemData(position)?.run {
                    if (itemSpanCount == -1) {
                        spanCount
                    } else {
                        itemSpanCount
                    }
                } ?: 1
            }
        }
    }
    this.spanSizeLookup = spanSizeLookup
    return spanSizeLookup
}

/**SpanSizeLookup*/
fun GridLayoutManager.dslSpanSizeLookup(dslAdapter: DslAdapter): GridLayoutManager.SpanSizeLookup {
    //设置span size
    val spanCount = spanCount
    val spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return when {
                dslAdapter.isAdapterStatus() -> spanCount
                else -> dslAdapter.getItemData(position)?.run {
                    if (itemSpanCount == -1) {
                        spanCount
                    } else {
                        itemSpanCount
                    }
                } ?: 1
            }
        }
    }
    this.spanSizeLookup = spanSizeLookup
    return spanSizeLookup
}

fun View.fullSpan(full: Boolean = true) {
    val layoutParams = layoutParams
    if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
        if (full != layoutParams.isFullSpan) {
            layoutParams.isFullSpan = true
            this.layoutParams = layoutParams
        }
    }
}

/**文本的高度*/
fun Paint.textHeight(): Float = descent() - ascent()

val FLAG_NO_INIT = -1

val FLAG_NONE = 0

val FLAG_ALL = ItemTouchHelper.LEFT or
        ItemTouchHelper.RIGHT or
        ItemTouchHelper.DOWN or
        ItemTouchHelper.UP

val FLAG_VERTICAL = ItemTouchHelper.DOWN or ItemTouchHelper.UP

val FLAG_HORIZONTAL = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT

