package com.angcyo.widget.loading

import android.content.Context
import android.util.AttributeSet
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.drawable.loading.CircleScaleLoadingDrawable
import com.angcyo.widget.R
import com.angcyo.widget.base.BaseDrawableView

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/14
 */
class CircleScaleLoadingView(context: Context, attributeSet: AttributeSet? = null) :
    BaseDrawableView(context, attributeSet) {

    init {
        R.styleable.CircleScaleLoadingView
    }

    override fun initDrawables(list: MutableList<AbsDslDrawable>) {
        list.add(CircleScaleLoadingDrawable().apply {
            loading = true
            if (isInEditMode()) {
                progress = 50f
            }
        })
    }

    fun loading(loading: Boolean = true) {
        firstDrawable<CircleScaleLoadingDrawable>()?.loading = loading
    }
}