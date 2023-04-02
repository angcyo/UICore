package com.angcyo.canvas.utils

import com.angcyo.canvas.LinePath
import com.angcyo.canvas.core.renderer.GroupRenderer
import com.angcyo.canvas.core.renderer.SelectGroupRenderer
import com.angcyo.canvas.items.BaseItem
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.laserpacker.LPDataConstant
import com.angcyo.laserpacker.bean.LPElementBean

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/29
 */

/**当前渲染的是否是[LinePath]*/
fun BaseItemRenderer<*>.isLineShape() = getRendererRenderItem()?.isLineShape() == true

/**判断当前的渲染器是否是群组渲染器, 但是不是选择群组渲染器*/
fun BaseItemRenderer<*>?.isJustGroupRenderer() =
    this is GroupRenderer && this !is SelectGroupRenderer

/**是否是线条类型*/
fun BaseItem.isLineShape(): Boolean {
    val item = this
    if (item is DataItem) {
        return item.dataBean.isLineShape()
    }
    return false
}

fun LPElementBean.isLineShape(): Boolean = mtype == LPDataConstant.DATA_TYPE_LINE

/**获取所有依赖的渲染器, 拆组后的所有渲染器*/
fun Collection<BaseItemRenderer<*>>.getAllDependRendererList(): List<BaseItemRenderer<*>> {
    val result = mutableListOf<BaseItemRenderer<*>>()
    for (renderer in this) {
        result.addAll(renderer.getDependRendererList())
    }
    return result
}
