package com.angcyo.widget

import android.content.Context
import android.util.AttributeSet
import com.angcyo.drawable.StateBarDrawable
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.widget.base.BaseDrawableView

/**

 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/04/16
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class StateBarView(context: Context, attributeSet: AttributeSet? = null) :
    BaseDrawableView(context, attributeSet) {
    override fun initDrawables(list: MutableList<AbsDslDrawable>) {
        list.add(StateBarDrawable().apply {
            if (isInEditMode()) {
                state = StateBarDrawable.STATE_ING
                ingProgress = 50f
            }
        })
    }
}