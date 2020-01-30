package com.angcyo.widget.layout

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.angcyo.widget.R
import com.angcyo.widget.base.RequestLayoutProperty
import kotlin.math.max

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/30
 */
class DslFlowLayout(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    var _allViews = mutableListOf<List<View>>() //保存所有行的所有View
    var _lineHeight = mutableListOf<Int>() //保存每一行的行高

    /** 每一行最多多少个, 强制限制. -1, 不限制. 大于0生效 */
    var maxCountLine: Int by RequestLayoutProperty(-1)

    /** 每一行的Item等宽 */
    var itemEquWidth: Boolean by RequestLayoutProperty(false)

    /** item之间, 横竖向间隔. */
    var itemHorizontalSpace: Int by RequestLayoutProperty(0)

    var itemVerticalSpace: Int by RequestLayoutProperty(0)

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.DslFlowLayout)
        maxCountLine =
            array.getInt(R.styleable.DslFlowLayout_r_flow_max_line_child_count, maxCountLine)
        itemEquWidth = array.getBoolean(R.styleable.DslFlowLayout_r_flow_equ_width, itemEquWidth)
        itemHorizontalSpace = array.getDimensionPixelOffset(
            R.styleable.DslFlowLayout_r_flow_item_horizontal_space,
            itemHorizontalSpace
        )
        itemVerticalSpace = array.getDimensionPixelOffset(
            R.styleable.DslFlowLayout_r_flow_item_vertical_space,
            itemVerticalSpace
        )
        array.recycle()
        orientation = HORIZONTAL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val measureWidth = MeasureSpec.getSize(widthMeasureSpec)
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val measureHeight = MeasureSpec.getSize(heightMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        var width = 0
        var height = 0
        var lineWidth = 0
        var lineHeight = 0
        var childWidth = 0
        var childHeight = 0
        _allViews.clear()
        _lineHeight.clear()

        var lineViews = mutableListOf<View>()

        //视图可用空间
        val viewAvailableWidth = measureWidth - paddingLeft - paddingRight
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) {
                continue
            }
            if (itemEquWidth) {
                measureChild(
                    child,
                    MeasureSpec.makeMeasureSpec(measureWidth, MeasureSpec.AT_MOST),
                    heightMeasureSpec
                )
            } else {
                measureChild(child, widthMeasureSpec, heightMeasureSpec)
            }
            val params = child.layoutParams as LayoutParams
            childWidth = child.measuredWidth + params.leftMargin + params.rightMargin
            childHeight = child.measuredHeight + params.topMargin + params.bottomMargin
            val lineViewSize = lineViews.size
            //本次追加 child后 , 需要的宽度
            val needWidth = lineWidth + childWidth + itemHorizontalSpace
            if (needWidth > viewAvailableWidth || maxCountLine > 0 && lineViewSize == maxCountLine) { //需要换新行
                if (itemEquWidth) { //margin,padding 消耗的宽度
                    childWidth = measureLineEquWidth(
                        lineViews,
                        measureWidth,
                        heightMeasureSpec
                    ) + params.leftMargin + params.rightMargin
                    var maxChildHeight = 0
                    if (lineViews.isEmpty()) {
                        maxChildHeight = childHeight
                    } else {
                        for (j in lineViews.indices) {
                            val childAt = lineViews[j]
                            maxChildHeight = max(maxChildHeight, childAt.measuredHeight)
                        }
                    }
                    lineHeight = maxChildHeight
                }
                width = max(width, lineWidth)
                height += lineHeight + itemVerticalSpace
                _lineHeight.add(lineHeight)
                _allViews.add(lineViews)
                lineWidth = childWidth
                lineHeight = childHeight
                lineViews = mutableListOf()
            } else {
                lineWidth += childWidth + itemHorizontalSpace
                lineHeight = max(childHeight, lineHeight)
            }
            lineViews.add(child)
            if (i == count - 1) {
                width = max(width, lineWidth)
                height += lineHeight
            }
        }
        _lineHeight.add(lineHeight)
        _allViews.add(lineViews)
        if (itemEquWidth) {
            measureLineEquWidth(lineViews, measureWidth, heightMeasureSpec)
        }
        width += paddingLeft + paddingRight
        height += paddingTop + paddingBottom
        setMeasuredDimension(
            max(
                if (modeWidth == MeasureSpec.AT_MOST || modeWidth == MeasureSpec.UNSPECIFIED) width else measureWidth,
                minimumWidth
            ),
            max(
                if (modeHeight == MeasureSpec.AT_MOST || modeHeight == MeasureSpec.UNSPECIFIED) height else measureHeight,
                minimumHeight
            )
        )
    }

    /**
     * 等宽并且maxCountLine>0 的时候, 计算 每个child的需要的宽度, margin 属性, 将使用每一行的第一个child
     */
    private fun measureEquChildWidth(lineViews: List<View>, viewWidth: Int): Int {
        if (lineViews.isEmpty()) {
            return viewWidth
        }
        var consumeWidth =
            paddingLeft + paddingRight + itemHorizontalSpace * max(maxCountLine - 1, 0)
        val firstChild = lineViews[0]
        val lineViewParams = firstChild.layoutParams as LayoutParams
        for (i in 0 until maxCountLine) {
            consumeWidth += lineViewParams.leftMargin + lineViewParams.rightMargin
        }
        val lineChildWidth: Int
        lineChildWidth = if (maxCountLine > 0) {
            (viewWidth - consumeWidth) / maxCountLine
        } else {
            viewWidth - consumeWidth
        }
        return lineChildWidth
    }

    private fun measureLineEquWidth(
        lineViews: List<View>,
        viewWidth: Int,
        heightMeasureSpec: Int
    ): Int {
        val lineViewSize = lineViews.size
        val lineChildWidth: Int
        if (maxCountLine > 0) {
            //等宽并且平分, 当lineViewSize没有达到maxCountLine数量时, 需要考虑计算方式.
            lineChildWidth = measureEquChildWidth(lineViews, viewWidth)
        } else {
            var consumeWidth =
                paddingLeft + paddingRight + itemHorizontalSpace * max(maxCountLine - 1, 0)
            for (j in 0 until lineViewSize) {
                val lineView = lineViews[j]
                val lineViewParams =
                    lineView.layoutParams as LayoutParams; consumeWidth += lineViewParams.leftMargin + lineViewParams.rightMargin
            }
            lineChildWidth = (viewWidth - consumeWidth) / lineViewSize
        }
        for (j in 0 until lineViewSize) {
            val lineView = lineViews[j]
            val lineViewParams = lineView.layoutParams as LayoutParams
            lineView.measure(
                MeasureSpec.makeMeasureSpec(lineChildWidth, MeasureSpec.EXACTLY),
                ViewGroup.getChildMeasureSpec(heightMeasureSpec, 0, lineViewParams.height)
            )
        }
        return lineChildWidth
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount == 0) {
            super.onLayout(changed, l, t, r, b)
            return
        }
        var top = paddingTop //开始布局子view的 top距离
        var left = paddingLeft //开始布局子view的 left距离
        val lineNum = _allViews.size //行数
        var lineView: List<View>
        var lineHeight: Int
        for (i in 0 until lineNum) {
            lineView = _allViews[i]
            lineHeight = _lineHeight[i]
            for (j in lineView.indices) {
                val child = lineView[j]
                if (child.visibility == View.GONE) {
                    continue
                }
                val params = child.layoutParams as LayoutParams
                val ld = left + params.leftMargin
                val td = top + params.topMargin
                //不需要加上 params.rightMargin,
                val rd = ld + child.measuredWidth
                //不需要加上 params.bottomMargin, 因为在 onMeasure , 中已经加在了 lineHeight 中
                val bd = td + child.measuredHeight
                child.layout(ld, td, rd, bd)
                //因为在 这里添加了;
                left += child.measuredWidth + params.leftMargin + params.rightMargin + itemHorizontalSpace
            }
            left = paddingLeft
            top += lineHeight + itemVerticalSpace
        }
    }
}