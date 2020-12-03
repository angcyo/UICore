package com.angcyo.widget.layout

import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020-03-07
 */
abstract class LayoutDelegate {

    lateinit var delegateView: View

    val paddingLeft: Int get() = delegateView.paddingLeft
    val paddingTop: Int get() = delegateView.paddingTop
    val paddingRight: Int get() = delegateView.paddingRight
    val paddingBottom: Int get() = delegateView.paddingBottom

    val minimumWidth: Int get() = delegateView.minimumWidth
    val minimumHeight: Int get() = delegateView.minimumHeight

    val childCount: Int
        get() = if (delegateView is ViewGroup) (delegateView as ViewGroup).childCount else 0

    abstract fun initAttribute(view: View, attributeSet: AttributeSet?)

    //<editor-fold desc="核心代理方法">

    open fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int): IntArray {
        return intArrayOf(-1, -1)
    }

    open fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

    }

    open fun draw(canvas: Canvas) {

    }

    open fun onDraw(canvas: Canvas) {

    }

    //</editor-fold desc="核心代理方法">

    //<editor-fold desc="兼容方法">

    fun getChildAt(index: Int): View? {
        return if (delegateView is ViewGroup) (delegateView as ViewGroup).getChildAt(index) else null
    }

    fun measureChild(child: View, parentWidthMeasureSpec: Int, parentHeightMeasureSpec: Int) {
        val lp = child.layoutParams
        val childWidthMeasureSpec = ViewGroup.getChildMeasureSpec(
            parentWidthMeasureSpec,
            paddingLeft + paddingRight, lp.width
        )
        val childHeightMeasureSpec = ViewGroup.getChildMeasureSpec(
            parentHeightMeasureSpec,
            paddingTop + paddingBottom, lp.height
        )
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    //</editor-fold desc="兼容方法">

}