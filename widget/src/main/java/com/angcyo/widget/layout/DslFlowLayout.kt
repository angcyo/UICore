package com.angcyo.widget.layout

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/30
 */
class DslFlowLayout(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs), IFlowLayoutDelegate {

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

    override fun setGravity(gravity: Int) {
        //在父类初始化的时候, 就会执行此方法. 此时flowLayoutDelegate还未初始化
        flowLayoutDelegate?.lineGravity = gravity
        super.setGravity(gravity)
    }

    override fun getCustomFlowLayoutDelegate(): FlowLayoutDelegate {
        return flowLayoutDelegate
    }
}