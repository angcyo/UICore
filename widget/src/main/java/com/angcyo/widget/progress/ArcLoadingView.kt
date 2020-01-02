package com.angcyo.widget.progress

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.angcyo.drawable.loading.ArcLoadingDrawable
import com.angcyo.widget.R
import com.angcyo.widget.base.getColor

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/02
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class ArcLoadingView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    var arcLoadingDrawable = ArcLoadingDrawable()

    var duration: Long = 2000

    init {

        val array: TypedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.ArcLoadingView)

        if (isInEditMode) {
            arcLoadingDrawable.progress = 50
        }

        arcLoadingDrawable.arcColor = getColor(R.color.colorAccent)

        arcLoadingDrawable.arcColor =
            array.getColor(R.styleable.ArcLoadingView_arc_color, arcLoadingDrawable.arcColor)

        arcLoadingDrawable.strokeWidth = array.getDimensionPixelOffset(
            R.styleable.ArcLoadingView_arc_width,
            arcLoadingDrawable.strokeWidth.toInt()
        ).toFloat()

        duration =
            array.getInt(R.styleable.ArcLoadingView_arc_duration, duration.toInt()).toLong()

        arcLoadingDrawable.progress =
            array.getInt(R.styleable.ArcLoadingView_arc_progress, arcLoadingDrawable.progress)

        array.recycle()

        arcLoadingDrawable.callback = this
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who == arcLoadingDrawable
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        arcLoadingDrawable.apply {
            setBounds(paddingLeft, paddingTop, right - paddingRight, bottom - paddingBottom)
            draw(canvas)
        }
    }
}