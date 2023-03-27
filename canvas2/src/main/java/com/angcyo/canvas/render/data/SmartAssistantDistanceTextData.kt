package com.angcyo.canvas.render.data

import android.widget.LinearLayout
import com.angcyo.canvas.render.annotation.CanvasInsideCoordinate
import com.angcyo.library.annotation.Pixel

/**
 * 智能提示距离文本
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/27
 */
data class SmartAssistantDistanceTextData(
    /**距离*/
    @Pixel
    val distance: Float,
    /**水平文本绘制的起始坐标*/
    @Pixel
    @CanvasInsideCoordinate
    val drawX: Float,
    @Pixel
    @CanvasInsideCoordinate
    val drawY: Float,
    /**[distance] 是什么方向的值, 竖线or横线
     * [LinearLayout.VERTICAL]
     * [LinearLayout.HORIZONTAL]*/
    var orientation: Int,
)
