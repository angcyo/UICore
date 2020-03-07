package com.angcyo.widget.layout

import android.content.Context
import android.util.AttributeSet
import android.widget.RadioGroup

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/07
 */

class DslRadioFlowLayout(context: Context, attrs: AttributeSet? = null) :
    RadioGroup(context, attrs), IFlowLayoutDelegate {

    val flowLayoutDelegate = FlowLayoutDelegate()

    init {
        flowLayoutDelegate.initAttribute(this, attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        flowLayoutDelegate.onMeasure(widthMeasureSpec, heightMeasureSpec).apply {
            setMeasuredDimension(this[0], this[1])
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount == 0) {
            super.onLayout(changed, l, t, r, b)
            return
        }
        flowLayoutDelegate.onLayout(changed, l, t, r, b)
    }

    override fun getCustomFlowLayoutDelegate(): FlowLayoutDelegate {
        return flowLayoutDelegate
    }
}
