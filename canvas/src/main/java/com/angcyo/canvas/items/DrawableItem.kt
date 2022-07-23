package com.angcyo.canvas.items

import android.graphics.drawable.Drawable

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/11
 */
open class DrawableItem : BaseItem() {

    /**可绘制的对象*/
    var drawable: Drawable? = null

    init {
        itemLayerName = "Drawable"
    }

    /**更新[drawable]*/
    fun updateDrawable(drawable: Drawable?) {
        this.drawable = drawable
        this.itemWidth = drawable?.minimumWidth?.toFloat() ?: 0f
        this.itemHeight = drawable?.minimumHeight?.toFloat() ?: 0f
    }

}