package com.angcyo.widget.layout

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.GravityCompat
import com.angcyo.library.ex.inflate
import com.angcyo.library.ex.size
import com.angcyo.widget.R
import com.angcyo.widget.base.atMost
import com.angcyo.widget.base.exactly
import kotlin.math.max

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/07
 */

class FlowLayoutDelegate : ClipLayoutDelegate() {

    val _allViews = mutableListOf<List<View>>() //保存所有行的所有View
    val _lineHeight = mutableListOf<Int>() //保存每一行的行高
    val _allVisibleViews = mutableListOf<View>() //保存所有可见的view

    /** 每一行最多多少个, 强制限制. -1, 不限制. 大于0生效 */
    var maxCountOnLine: Int = -1

    /**最大的行数*/
    var maxLineCount: Int = -1

    /** 每一行的Item等宽 */
    var itemEquWidth: Boolean = false

    /**当子Item数量在此范围内时,开启等宽,此属性优先级最高
     * [~3] 小于等于3个
     * [3~] 大于等于3个
     * [3~5] 3<= <=5
     * [itemEquWidth]
     * */
    var itemEquWidthCountRange: IntRange? = null

    /**配合[itemEquWidth]使用, 开启仅支持单行样式*/
    var singleLine: Boolean = false

    /** item之间, 横竖向间隔. */
    var itemHorizontalSpace: Int = 0

    var itemVerticalSpace: Int = 0

    /**布局方式, 相对于一行中*/
    var lineGravity = Gravity.LEFT or Gravity.CENTER_VERTICAL

    /**分割线的大小*/
    var dividerHorizontalSize = 1

    var dividerVerticalSize = 1

    /**横向分割线*/
    var dividerHorizontalDrawable: Drawable? = null

    /**纵向分割线*/
    var dividerVerticalDrawable: Drawable? = null

