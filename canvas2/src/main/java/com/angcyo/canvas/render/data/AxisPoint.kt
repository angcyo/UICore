package com.angcyo.canvas.render.data

import com.angcyo.library.canvas.annotation.CanvasInsideCoordinate
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.have
import com.angcyo.library.unit.IRenderUnit

/**刻度尺的描述信息
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/15
 */
data class AxisPoint(

    /**当前刻度的绘制在view上的坐标位置*/
    @Pixel
    var pixel: Float = 0f,

    /**当前刻度描述的画布中的坐标值, 对应画布中的坐标值*/
    @Pixel
    @CanvasInsideCoordinate
    var value: Float = 0f,

    /**当前刻度距离0的索引, 正负数值*/
    var index: Int = 0,

    /**当前刻度的类型
     * [import com.angcyo.library.unit.IRenderUnit.AXIS_TYPE_NORMAL]
     * [import com.angcyo.library.unit.IRenderUnit.AXIS_TYPE_SECONDARY]
     * [import com.angcyo.library.unit.IRenderUnit.AXIS_TYPE_PRIMARY]
     * */
    var type: Int = IRenderUnit.AXIS_TYPE_NORMAL
) {

    /**只获取[IRenderUnit.AXIS_TYPE_NORMAL]数值*/
    val typeMask: Int
        get() = type and IRenderUnit.AXIS_TYPE_MASK

    /**是否是刻度尺*/
    val isLineRule: Boolean
        get() = type.have(IRenderUnit.AXIS_TYPE_NORMAL) ||
                type.have(IRenderUnit.AXIS_TYPE_SECONDARY) ||
                type.have(IRenderUnit.AXIS_TYPE_PRIMARY)

    /**是否是关键的刻度*/
    val isMasterRule: Boolean
        get() = type.have(IRenderUnit.AXIS_TYPE_SECONDARY) ||
                type.have(IRenderUnit.AXIS_TYPE_PRIMARY)
}
