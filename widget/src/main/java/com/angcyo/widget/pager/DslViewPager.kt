package com.angcyo.widget.pager

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.angcyo.widget.DslViewHolder

/**
 * 垂直滚动的ViewPager
 * https://github.com/kaelaela/VerticalViewPager
 *
 * https://github.com/xmuSistone/ViewpagerTransition
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */

open class DslViewPager : ViewPager {

    val dslPagerAdapter: DslPagerAdapter?
        get() = adapter as? DslPagerAdapter

    var _heightMeasureMode = MeasureSpec.EXACTLY
    var _orientation = LinearLayout.HORIZONTAL

    lateinit var _gestureDetectorCompat: GestureDetectorCompat

    /**最后一页继续滑动时回调*/
    var pagerEndListener: OnPagerEndListener? = null

    constructor(context: Context) : super(context) {
        initAttribute(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttribute(context, attrs)
    }

    private fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        if (_orientation == LinearLayout.HORIZONTAL) {
            setPageTransformer(true, FadeInOutPageTransformer())
        } else {
            setPageTransformer(true, DefaultVerticalTransformer())
        }

        //监听最后一页滚动
        _gestureDetectorCompat = GestureDetectorCompat(context, object : SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (pagerEndListener != null &&
                    adapter != null &&
                    currentItem == adapter!!.count - 1
                ) {
                    if (_orientation == LinearLayout.VERTICAL) {
                        if (velocityY < -1000) {
                            pagerEndListener?.onPagerFlingEnd()
                        }
                    } else {
                        if (velocityX < -1000) {
                            pagerEndListener?.onPagerFlingEnd()
                        }
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })

        //支持wrap_content测量模式
        addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                if (_heightMeasureMode != MeasureSpec.EXACTLY) {
                    post { requestLayout() }
                }
            }
        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        _heightMeasureMode = heightMode

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (heightMode == MeasureSpec.AT_MOST) {
            //支持高度的wrap_content
            if (childCount > currentItem) {
                val childAt = getChildAt(currentItem)
                measureChild(childAt, widthMeasureSpec, heightMeasureSpec)
                setMeasuredDimension(widthSize, childAt.measuredHeight + paddingLeft + paddingRight)
            }
        }
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        if (!isEnabled) {
            return true
        }
        try {
            return if (_orientation == LinearLayout.VERTICAL) {
                (_gestureDetectorCompat.onTouchEvent(ev) || super.onTouchEvent(swapTouchEvent(ev)))
            } else {
                _gestureDetectorCompat.onTouchEvent(ev) || super.onTouchEvent(ev)
            }
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
        }
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (!isEnabled) {
            return false
        }
        try {
            return if (_orientation == LinearLayout.VERTICAL)
                super.onInterceptTouchEvent(swapTouchEvent(ev))
            else
                super.onInterceptTouchEvent(ev)
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
        }
        return false
    }

    fun swapTouchEvent(event: MotionEvent?): MotionEvent? {
        if (event == null) {
            return null
        }
        val width = width.toFloat()
        val height = height.toFloat()
        val swappedX = event.y / height * width
        val swappedY = event.x / width * height
        event.setLocation(swappedX, swappedY)
        return event
    }

    override fun setAdapter(adapter: PagerAdapter?) {
        val oldAdapter = getAdapter()
        if (oldAdapter is RPagerAdapter) {
            oldAdapter.dslViewPager = null
            removeOnPageChangeListener(oldAdapter)
        }
        super.setAdapter(adapter)
        if (adapter is RPagerAdapter) {
            adapter.dslViewPager = this
            addOnPageChangeListener(adapter)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        adapter?.run {
            if (this is RPagerAdapter) {
                for (i in 0 until childCount) {
                    val child = getChildAt(i)
                    if (child != null && child.tag is DslViewHolder) {
                        val dslViewHolder = child.tag as DslViewHolder
                        val adapterPosition =
                            (child.layoutParams as? LayoutParams)?.adapterPosition ?: -1

                        if (adapterPosition != -1) {
                            destroyItem(this@DslViewPager, adapterPosition, dslViewHolder)
                        }
                    }
                }
            }
        }
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams()
    }

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    class LayoutParams : ViewPager.LayoutParams {

        var adapterPosition: Int = RecyclerView.NO_POSITION

        constructor() : super()

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    }

    interface OnPagerEndListener {
        /**
         * 最后一一页快速滚动
         */
        fun onPagerFlingEnd()
    }
}