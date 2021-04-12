package com.angcyo.widget.layout.touch

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import com.angcyo.library.ex.abs
import com.angcyo.widget.R
import com.angcyo.widget.base.topCanScroll
import com.angcyo.widget.layout.ILayoutDelegate
import com.angcyo.widget.layout.RLayoutDelegate

/**
 * 下拉返回, 上拉全屏
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2017/10/16 17:06
 */
open class TouchBackLayout(context: Context, attributeSet: AttributeSet? = null) :
    TouchLayout(context, attributeSet), ILayoutDelegate {

    val layoutDelegate = RLayoutDelegate()

    /**是否激活下拉返回*/
    var enableTouchBack = false

    /**顶部留出多少空间, 用来实现半屏效果*/
    var offsetScrollTop = 0
        set(value) {
            field = value
            if (field != 0) {
                scrollTo(0, -offsetScrollTop)
            }
        }

    private var defaultOffsetScrollTop = 0
    private var isFling = false

    private var rLayoutWidth: String? = null
    private var rLayoutHeight: String? = null

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.TouchBackLayout)
        enableTouchBack =
            typedArray.getBoolean(R.styleable.TouchBackLayout_r_enable_touch_back, enableTouchBack)
        defaultOffsetScrollTop = typedArray.getDimensionPixelOffset(
            R.styleable.TouchBackLayout_r_touch_offset_scroll_top,
            defaultOffsetScrollTop
        )
        rLayoutWidth = typedArray.getString(R.styleable.TouchBackLayout_r_layout_width)
        rLayoutHeight = typedArray.getString(R.styleable.TouchBackLayout_r_layout_height)
        layoutDelegate.initAttribute(this, attributeSet)
        typedArray.recycle()
        offsetScrollTop = defaultOffsetScrollTop
    }

    override fun draw(canvas: Canvas) {
        layoutDelegate.maskLayout(canvas) {
            layoutDelegate.draw(canvas)
            super.draw(canvas)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val layoutWidthHeightSpec =
            layoutDelegate.layoutWidthHeightSpec(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(layoutWidthHeightSpec[0], layoutWidthHeightSpec[1])
        layoutDelegate.onMeasure(layoutWidthHeightSpec[0], layoutWidthHeightSpec[1])
    }

    override fun getCustomLayoutDelegate(): RLayoutDelegate {
        return layoutDelegate
    }

//    /**布局方式, 采用的是垂直方向的线性布局方式*/
//    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
//        //super.onLayout(changed, left, top, right, bottom)
//        var top = 0
//        for (i in 0 until childCount) {
//            val childAt = getChildAt(i)
//            childAt.layout(0, top, measuredWidth, top + childAt.measuredHeight)
//            top += childAt.measuredHeight
//        }
//        viewMaxHeight = top
//    }

    private var downY: Float = 0f

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val touchEvent = super.onInterceptTouchEvent(ev)
        if (enableTouchBack) {

            if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
                downY = ev.y
            } else if (ev.actionMasked == MotionEvent.ACTION_MOVE &&
                !isNestedAccepted &&
                scrollY != 0 &&
                (ev.y - downY).abs() > 10
            ) {
                return true
            }

            return touchEvent
        } else {
            return false
        }
    }

