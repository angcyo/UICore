package com.angcyo.widget.progress

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.widget.R
import com.angcyo.widget.base.BaseAnimatorDrawableView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/06/11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class HSProgressView(context: Context, attributeSet: AttributeSet? = null) :
    BaseAnimatorDrawableView(context, attributeSet) {

    init {
        onAnimatorUpdate = {
            firstDrawable<HSProgressDrawable>()?.progress = (it * 100).toInt()
        }

        onConfigAnimator = {
            it.interpolator = LinearInterpolator()
            it.duration = 1400
            it.repeatMode = ValueAnimator.RESTART
            it.repeatCount = ValueAnimator.INFINITE
        }

        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.HSProgressView)
        autoStartAnimator = typedArray.getBoolean(
            R.styleable.HSProgressView_r_auto_start_animator,
            autoStartAnimator
        )
        typedArray.recycle()

        if (isInEditMode) {
            firstDrawable<HSProgressDrawable>()?.progress = 50
        }
    }

    override fun initDrawables(list: MutableList<AbsDslDrawable>) {
        list.add(HSProgressDrawable())
    }

    override fun stopAnimator() {
        super.stopAnimator()
        firstDrawable<HSProgressDrawable>()?.progress = 0
    }
}