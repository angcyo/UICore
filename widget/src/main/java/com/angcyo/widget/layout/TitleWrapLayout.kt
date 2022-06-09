package com.angcyo.widget.layout

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.GravityCompat
import com.angcyo.library.ex.getStatusBarHeight
import com.angcyo.widget.R
import com.angcyo.widget.base.exactly
import com.angcyo.widget.base.getSize

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class TitleWrapLayout(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet) {

    private val DEFAULT_CHILD_GRAVITY = Gravity.TOP or Gravity.START

    /**强制加入状态栏的高度*/
    var forceFitStatusBar = false

    init {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.TitleWrapLayout)
        forceFitStatusBar = typedArray.getBoolean(
            R.styleable.TitleWrapLayout_r_force_fit_status_bar,
            forceFitStatusBar
        )
        typedArray.recycle()
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return initLayoutParams(super.generateDefaultLayoutParams())
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return initLayoutParams(super.generateLayoutParams(attrs))
    }

    fun initLayoutParams(lp: LayoutParams): LayoutParams {
        return lp.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && topMargin <= 0) {
                //topMargin = getStatusBarHeight()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = heightMeasureSpec.getSize()
        val statusBarHeight = getExcludeTopHeight()

        if (layoutParams.height == -1) {
            super.onMeasure(widthMeasureSpec, exactly(size - statusBarHeight))
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
        setMeasuredDimension(measuredWidth, measuredHeight + statusBarHeight)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        layoutChildren(left, top, right, bottom, false)
    }

    //copy from FrameLayout
    fun layoutChildren(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        forceLeftGravity: Boolean
    ) {
        val excludeTopHeight = getExcludeTopHeight()
        val count = childCount
        val parentLeft: Int = paddingLeft
        val parentRight: Int = right - left - paddingRight
        val parentTop: Int = paddingTop + excludeTopHeight
        val parentBottom: Int = bottom - top - paddingBottom
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                val lp = child.layoutParams as LayoutParams
                val width = child.measuredWidth
                val height = child.measuredHeight
                var childLeft: Int
                var childTop: Int
                var gravity = lp.gravity
                if (gravity == -1) {
                    gravity = DEFAULT_CHILD_GRAVITY
                }
                val layoutDirection =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        layoutDirection
                    } else {
                        0
                    }
                val absoluteGravity = GravityCompat.getAbsoluteGravity(gravity, layoutDirection)
                val verticalGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK

                childLeft = when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                    Gravity.CENTER_HORIZONTAL -> parentLeft + (parentRight - parentLeft - width) / 2 +
                            lp.leftMargin - lp.rightMargin
                    Gravity.RIGHT -> {
                        if (!forceLeftGravity) {
                            parentRight - width - lp.rightMargin
                        } else {
                            parentLeft + lp.leftMargin
                        }
                    }
                    Gravity.LEFT -> parentLeft + lp.leftMargin
                    else -> parentLeft + lp.leftMargin
                }

                childTop = when (verticalGravity) {
                    Gravity.TOP -> parentTop + lp.topMargin
                    Gravity.CENTER_VERTICAL -> parentTop + (parentBottom - parentTop - height) / 2 +
                            lp.topMargin - lp.bottomMargin
                    Gravity.BOTTOM -> parentBottom - height - lp.bottomMargin
                    else -> parentTop + lp.topMargin
                }
                child.layout(childLeft, childTop, childLeft + width, childTop + height)
            }
        }
    }

    val _location = intArrayOf(0, 0)

    /**当在进行属性动画时, [getLocationOnScreen] 获取到的数据不准确.
     * 此时可以使用[forceFitStatusBar]属性强制设置*/
    open fun getExcludeTopHeight(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (forceFitStatusBar) {
                return getStatusBarHeight()
            } else {
                getLocationOnScreen(_location)
                if (_location[1] <= 0) {
                    //布局在状态栏
                    return getStatusBarHeight()
                }
            }
        }
        //不考虑状态栏高度
        return 0
    }
}