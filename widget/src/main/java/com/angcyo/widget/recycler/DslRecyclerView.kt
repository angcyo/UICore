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

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttribute(context, attrs, defStyleAttr)
    }

    fun initAttribute(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0) {
        val typedArray =
            context.obtainStyledAttributes(
                attributeSet,
                R.styleable.DslRecyclerView,
                defStyleAttr,
                0
            )
        typedArray.recycle()
    }
}