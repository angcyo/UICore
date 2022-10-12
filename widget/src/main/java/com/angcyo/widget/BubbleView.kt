package com.angcyo.widget

import android.content.Context
import android.util.AttributeSet
import com.angcyo.drawable.BubbleDrawable
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.widget.base.BaseDrawableView

/**
 * 气泡View
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/08/18
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class BubbleView(context: Context, attributeSet: AttributeSet? = null) :
    BaseDrawableView(context, attributeSet) {

    override fun initDrawables(list: MutableList<AbsDslDrawable>) {
        list.add(BubbleDrawable())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        if (widthMode == MeasureSpec.EXACTLY) {
            //指定了宽度
            val widthSize = MeasureSpec.getSize(widthMeasureSpec)
            bubbleDrawable {
                bubbleMinWidth = widthSize
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun bubbleDrawable(dsl: BubbleDrawable.() -> Unit) {
        firstDrawable<BubbleDrawable>()?.apply(dsl)
    }

    fun render(dsl: BubbleDrawable.() -> Unit) {
        bubbleDrawable { render(dsl) }
    }
}