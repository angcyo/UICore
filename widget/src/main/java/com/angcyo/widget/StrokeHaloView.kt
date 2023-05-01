package com.angcyo.widget

import android.content.Context
import android.util.AttributeSet
import com.angcyo.drawable.StrokeHaloDrawable
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.widget.base.BaseDrawableView

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/05/01
 */
class StrokeHaloView(context: Context, attributeSet: AttributeSet? = null) :
    BaseDrawableView(context, attributeSet) {
    override fun initDrawables(list: MutableList<AbsDslDrawable>) {
        list.add(StrokeHaloDrawable().apply {
            if (isInEditMode()) {
                loadingProgress = 50f
            }
        })
    }
}