package com.angcyo.widget.progress

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.widget.base.BaseDrawableView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/06/11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class HSProgressView(context: Context, attributeSet: AttributeSet? = null) :
    BaseDrawableView(context, attributeSet) {

    override fun initDrawables(list: MutableList<AbsDslDrawable>) {
        list.add(HSProgressDrawable())
    }

    private var valueAnimator: ValueAnimator? = null

    /**开始进度动画*/
    fun startAnimator() {
        if (valueAnimator == null) {
            valueAnimator = ValueAnimator.ofInt(1, 100).apply {
                interpolator = LinearInterpolator()
                duration = 1400
                repeatMode = ValueAnimator.RESTART
                repeatCount = ValueAnimator.INFINITE
                addUpdateListener { animation ->
                    firstDrawable<HSProgressDrawable>()?.progress = animation.animatedValue as Int
                    postInvalidate()
                }
                start()
            }
        }
    }

    /**停止进度动画*/
    fun stopAnimator() {
        valueAnimator?.cancel()
        valueAnimator = null
        firstDrawable<HSProgressDrawable>()?.progress = 0
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (!isInEditMode && visibility != VISIBLE) {
            stopAnimator()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //startAnimator()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimator()
    }
}