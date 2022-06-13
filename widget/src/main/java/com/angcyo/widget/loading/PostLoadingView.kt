package com.angcyo.widget.loading

import android.content.Context
import android.util.AttributeSet
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.drawable.loading.PostLoadingDrawable
import com.angcyo.widget.base.BaseDrawableView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/06/13
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class PostLoadingView(context: Context, attributeSet: AttributeSet? = null) :
    BaseDrawableView(context, attributeSet) {

    override fun initDrawables(list: MutableList<AbsDslDrawable>) {
        list.add(PostLoadingDrawable().apply {
            loading = true
            if (isInEditMode()) {
                progress = 50
            }
        })
    }

    fun loading(loading: Boolean = true) {
        firstDrawable<PostLoadingDrawable>()?.loading = loading
    }

}