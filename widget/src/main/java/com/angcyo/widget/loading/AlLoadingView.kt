package com.angcyo.widget.loading

import android.content.Context
import android.util.AttributeSet
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.drawable.loading.AlLoadingDrawable
import com.angcyo.widget.R
import com.angcyo.widget.base.BaseDrawableView

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/15
 */
class AlLoadingView(context: Context, attributeSet: AttributeSet? = null) :
    BaseDrawableView(context, attributeSet) {

    init {
        R.styleable.AlLoadingView
    }

    override fun initDrawables(list: MutableList<AbsDslDrawable>) {
        list.add(AlLoadingDrawable().apply {
            loading = true
        })
    }

    fun loading(loading: Boolean = true) {
        firstDrawable<AlLoadingDrawable>()?.loading = loading
    }

}