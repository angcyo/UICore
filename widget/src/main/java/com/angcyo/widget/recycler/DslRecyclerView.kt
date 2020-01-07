package com.angcyo.widget.recycler

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.widget.R

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class DslRecyclerView : RecyclerView {
    constructor(context: Context) : super(context) {
        initAttribute(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttribute(context, attrs)
    }

    fun initAttribute(context: Context, attributeSet: AttributeSet? = null) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.DslRecyclerView)
        typedArray.getString(R.styleable.DslRecyclerView_r_layout_manager)?.let {
            resetLayoutManager(it)
        }
        typedArray.recycle()
    }
}