package com.angcyo.widget.loading

import android.content.Context
import android.util.AttributeSet
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.drawable.loading.RadarScanLoadingDrawable
import com.angcyo.widget.R
import com.angcyo.widget.base.BaseDrawableView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/06/18
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class RadarScanLoadingView(context: Context, attributeSet: AttributeSet? = null) :
    BaseDrawableView(context, attributeSet) {

    init {
        R.styleable.RadarScanLoadingView
    }

    override fun initDrawables(list: MutableList<AbsDslDrawable>) {
        list.add(RadarScanLoadingDrawable().apply {
            loading = true
        })
    }

    fun loading(loading: Boolean = true) {
        firstDrawable<RadarScanLoadingDrawable>()?.loading = loading
    }

}