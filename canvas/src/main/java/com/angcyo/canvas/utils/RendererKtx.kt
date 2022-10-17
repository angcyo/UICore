package com.angcyo.canvas.utils

import android.graphics.Path
import com.angcyo.canvas.LinePath
import com.angcyo.canvas.items.BaseItem
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataPathItem
import com.angcyo.canvas.items.renderer.BaseItemRenderer

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/29
 */

/**当前渲染的是否是[LinePath]*/
fun BaseItemRenderer<*>.isLineShape() = getRendererRenderItem()?.isLineShape() == true

fun BaseItem.isLineShape(): Boolean {
    val item = this
    if (item is DataItem) {
        return item.dataBean.mtype == CanvasConstant.DATA_TYPE_LINE
    }
    return false
}

fun BaseItemRenderer<*>.getPathList(): List<Path>? {
    val item = getRendererRenderItem()
    if (item is DataPathItem) {
        return item.dataPathList
    }
    return null
}