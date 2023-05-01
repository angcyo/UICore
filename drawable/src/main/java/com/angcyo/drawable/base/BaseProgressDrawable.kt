package com.angcyo.drawable.base

import android.content.Context
import android.util.AttributeSet
import com.angcyo.drawable.R

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/16
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
abstract class BaseProgressDrawable : AbsDslDrawable(), IProgressDrawable {

    /**不确定的进度*/
    override var isIndeterminate: Boolean = true
        set(value) {
            field = value
            invalidateSelf()
        }

    /**当前的进度
     * [0~100]*/
    override var progress: Float = 0f
        set(value) {
            field = value
            invalidateSelf()
        }

    override fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        val typedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.BaseProgressDrawable)
        isIndeterminate = typedArray.getBoolean(
            R.styleable.BaseProgressDrawable_r_loading_is_indeterminate, isIndeterminate
        )
        progress =
            typedArray.getInt(R.styleable.BaseProgressDrawable_r_loading_progress, progress.toInt())
                .toFloat()
        typedArray.recycle()
    }

}