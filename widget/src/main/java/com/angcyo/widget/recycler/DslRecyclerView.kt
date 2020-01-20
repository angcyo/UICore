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

    /** 通过[V] [H] [GV2] [GH3] [SV2] [SV3] 方式, 设置 [LayoutManager] */
    var layout: String? = null
        set(value) {
            field = value
            value?.run { resetLayoutManager(this) }
        }

    constructor(context: Context) : super(context) {
        initAttribute(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttribute(context, attrs)
    }

    fun initAttribute(context: Context, attributeSet: AttributeSet? = null) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.DslRecyclerView)
        typedArray.getString(R.styleable.DslRecyclerView_r_layout_manager)?.let {
            layout = it
        }
        typedArray.recycle()
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (layoutManager == null) {
            layout?.run { layout = this }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }
}