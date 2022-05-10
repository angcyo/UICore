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
        itemName = "Drawable"
    }

}