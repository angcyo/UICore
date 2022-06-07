package com.angcyo.canvas.core

import com.angcyo.canvas.items.renderer.BaseItemRenderer

/**
 * 需要偏移的数据
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/07
 */
data class OffsetItemData(
    val item: BaseItemRenderer<*>,
    val dx: Float,
    val dy: Float,
)
