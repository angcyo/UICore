package com.angcyo.canvas.utils

import com.angcyo.canvas.LinePath
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.items.BaseItem
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.renderer.BaseItemRenderer

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/29
 */

/**当前渲染的是否是[LinePath]*/
fun BaseItemRenderer<*>.isLineShape() = getRendererRenderItem()?.isLineShape() == true

/**是否是线条类型*/
fun BaseItem.isLineShape(): Boolean {
    val item = this
    if (item is DataItem) {
        return item.dataBean.isLineShape()
    }
    return false
}

fun CanvasProjectItemBean.isLineShape(): Boolean = mtype == CanvasConstant.DATA_TYPE_LINE