package com.angcyo.canvas.core.component

import android.graphics.RectF

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/25
 */
data class SmartAssistantData(

    /**从那个值, 计算出来的推荐值, 相对于坐标系统的值*/
    val fromValue: Float = -1f,

    /**推荐值*/
    val smartValue: SmartAssistantValueData,

    /**绘制提示的线坐标.
     * 坐标系统中的坐标
     * */
    var drawRect: RectF? = null,

    )