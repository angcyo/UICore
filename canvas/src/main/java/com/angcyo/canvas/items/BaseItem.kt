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

    /**雕刻数据的索引, 改变位置后, 不用更新索引. 调整宽高旋转分辨率后需要更新索引*/
    var engraveIndex: Int? = null

    /**用来存放自定义的数据*/
    var data: Any? = null

    //临时
    var dataType = 0
    var dataMode = 0
    var itemWidth = 0f
    var itemHeight = 0f

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