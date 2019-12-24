package com.angcyo.widget.layout

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.LinearLayout
import com.angcyo.widget.R
import com.angcyo.widget.base.InvalidateProperty

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class RLinearLayout(context: Context, attributeSet: AttributeSet? = null) :
    LinearLayout(context, attributeSet) {

    var bDrawable: Drawable? by InvalidateProperty(null)

    init {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.RLinearLayout)
        bDrawable = typedArray.getDrawable(R.styleable.RLinearLayout_r_background)
        typedArray.recycle()
    }

    override fun draw(canvas: Canvas) {
        bDrawable?.run {
            canvas.getClipBounds(bounds)
            draw(canvas)
        }
        super.draw(canvas)
    }
}