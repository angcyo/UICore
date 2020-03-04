package com.angcyo.widget.layout

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout
import com.angcyo.widget.R

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/02
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class RFrameLayout(
    context: Context,
    attributeSet: AttributeSet? = null
) : FrameLayout(context, attributeSet), ILayoutDelegate {

    val layoutDelegate = RLayoutDelegate()

    init {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.RFrameLayout)
        layoutDelegate.initAttribute(this, attributeSet)
        typedArray.recycle()
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
        layoutDelegate.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun getRLayoutDelegate(): RLayoutDelegate {
        return layoutDelegate
    }
}
