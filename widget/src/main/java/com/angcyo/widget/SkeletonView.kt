package com.angcyo.widget

import android.content.Context
import android.util.AttributeSet
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.drawable.skeleton.SkeletonDrawable
import com.angcyo.widget.base.BaseDrawableView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/22
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

class SkeletonView(context: Context, attributeSet: AttributeSet? = null) :
    BaseDrawableView(context, attributeSet) {
    override fun initDrawables(list: MutableList<AbsDslDrawable>) {
        list.add(SkeletonDrawable())
    }
}