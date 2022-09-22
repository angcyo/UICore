package com.angcyo.canvas.graphics

import android.graphics.Canvas
import com.angcyo.canvas.data.ItemDataBean
import com.angcyo.canvas.items.DataItem
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.library.ex.withPicture

/**
 * 解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */
interface IGraphicsParser {

    /**解析数据*/
    fun parse(bean: ItemDataBean): DataItem? = null

    //---

    /**当绘制内容包裹在[ScalePictureDrawable]中*/
    fun wrapScalePictureDrawable(width: Int, height: Int, block: Canvas.() -> Unit) =
        ScalePictureDrawable(withPicture(width, height) {
            block()
        })
}