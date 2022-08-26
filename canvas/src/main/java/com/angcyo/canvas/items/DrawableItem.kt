package com.angcyo.canvas.items

import android.graphics.Paint
import android.graphics.drawable.Drawable
import com.angcyo.canvas.utils.CanvasConstant

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/11
 */
open class DrawableItem : BaseItem() {

    /**可绘制的对象*/
    var drawable: Drawable? = null

    init {
        itemLayerName = "Drawable"
        dataType = CanvasConstant.DATA_TYPE_BITMAP
    }

    /**更新[drawable]*/
    open fun updateDrawable(drawable: Drawable?) {
        this.drawable = drawable
        this.itemWidth = drawable?.intrinsicWidth?.toFloat() ?: 0f
        this.itemHeight = drawable?.intrinsicHeight?.toFloat() ?: 0f
    }

    override fun updateItem(paint: Paint) {
        updateDrawable(drawable)
    }

}