    override fun initAttribute(view: View, attributeSet: AttributeSet?) {
        super.initAttribute(view, attributeSet)

        val array = delegateView.context.obtainStyledAttributes(
            attributeSet,
            R.styleable.FlowLayoutDelegate
        )
        maxCountOnLine =
            array.getInt(R.styleable.FlowLayoutDelegate_r_flow_max_line_child_count, maxCountOnLine)
        maxLineCount = array.getInt(R.styleable.FlowLayoutDelegate_r_flow_max_line_count, -1)
        itemEquWidth =
            array.getBoolean(R.styleable.FlowLayoutDelegate_r_flow_equ_width, itemEquWidth)
        if (array.hasValue(R.styleable.FlowLayoutDelegate_r_flow_equ_width_count_range)) {
            val equWidthCountRangeString =
                array.getString(R.styleable.FlowLayoutDelegate_r_flow_equ_width_count_range)
            if (equWidthCountRangeString.isNullOrBlank()) {
                itemEquWidthCountRange = null
            } else {
                val rangeList = equWidthCountRangeString.split("~")
                if (rangeList.size() >= 2) {
                    val min = rangeList.getOrNull(0)?.toIntOrNull() ?: 0
                    val max = rangeList.getOrNull(1)?.toIntOrNull() ?: Int.MAX_VALUE
                    itemEquWidthCountRange = IntRange(min, max)
                } else {
                    val min = rangeList.getOrNull(0)?.toIntOrNull() ?: Int.MAX_VALUE
                    itemEquWidthCountRange = IntRange(min, Int.MAX_VALUE)
                }
            }
        }
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

        dividerHorizontalDrawable =
            array.getDrawable(R.styleable.FlowLayoutDelegate_r_flow_horizontal_divider)
        dividerVerticalDrawable =
            array.getDrawable(R.styleable.FlowLayoutDelegate_r_flow_vertical_divider)

        dividerHorizontalSize = array.getDimensionPixelOffset(
            R.styleable.FlowLayoutDelegate_r_flow_horizontal_divider_size,
            dividerHorizontalSize
        )
        dividerVerticalSize = array.getDimensionPixelOffset(
            R.styleable.FlowLayoutDelegate_r_flow_vertical_divider_size,
            dividerVerticalSize
        )

        //preview
        if (view.isInEditMode && view is ViewGroup) {
            val layoutId =
                array.getResourceId(
                    R.styleable.FlowLayoutDelegate_r_flow_preview_item_layout_id,
                    -1
                )
            val layoutCount =
                array.getInt(R.styleable.FlowLayoutDelegate_r_flow_preview_item_count, 3)
            if (layoutId != -1) {
                for (i in 0 until layoutCount) {
                    view.inflate(layoutId, true).let {
                        if (it is TextView) {
                            if (it.text.isNullOrEmpty()) {
                                it.text = "Item $i"
                            } else {
                                it.text = "${it.text}/$i"
                            }
                        }
                    }
                }
            }
        }

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
        _allVisibleViews.clear()

        var lineViews = mutableListOf<View>()

        //视图可用空间
        val viewAvailableWidth = measureWidthSize - paddingLeft - paddingRight
        val viewAvailableHeight = measureHeightSize - paddingTop - paddingBottom

        //child总数
        val count = childCount

        //可见child总数
        var visibleCount = 0
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child == null || child.visibility == View.GONE) {
                continue
            }
            _allVisibleViews.add(child)
            visibleCount++
        }

        //等宽模式
        val equWidth = itemEquWidth || itemEquWidthCountRange?.contains(visibleCount) == true

        var singleLineChildWidthMeasureSpec = 0
        if (equWidth && singleLine) {
            //单行模式下, 等宽测量模式

            var useWidth = paddingLeft + paddingRight
            for (i in 0 until visibleCount) {
                val child = _allVisibleViews[i]
                val lp: ViewGroup.MarginLayoutParams =
                    child.layoutParams as ViewGroup.MarginLayoutParams
                useWidth += lp.leftMargin + lp.rightMargin
            }

            if (visibleCount > 0) {
                useWidth += itemHorizontalSpace * (visibleCount - 1)
            }

            singleLineChildWidthMeasureSpec = exactly((measureWidthSize - useWidth) / visibleCount)
        }

        for (i in 0 until visibleCount) {
            val child = _allVisibleViews[i]
            val params = child.layoutParams as LinearLayout.LayoutParams
            if (equWidth) {
                if (singleLine) {
                    val lp = child.layoutParams
                    val childWidthMeasureSpec = singleLineChildWidthMeasureSpec
                    val childHeightMeasureSpec = ViewGroup.getChildMeasureSpec(
                        heightMeasureSpec,
                        paddingTop + paddingBottom,
                        lp.height
                    )
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
                } else if (maxCountOnLine > 0) {
                    //需要平分宽度
                    val consumeWidth = childConsumeWidth()
                    val lineChildWidth = (measureWidthSize - consumeWidth) / maxCountOnLine
                    measureChild(
                        child, exactly(lineChildWidth), heightMeasureSpec
                    )
                } else {
                    measureChild(child, atMost(measureWidthSize), heightMeasureSpec)
                }
            } else {
                var childWidthMeasureSpec = widthMeasureSpec
                var childHeightMeasureSpec = heightMeasureSpec

                if (params.weight > 0) {
                    //支持[weight]属性
                    var weightSize =
                        measureWidthSize - params.leftMargin - params.rightMargin - paddingLeft - paddingRight
                    if (maxCountOnLine > 0) {
                        weightSize -= itemHorizontalSpace * (maxCountOnLine - 1)
                    }
                    childWidthMeasureSpec = exactly((weightSize * params.weight).toInt())
                } else {
                    if (params.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                        var childWidth = max(width, lineWidth)
                        if (childWidth <= 0) {
                            childWidth = viewAvailableWidth
                        }
                        childWidthMeasureSpec = exactly(childWidth + paddingLeft + paddingRight)
                    }
                }
                if (params.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                    childHeightMeasureSpec = exactly(max(height, viewAvailableHeight))
                } else if (params.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                    childHeightMeasureSpec =
                        atMost(if (viewAvailableHeight == 0) measureWidthSize else viewAvailableHeight)
                } else {
                    childHeightMeasureSpec = exactly(params.height)
                }
                measureChild(child, childWidthMeasureSpec, childHeightMeasureSpec)
            }
            childWidth = child.measuredWidth + params.leftMargin + params.rightMargin
            childHeight = child.measuredHeight + params.topMargin + params.bottomMargin
            val lineViewSize = lineViews.size
            //一行是否超过最大item数
            val outOfLineCount = maxCountOnLine > 0 && lineViewSize == maxCountOnLine
            val nextOutOfLineCount = maxCountOnLine > 0 && (lineViewSize + 1) == maxCountOnLine

            //本次追加 child后 , 需要的宽度
            var needWidth = lineWidth + childWidth
            if (nextOutOfLineCount) {
                //一行超出限制
            } else if (i == visibleCount - 1) {
                //最后一个
            } else {
                needWidth += itemHorizontalSpace
            }

            if (needWidth > viewAvailableWidth || outOfLineCount) { //需要换新行
                if (equWidth) { //margin,padding 消耗的宽度
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

                if (maxLineCount > 0 && _allViews.size >= maxLineCount) {
                    break
                }
            } else {
                lineWidth = needWidth
                lineHeight = max(childHeight, lineHeight)
            }
            lineViews.add(child)

            if (i == visibleCount - 1) {
                width = max(width, lineWidth)
                height += lineHeight
            }
        }

        if (maxLineCount > 0 && _allViews.size >= maxLineCount) {
            //行数超限, 已经收尾了操作
        } else {
            _lineHeight.add(lineHeight)
            _allViews.add(lineViews)
            if (equWidth) {
                if (!singleLine) {
                    measureLineEquWidth(lineViews, measureWidthSize, heightMeasureSpec)
                }
            }
        }

        width += paddingLeft + paddingRight
        height += paddingTop + paddingBottom

        _allVisibleViews.clear()

        val w = if (measureWidthMode != View.MeasureSpec.EXACTLY) width else measureWidthSize
        val h = if (measureHeightMode != View.MeasureSpec.EXACTLY) height else measureHeightSize

        return intArrayOf(max(w, minimumWidth), max(h, minimumHeight))
    }

    /**横向, 总共消耗的宽度*/
    private fun childConsumeWidth(): Int {
        return paddingLeft + paddingRight + itemHorizontalSpace * max(maxCountOnLine - 1, 0)
    }

    /**
     * 等宽并且maxCountLine>0 的时候, 计算 每个child的需要的宽度, margin 属性, 将使用每一行的第一个child
     */
    private fun measureEquChildWidth(lineViews: List<View>, viewWidth: Int): Int {
        if (lineViews.isEmpty()) {
            return viewWidth
        }
        var consumeWidth = childConsumeWidth()
        val firstChild = lineViews[0]
        val lineViewParams = firstChild.layoutParams as LinearLayout.LayoutParams
        for (i in 0 until maxCountOnLine) {
            consumeWidth += lineViewParams.leftMargin + lineViewParams.rightMargin
        }
        val lineChildWidth: Int = if (maxCountOnLine > 0) {
            (viewWidth - consumeWidth) / maxCountOnLine
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
        if (maxCountOnLine > 0) {
            //等宽并且平分, 当lineViewSize没有达到maxCountLine数量时, 需要考虑计算方式.
            lineChildWidth = measureEquChildWidth(lineViews, viewWidth)
        } else {
            var consumeWidth =
                paddingLeft + paddingRight + itemHorizontalSpace * max(lineViewSize - 1, 0)
            for (j in 0 until lineViewSize) {
                val lineView = lineViews[j]
                val lineViewParams =
                    lineView.layoutParams as LinearLayout.LayoutParams
                consumeWidth += lineViewParams.leftMargin + lineViewParams.rightMargin
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
                var childLeft = left + params.leftMargin

                val childTop = when (params.gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                    Gravity.CENTER_VERTICAL -> top + lineHeight / 2 - childHeight / 2 + params.topMargin
                    Gravity.BOTTOM -> top + lineHeight - params.bottomMargin - childHeight
                    else -> top + params.topMargin
                }

                if (lineView.size() == 1) {
                    //如果一行中, 只有一个item, 则激活横向的Gravity
                    childLeft = when (params.gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                        Gravity.CENTER_HORIZONTAL -> paddingLeft + (measuredWidth - paddingLeft - paddingRight) / 2 - childWidth / 2 + params.leftMargin
                        Gravity.RIGHT -> measuredWidth - paddingRight - params.rightMargin - childWidth
                        else -> childLeft
                    }
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
            Gravity.CENTER_HORIZONTAL -> (delegateView.measuredWidth - paddingLeft - paddingRight) / 2 - lineViewWidth / 2 + paddingLeft
            Gravity.RIGHT -> delegateView.measuredWidth - paddingRight - lineViewWidth
            else -> paddingLeft
        }
    }

    override fun drawAfter(canvas: Canvas) {
        super.drawAfter(canvas)
        //分割线, 绘制在child的上面

        val horizontalDrawable = dividerHorizontalDrawable
        val verticalDrawable = dividerVerticalDrawable
        if (horizontalDrawable != null || verticalDrawable != null) {

            var lineBottom = 0

            _allViews.forEachIndexed { row, lineList ->
                if (verticalDrawable != null) {
                    lineList.forEachIndexed { column, view ->
                        lineBottom = max(view.bottom, lineBottom)

                        val left = view.left - dividerVerticalSize
                        val right = view.left
                        val top = view.top
                        val bottom = view.bottom

                        if (column > 0) {
                            //只在左边绘制
                            verticalDrawable.setBounds(left, top, right, bottom)
                            verticalDrawable.draw(canvas)
                        }
                    }
                }

                if (horizontalDrawable != null && row + 1 != _allViews.size()) {
                    horizontalDrawable.setBounds(
                        paddingLeft,
                        lineBottom - dividerHorizontalSize,
                        measuredWidth - paddingRight,
                        lineBottom,
                    )
                    horizontalDrawable.draw(canvas)
                }
            }
        }
    }
}