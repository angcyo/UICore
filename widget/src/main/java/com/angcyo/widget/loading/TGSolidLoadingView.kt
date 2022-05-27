package com.angcyo.widget.loading

import android.content.Context
import android.util.AttributeSet
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.drawable.loading.BaseTGLoadingDrawable
import com.angcyo.drawable.loading.TGSolidLoadingDrawable
import com.angcyo.library.ex.dp
import com.angcyo.widget.base.BaseDrawableView

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/26
 */
class TGSolidLoadingView(context: Context, attributeSet: AttributeSet? = null) :
    BaseDrawableView(context, attributeSet) {

    override fun initDrawables(list: MutableList<AbsDslDrawable>) {
        list.add(TGSolidLoadingDrawable().apply {
            if (isInEditMode()) {
                loadingWidth = 19 * dp
                loadingOffset = 10 * dp
                indeterminateSweepAngle = 45f
            }
        })
    }

    fun loading(loading: Boolean = true) {
        firstDrawable<BaseTGLoadingDrawable>()?.loading = loading
    }

}