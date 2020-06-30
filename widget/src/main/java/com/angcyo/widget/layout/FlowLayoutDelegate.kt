package com.angcyo.widget.layout

import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.GravityCompat
import com.angcyo.widget.R
import com.angcyo.widget.base.exactly
import kotlin.math.max

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/07
 */

class FlowLayoutDelegate : LayoutDelegate() {

    var _allViews = mutableListOf<List<View>>() //保存所有行的所有View
    var _lineHeight = mutableListOf<Int>() //保存每一行的行高

    /** 每一行最多多少个, 强制限制. -1, 不限制. 大于0生效 */
    var maxCountLine: Int by RequestLayoutDelegateProperty(-1)

    /** 每一行的Item等宽 */
    var itemEquWidth: Boolean by RequestLayoutDelegateProperty(false)

    /**配合[itemEquWidth]使用, 开启仅支持单行样式*/
    var singleLine: Boolean by RequestLayoutDelegateProperty(false)

    /** item之间, 横竖向间隔. */
    var itemHorizontalSpace: Int by RequestLayoutDelegateProperty(0)

    var itemVerticalSpace: Int by RequestLayoutDelegateProperty(0)

    /**布局方式, 相对于一行中*/
    var lineGravity = Gravity.LEFT or Gravity.CENTER_VERTICAL

