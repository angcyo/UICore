package com.angcyo.widget.progress

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.AccelerateInterpolator
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.widget.R
import com.angcyo.widget.base.BaseAnimatorDrawableView

/**
 * 模仿抖音进度条, 刷刷刷, 闪电般的加载效果.
 * 从中间开始, 向两端扩散的效果
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2017/09/25 17:06
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class DYProgressView(context: Context, attributeSet: AttributeSet? = null) :
    BaseAnimatorDrawableView(context, attributeSet) {

    init {
        onConfigAnimator = {
            it.interpolator = AccelerateInterpolator()
            it.repeatMode = ValueAnimator.RESTART
            it.repeatCount = ValueAnimator.INFINITE
        }

        onAnimatorUpdate = {
            firstDrawable<DYProgressDrawable>()?.apply {
                progress = (it * 100).toInt()
            }
        }

        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.DYProgressView)
        autoStartAnimator = typedArray.getBoolean(
            R.styleable.DYProgressView_r_auto_start_animator,
            autoStartAnimator
        )
        typedArray.recycle()
    }

    override fun initDrawables(list: MutableList<AbsDslDrawable>) {
        list.add(DYProgressDrawable().apply {
            drawType = AbsDslDrawable.DRAW_TYPE_DRAW_NONE
        })
    }

    override fun startAnimator() {
        firstDrawable<DYProgressDrawable>()?.drawType = AbsDslDrawable.DRAW_TYPE_ON_DRAW_AFTER
        super.startAnimator()
    }

    override fun stopAnimator() {
        super.stopAnimator()
        firstDrawable<DYProgressDrawable>()?.apply {
            drawType = AbsDslDrawable.DRAW_TYPE_DRAW_NONE
            progress = 0
            drawProgressRect.setEmpty()
        }
    }
}