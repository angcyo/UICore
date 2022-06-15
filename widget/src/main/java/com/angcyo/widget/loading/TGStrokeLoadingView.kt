package com.angcyo.widget.loading

import android.content.Context
import android.util.AttributeSet
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.drawable.loading.BaseTGLoadingDrawable
import com.angcyo.drawable.loading.TGStrokeLoadingDrawable
import com.angcyo.library.ex.dp
import com.angcyo.widget.R
import com.angcyo.widget.base.BaseDrawableView

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/26
 */
class TGStrokeLoadingView(context: Context, attributeSet: AttributeSet? = null) :
    BaseDrawableView(context, attributeSet) {

    init {
        R.styleable.TGStrokeLoadingView
    }

    override fun initDrawables(list: MutableList<AbsDslDrawable>) {
        list.add(TGStrokeLoadingDrawable().apply {
            if (isInEditMode()) {
                loadingWidth = 19 * dp
                loadingOffset = loadingWidth + 10 * dp
                bgStrokeWidth = loadingWidth
                indeterminateSweepAngle = 45f
                loadingColor = loadingBgColor
            }
        })
    }

    fun loading(loading: Boolean = true) {
        firstDrawable<BaseTGLoadingDrawable>()?.loading = loading
    }

}