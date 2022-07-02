package com.angcyo.canvas.core.component

import com.angcyo.canvas.core.IRenderer
import com.angcyo.canvas.items.renderer.IItemRenderer

/**
 * 智能提示 参考的值
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/02
 */
data class SmartAssistantValueData(
    /**参考的值, 比如x,y, w,h 像素, 或者旋转的角度*/
    var refValue: Float = -1f,
    /**[refValue]参考的[IItemRenderer], 如果有*/
    var refRenderer: IRenderer? = null,
)
