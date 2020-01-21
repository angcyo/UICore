package com.angcyo.glide

import android.content.Context
import android.util.AttributeSet
import com.angcyo.widget.image.DslImageView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/20
 */

open class GlideImageView : DslImageView {

    val dslGlide: DslGlide by lazy {
        DslGlide().apply {
            targetView = this@GlideImageView
        }
    }

    constructor(context: Context) : super(context) {
        initAttribute(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttribute(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttribute(context, attrs)
    }

    private fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        dslGlide.placeholderDrawable = drawable
    }

    fun load(url: String?, action: DslGlide.() -> Unit = {}) {
        dslGlide.apply {
            reset()
            action()
            load(url)
        }
    }

    //</editor-fold desc="Glide操作">

    //<editor-fold desc="操作">

    //</editor-fold desc="操作">
}