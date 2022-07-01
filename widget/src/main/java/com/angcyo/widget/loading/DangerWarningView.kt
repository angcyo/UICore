package com.angcyo.widget.loading

import android.content.Context
import android.util.AttributeSet
import com.angcyo.drawable.DangerWarningDrawable
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.widget.base.BaseDrawableView

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/01
 */
class DangerWarningView(context: Context, attributeSet: AttributeSet? = null) :
    BaseDrawableView(context, attributeSet) {

    init {
        //R.styleable.TGSolidLoadingView
    }

    override fun initDrawables(list: MutableList<AbsDslDrawable>) {
        list.add(DangerWarningDrawable().apply {
            if (isInEditMode()) {
                loadingProgress
                loadingStep
                enableReverse
            }
        })
    }

    fun loading(loading: Boolean = true) {
        firstDrawable<DangerWarningDrawable>()?.loading = loading
    }

}