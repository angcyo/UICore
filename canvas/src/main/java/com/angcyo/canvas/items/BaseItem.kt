package com.angcyo.canvas.items

import android.graphics.drawable.Drawable
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.library.ex.uuid

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/08
 */
abstract class BaseItem : ICanvasItem {

    /**唯一标识符*/
    var uuid: String = uuid()

    //

    /**图层预览的名称*/
    override var itemLayerName: CharSequence? = null

    /**图层预览的图形*/
    override var itemLayerDrawable: Drawable? = null

    //

    /**获取当前的缩放比例*/
    open fun getItemScaleX(renderer: BaseItemRenderer<*>): Float = 1f

    open fun getItemScaleY(renderer: BaseItemRenderer<*>): Float = 1f
}