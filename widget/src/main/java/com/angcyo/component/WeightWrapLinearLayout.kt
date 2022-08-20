package com.angcyo.component

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import com.angcyo.library.ex.isVisible

/**
 * 当前布局使用wrap, child又包含weight
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/08/20
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class WeightWrapLinearLayout(context: Context, attributeSet: AttributeSet? = null) :
    LinearLayout(context, attributeSet) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var haveWeight = false
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.isVisible()) {
                val lp: LayoutParams = child.layoutParams as LayoutParams
                if (lp.weight > 0) {
                    haveWeight = true
                    break
                }
            }
        }
        if (haveWeight) {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}