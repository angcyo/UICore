package com.angcyo.canvas.items

import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.angcyo.canvas.core.RenderParams
import com.angcyo.library.annotation.Pixel

/**
 * 简单的渲染数据
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/01/07
 */
class SimpleItem : BaseItem() {

    /**用来渲染在界面上的[Drawable]*/
    var renderDrawable: Drawable? = null
        set(value) {
            field = value
            value?.let {
                if (renderBounds == null) {
                    updateBoundsWith(it)
                }
            }
        }

    /**需要渲染在界面上的什么位置*/
    @Pixel
    var renderBounds: RectF? = null

    //---

    override var itemLayerDrawable: Drawable?
        get() = renderDrawable
        set(value) {
            super.itemLayerDrawable = value
        }

    init {
        itemLayerName = "Simple"
    }

    override fun getDrawDrawable(renderParams: RenderParams): Drawable? = renderDrawable

    //---

    /**使用[drawable]确定渲染的位置*/
    fun updateBoundsWith(drawable: Drawable) {
        renderBounds = RectF(drawable.bounds)
    }
}