    override fun initAttribute(view: View, attributeSet: AttributeSet?) {
        this.delegateView = view

        val array =
            delegateView.context.obtainStyledAttributes(
                attributeSet,
                R.styleable.FlowLayoutDelegate
            )
        maxCountLine =
            array.getInt(R.styleable.FlowLayoutDelegate_r_flow_max_line_child_count, maxCountLine)
        itemEquWidth =
            array.getBoolean(R.styleable.FlowLayoutDelegate_r_flow_equ_width, itemEquWidth)
        singleLine =
            array.getBoolean(R.styleable.FlowLayoutDelegate_r_flow_single_line, singleLine)
        itemHorizontalSpace = array.getDimensionPixelOffset(
            R.styleable.FlowLayoutDelegate_r_flow_item_horizontal_space,
            itemHorizontalSpace
        )
        itemVerticalSpace = array.getDimensionPixelOffset(
            R.styleable.FlowLayoutDelegate_r_flow_item_vertical_space,
            itemVerticalSpace
        )
        array.recycle()

        //获取系统属性值
        for (i in 0 until (attributeSet?.attributeCount ?: 0)) {
            val name = attributeSet!!.getAttributeName(i)
            if ("gravity" == name) {
                lineGravity = attributeSet.getAttributeIntValue(i, lineGravity)
            }
        }

        if (delegateView is LinearLayout) {
            (delegateView as LinearLayout).orientation = LinearLayout.HORIZONTAL
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int): IntArray {
        val measureWidthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val measureWidthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val measureHeightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        val measureHeightMode = View.MeasureSpec.getMode(heightMeasureSpec)

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
        val viewAvailableWidth = measureWidthSize - paddingLeft - paddingRight
        val count = childCount

        var singleLineChildWidthMeasureSpec = 0
        if (itemEquWidth && singleLine) {
            //单行模式下, 等宽测量模式

            var useWidth = paddingLeft + paddingRight
            var visibleCount = 0
            for (i in 0 until count) {
                val child = getChildAt(i)
                if (child == null || child.visibility == View.GONE) {
                    continue
                }
                visibleCount++
                val lp: ViewGroup.MarginLayoutParams =
                    child.layoutParams as ViewGroup.MarginLayoutParams
                useWidth += lp.leftMargin + lp.rightMargin
            }

            if (visibleCount > 0) {
                useWidth += itemHorizontalSpace * (visibleCount - 1)
            }

            singleLineChildWidthMeasureSpec = exactly((measureWidthSize - useWidth) / visibleCount)
        }

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child == null || child.visibility == View.GONE) {
                continue
            }
            val params = child.layoutParams as LinearLayout.LayoutParams
            if (itemEquWidth) {
                if (singleLine) {
                    val lp = child.layoutParams
                    val childWidthMeasureSpec = singleLineChildWidthMeasureSpec
                    val childHeightMeasureSpec = ViewGroup.getChildMeasureSpec(
                        heightMeasureSpec,
                        paddingTop + paddingBottom,
                        lp.height
                    )
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
                } else {
                    measureChild(
                        child,
                        View.MeasureSpec.makeMeasureSpec(
                            measureWidthSize,
                            View.MeasureSpec.AT_MOST
                        ),
                        heightMeasureSpec
                    )
                }
            } else {
                if (params.weight > 0) {
                    //支持[weight]属性
                    child.measure(
                        exactly(((measureWidthSize - params.leftMargin - params.rightMargin - paddingLeft - paddingRight) * params.weight).toInt()),
                        heightMeasureSpec
                    )
                } else {
                    measureChild(child, widthMeasureSpec, heightMeasureSpec)
                }
            }
            childWidth = child.measuredWidth + params.leftMargin + params.rightMargin
            childHeight = child.measuredHeight + params.topMargin + params.bottomMargin
            val lineViewSize = lineViews.size
            //本次追加 child后 , 需要的宽度
            val needWidth = lineWidth + childWidth + itemHorizontalSpace
            if (needWidth > viewAvailableWidth || maxCountLine > 0 && lineViewSize == maxCountLine) { //需要换新行
                if (itemEquWidth) { //margin,padding 消耗的宽度
                    childWidth = measureLineEquWidth(
                        lineViews,
                        measureWidthSize,
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
            measureLineEquWidth(lineViews, measureWidthSize, heightMeasureSpec)
        }
        width += paddingLeft + paddingRight
        height += paddingTop + paddingBottom

        return intArrayOf(
            max(
                if (measureWidthMode == View.MeasureSpec.AT_MOST || measureWidthMode == View.MeasureSpec.UNSPECIFIED) width else measureWidthSize,
                minimumWidth
            ),
            max(
                if (measureHeightMode == View.MeasureSpec.AT_MOST || measureHeightMode == View.MeasureSpec.UNSPECIFIED) height else measureHeightSize,
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
        val lineViewParams = firstChild.layoutParams as LinearLayout.LayoutParams
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
                    lineView.layoutParams as LinearLayout.LayoutParams; consumeWidth += lineViewParams.leftMargin + lineViewParams.rightMargin
            }
            lineChildWidth = (viewWidth - consumeWidth) / lineViewSize
        }
        for (j in 0 until lineViewSize) {
            val lineView = lineViews[j]
            val lineViewParams = lineView.layoutParams as LinearLayout.LayoutParams
            lineView.measure(
                View.MeasureSpec.makeMeasureSpec(lineChildWidth, View.MeasureSpec.EXACTLY),
                ViewGroup.getChildMeasureSpec(heightMeasureSpec, 0, lineViewParams.height)
            )
        }
        return lineChildWidth
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var top = paddingTop //开始布局子view的 top距离
        var left = _lineLeft(0) //开始布局子view的 left距离
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
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight

                val params = child.layoutParams as LinearLayout.LayoutParams
                if (params.gravity == -1) {
                    params.gravity = lineGravity
                }
                val childLeft = left + params.leftMargin

                val childTop = when (params.gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                    Gravity.CENTER_VERTICAL -> top + lineHeight / 2 - childHeight / 2 + params.topMargin
                    Gravity.BOTTOM -> top + lineHeight - params.bottomMargin - childHeight
                    else -> top + params.topMargin
                }

                //不需要加上 params.rightMargin,
                val childRight = childLeft + childWidth
                //不需要加上 params.bottomMargin, 因为在 onMeasure , 中已经加在了 lineHeight 中
                val childBottom = childTop + childHeight
                child.layout(childLeft, childTop, childRight, childBottom)
                //因为在 这里添加了;
                left += childWidth + params.leftMargin + params.rightMargin + itemHorizontalSpace
            }
            left = _lineLeft(i + 1)
            top += lineHeight + itemVerticalSpace
        }
    }

    fun _lineLeft(line: Int): Int {
        val layoutDirection = 0
        val absoluteGravity = GravityCompat.getAbsoluteGravity(lineGravity, layoutDirection)

        //这一行 总共多少个view
        val lineView = _allViews.getOrNull(line)

        //这一行view + space 的宽度
        val lineViewWidth =
            lineView?.let { it.sumBy { it.measuredWidth } + (it.size - 1) * itemHorizontalSpace }
                ?: 0

        return when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
            Gravity.CENTER_HORIZONTAL -> delegateView.measuredWidth / 2 - lineViewWidth / 2 + paddingLeft - paddingRight
            Gravity.RIGHT -> delegateView.measuredWidth - paddingRight - lineViewWidth
            else -> paddingLeft
        }
    }
}