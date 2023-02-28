package com.angcyo.canvas.render.data

import com.angcyo.canvas.render.unit.IRenderUnit
import com.angcyo.library.annotation.Pixel

/**刻度尺的描述信息
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/15
 */
data class AxisPoint(

    /**当前刻度的绘制像素坐标值*/
    @Pixel
    var pixel: Float = 0f,

    /**当前刻度描述画布中的坐标值*/
    @Pixel
    var value: Float = 0f,

    /**当前刻度距离0的索引, 正负数值*/
    var index: Int = 0,

    /**当前刻度的类型
     * [com.angcyo.canvas.render.unit.IRenderUnit.AXIS_TYPE_NORMAL]
     * [com.angcyo.canvas.render.unit.IRenderUnit.AXIS_TYPE_SECONDARY]
     * [com.angcyo.canvas.render.unit.IRenderUnit.AXIS_TYPE_PRIMARY]
     * */
    var type: Int = IRenderUnit.AXIS_TYPE_NORMAL
) {

    /**只获取[IRenderUnit.AXIS_TYPE_NORMAL]数值*/
    val typeMask: Int
        get() = type and IRenderUnit.AXIS_TYPE_MASK
}