//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        orientationGestureDetector.onTouchEvent(event)
//        return super.onTouchEvent(event)
//    }

    /*touch up时的, 恢复操作*/
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val actionMasked = ev.actionMasked
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            isNestedAccepted = false
        }

        if (!enableTouchBack) {
            return super.dispatchTouchEvent(ev)
        }

        val dispatchTouchEvent = super.dispatchTouchEvent(ev)
        when (actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isFling = false
            }
            MotionEvent.ACTION_UP -> {
                if (offsetScrollTop.abs() > scrollY.abs()) {
                    offsetScrollTop = 0
                    scrollToDefault()
                } else {
                    if (!isFling) {
                        resetScroll()
                    }
                }
            }
        }
        return dispatchTouchEvent
    }

    /*外面的滚动处理*/
    override fun onScrollChange(orientation: ORIENTATION, distance: Float) {
        super.onScrollChange(orientation, distance)
        if (enableTouchBack && isVertical(orientation) && !isNestedAccepted) {
            scrollBy(0, calcConsumedDy(distance.toInt()))
        }
    }

    override fun onFlingChange(orientation: ORIENTATION, velocity: Float) {
        super.onFlingChange(orientation, velocity)
        if (isVertical(orientation) && !isNestedAccepted) {
            if (velocity > 1000) {
                isFling = true
                scrollToBack()
            } else if (velocity < -1000) {
                isFling = true
                scrollToDefault()
            }
        }
    }

    private var isNestedAccepted = false

    /*接收内嵌滚动*/
    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return enableTouchBack && nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedScrollAccepted(child: View?, target: View?, axes: Int) {
        super.onNestedScrollAccepted(child, target, axes)
        isNestedAccepted = true
    }

    override fun onStopNestedScroll(child: View?) {
        super.onStopNestedScroll(child)
    }

    /*内嵌滚动开始, 处理需要消耗的滚动距离*/
    override fun onNestedPreScroll(target: View?, dx: Int, dy: Int, consumed: IntArray) {
        super.onNestedPreScroll(target, dx, dy, consumed)
        if (dy < 0) {
            //手指向下滑动
            if (!target.topCanScroll()) {
                scrollBy(0, dy)
                consumed[1] = dy
            }
        } else if (dy > 0) {
            if (scrollY < 0) {
                val calcConsumedDy = calcConsumedDy(dy)
                scrollBy(0, calcConsumedDy)
                consumed[1] = calcConsumedDy
            }
        }
    }

    /*滚动的边界处理*/
    private fun calcConsumedDy(dy: Int): Int {
        var y = dy
        if (dy < 0) {

        } else {
            if (scrollY + dy > 0) {
                y = -scrollY
            }
        }
        return y
    }

    /**滚动到底部, 并返回*/
    fun scrollToBack(anim: Boolean = true) {
        if (anim) {
            startScrollY(-measuredHeight - scrollY)
            postInvalidate()
        } else {
            scrollTo(0, -measuredHeight)
        }
    }

    /**滚动到初始位置 (顶部)*/
    fun scrollToDefault(anim: Boolean = true) {
        if (anim) {
            startScrollY(-offsetScrollTop - scrollY)
            postInvalidate()
        } else {
            scrollTo(0, 0)
        }
    }

    /**滚动到最开始偏移位置, 如果没有偏移位置, 那么就是顶部*/
    fun scrollToDefaultOffset(anim: Boolean = true) {
        if (anim) {
            startScrollY(-scrollY - defaultOffsetScrollTop)
            postInvalidate()
        } else {
            scrollTo(0, -defaultOffsetScrollTop)
        }
    }

    /**重置位置*/
    fun resetScroll(anim: Boolean = true) {
        val scrollY = scrollY
        if (scrollY.abs() - offsetScrollTop >= (measuredHeight - offsetScrollTop) / 4) {
            //需要下滑返回
            scrollToBack(anim)
        } else {
            //恢复到初始化位置
            scrollToDefault(anim)
        }
    }

    override fun scrollTo(x: Int, y: Int) {
        if (enableTouchBack) {
            val oldY = scrollY
            super.scrollTo(x, y)
            onTouchBackListener?.onTouchBackListener(this, oldY.abs(), y.abs(), measuredHeight)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        overScroller.forceFinished(true)
    }

    var onTouchBackListener: OnTouchBackListener? = null

    interface OnTouchBackListener {
        fun onTouchBackListener(
            layout: TouchBackLayout,
            oldScrollY: Int /*已经做了abs处理, 确保是正数*/,
            scrollY: Int /*已经做了abs处理, 确保是正数*/,
            maxScrollY: Int /*允许滚动的最大距离, 当达到最大距离, 视为back*/
        )
    }
}