package com.angcyo.widget.layout

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.angcyo.library.ex.calcSize
import com.angcyo.library.ex.mH
import com.angcyo.library.ex.mW
import com.angcyo.tablayout.exactlyMeasure
import com.angcyo.widget.R
import kotlin.math.max

/**
 * 高级[layout_weight]属性
 * [android.widget.LinearLayout.LayoutParams.weight]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/09
 */
class RatioLinearLayout(context: Context, attributeSet: AttributeSet? = null) :
    LinearLayout(context, attributeSet) {

    /**child 宽度的比例
     * -1:-2:0.5:0.3:20dp:30sp:40ph
     * -1 撑满
     * -2 wrap
     * 0.5 占剩余空间的比例
     * 20dp 强制大小
     * */
    var childRatio: String? = null
        set(value) {
            field = value
            _ratioList = null
            requestLayout()
        }

    init {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.RatioLinearLayout)
        childRatio = typedArray.getString(R.styleable.RatioLinearLayout_r_child_ratio)
        typedArray.recycle()
    }

    var _ratioList: List<String>? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val radio = childRatio
        if (radio.isNullOrEmpty()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            val ratioList = _ratioList ?: radio.split(":")
            _ratioList = ratioList

            if (orientation == VERTICAL) {
                measureVertical(widthMeasureSpec, heightMeasureSpec)
            } else {
                measureHorizontal(widthMeasureSpec, heightMeasureSpec)
            }
        }
    }

    fun measureHorizontal(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //布局的宽高
        var width = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        if (widthMode != MeasureSpec.EXACTLY) {
            width = paddingLeft + paddingRight
        }

        var height = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (heightMode != MeasureSpec.EXACTLY) {
            height = paddingTop + paddingBottom
        }

        //剩余空间, 用来计算比例
        var freeSpace = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        //需要计算比例的view
        val radioViewList = mutableListOf<View>()

        var index = 0

        for (childIndex in 0 until childCount) {
            val child = getChildAt(childIndex)
            if (child.visibility == View.GONE) {
                continue
            }

            val lp = child.layoutParams

            var childWidthMeasureSpec = getChildMeasureSpec(
                widthMeasureSpec,
                paddingLeft + paddingRight, lp.width
            )
            val childHeightMeasureSpec = getChildMeasureSpec(
                heightMeasureSpec,
                paddingTop + paddingBottom, lp.height
            )

            val measureRadio = _ratioList?.getOrNull(index)

            if (!measureRadio.isNullOrEmpty()) {
                val radioFloat = measureRadio.toFloatOrNull()
                if (radioFloat != null && radioFloat <= 1f && radioFloat > 0) {
                    //比例
                    radioViewList.add(child)
                    (child.layoutParams as LayoutParams).weight = radioFloat //保存比例
                    index++
                    continue
                }
                val radioInt = measureRadio.toIntOrNull()
                if (radioInt != null) {
                    if (radioInt == -1) {
                        //Match_Parent
                        childWidthMeasureSpec = getChildMeasureSpec(
                            widthMeasureSpec,
                            paddingLeft + paddingRight, -1
                        )
                    } else if (radioInt == -2) {
                        //Wrap_Content
                        childWidthMeasureSpec = getChildMeasureSpec(
                            widthMeasureSpec,
                            paddingLeft + paddingRight, -2
                        )
                    }
                } else {
                    //dp sp ph
                    val childWidth =
                        calcSize(measureRadio, width, height, paddingLeft + paddingRight)
                    childWidthMeasureSpec = exactlyMeasure(childWidth)
                }
            }

            //measure
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
            if (widthMode != MeasureSpec.EXACTLY) {
                width += child.mW()
            }
            if (heightMode != MeasureSpec.EXACTLY) {
                height = max(height, child.mH() + paddingTop + paddingBottom)
            }

            //...
            freeSpace -= child.mW()
            index++
        }//end

        freeSpace = max(0, freeSpace)

        //radio
        for (ratioView in radioViewList) {
            val lp = ratioView.layoutParams as LayoutParams
            val childWidthMeasureSpec = exactlyMeasure(lp.weight * freeSpace)
            val childHeightMeasureSpec = getChildMeasureSpec(
                heightMeasureSpec,
                paddingTop + paddingBottom, lp.height
            )

            //measure
            ratioView.measure(childWidthMeasureSpec, childHeightMeasureSpec)
            if (widthMode != MeasureSpec.EXACTLY) {
                width += ratioView.mW()
            }
            if (heightMode != MeasureSpec.EXACTLY) {
                height = max(height, ratioView.mH() + paddingTop + paddingBottom)
            }
        }

        setMeasuredDimension(width, height)
    }

    fun measureVertical(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //布局的宽高
        var width = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        if (widthMode != MeasureSpec.EXACTLY) {
            width = paddingLeft + paddingRight
        }

        var height = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (heightMode != MeasureSpec.EXACTLY) {
            height = paddingTop + paddingBottom
        }

        //剩余空间, 用来计算比例
        var freeSpace = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom
        //需要计算比例的view
        val radioViewList = mutableListOf<View>()

        var index = 0

        for (childIndex in 0 until childCount) {
            val child = getChildAt(childIndex)
            if (child.visibility == View.GONE) {
                continue
            }

            val lp = child.layoutParams

            val childWidthMeasureSpec = getChildMeasureSpec(
                widthMeasureSpec,
                paddingLeft + paddingRight, lp.width
            )
            var childHeightMeasureSpec = getChildMeasureSpec(
                heightMeasureSpec,
                paddingTop + paddingBottom, lp.height
            )

            val measureRadio = _ratioList?.getOrNull(index)

            if (!measureRadio.isNullOrEmpty()) {
                val radioFloat = measureRadio.toFloatOrNull()
                if (radioFloat != null && radioFloat <= 1f && radioFloat > 0) {
                    //比例
                    radioViewList.add(child)
                    (child.layoutParams as LayoutParams).weight = radioFloat //保存比例
                    index++
                    continue
                }
                val radioInt = measureRadio.toIntOrNull()
                if (radioInt != null) {
                    if (radioInt == -1) {
                        //Match_Parent
                        childHeightMeasureSpec = getChildMeasureSpec(
                            heightMeasureSpec,
                            paddingTop + paddingBottom, -1
                        )
                    } else if (radioInt == -2) {
                        //Wrap_Content
                        childHeightMeasureSpec = getChildMeasureSpec(
                            heightMeasureSpec,
                            paddingTop + paddingBottom, -2
                        )
                    }
                } else {
                    //dp sp ph
                    val childHeight =
                        calcSize(measureRadio, width, height, paddingTop + paddingBottom)
                    childHeightMeasureSpec = exactlyMeasure(childHeight)
                }
            }

            //measure
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
            if (widthMode != MeasureSpec.EXACTLY) {
                width = max(width, child.mW() + paddingLeft + paddingRight)
            }
            if (heightMode != MeasureSpec.EXACTLY) {
                height += child.mH()
            }

            //...
            freeSpace -= child.mH()
            index++
        }//end

        freeSpace = max(0, freeSpace)

        //radio
        for (ratioView in radioViewList) {
            val lp = ratioView.layoutParams as LayoutParams
            val childHeightMeasureSpec = exactlyMeasure(lp.weight * freeSpace)
            val childWidthMeasureSpec = getChildMeasureSpec(
                widthMeasureSpec,
                paddingLeft + paddingRight, lp.width
            )

            //measure
            ratioView.measure(childWidthMeasureSpec, childHeightMeasureSpec)
            if (widthMode != MeasureSpec.EXACTLY) {
                width = max(width, ratioView.mW() + paddingLeft + paddingRight)
            }
            if (heightMode != MeasureSpec.EXACTLY) {
                height += ratioView.mH()
            }
        }

        setMeasuredDimension(width, height)
    }

}