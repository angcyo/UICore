package com.angcyo.canvas.core

import android.graphics.RectF

/** 渲染的一些参数
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/11/29
 */
data class RenderParams(
    /**是否是来自画布的渲染请求, 决定了使用哪个对象绘制
     * [com.angcyo.canvas.items.data.DataItem.dataDrawable]
     * [com.angcyo.canvas.items.data.DataItem.renderDrawable]*/
    var isFromRenderer: Boolean = true,

    /**[IRenderer]在渲染时, 需要强制绘制在此区域
     * [com.angcyo.canvas.core.IRenderer.render]*/
    var itemRenderBounds: RectF? = null
)
