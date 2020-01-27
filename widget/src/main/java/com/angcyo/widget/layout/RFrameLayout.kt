package com.angcyo.widget.layout

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import com.angcyo.widget.R
import com.angcyo.widget.base.InvalidateProperty

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
) : FrameLayout(context, attributeSet) {

    var bDrawable: Drawable? by InvalidateProperty(null)

    init {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.RFrameLayout)
        bDrawable = typedArray.getDrawable(R.styleable.RFrameLayout_r_background)
        typedArray.recycle()
    }

    override fun draw(canvas: Canvas) {
        bDrawable?.run {
            setBounds(0, 0, measuredWidth, measuredHeight)
            draw(canvas)
        }
        super.draw(canvas)
    }
}
