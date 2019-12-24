package com.angcyo.widget.layout

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.FrameLayout
import com.angcyo.widget.base.getStatusBarHeight

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class TitleWrapLayout(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet) {

    init {
        setPadding(0, getStatusBarHeight(), 0, 0)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return initLayoutParams(super.generateDefaultLayoutParams())
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return initLayoutParams(super.generateLayoutParams(attrs))
    }

    fun initLayoutParams(lp: LayoutParams): LayoutParams {
        return lp.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && topMargin <= 0) {
                //topMargin = getStatusBarHeight()
            }
        }
    }
}