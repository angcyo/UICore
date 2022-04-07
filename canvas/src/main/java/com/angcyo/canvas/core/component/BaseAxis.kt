package com.angcyo.canvas.core.component

import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.floor

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
abstract class BaseAxis : BaseComponent() {

    companion object {
        //刻度线的绘制类型, 不绘制
        const val LINE_TYPE_NONE = 0

        //标准刻度线
        const val LINE_TYPE_NORMAL = 1

        //主要刻度, 占满整个轴
        const val LINE_TYPE_PROTRUDE = 2

        //次要刻度, 大小为轴的一半
        const val LINE_TYPE_SECONDARY = 3
    }

    /**轴的宽度/高度*/
    var axisSize = 20 * dp

    /**刻度的大小*/
    var lineSize: Float = 4 * dp

    /**关键刻度的大小*/
    var lineProtrudeSize: Float = axisSize

    /**次要刻度的大小*/
    var lineSecondarySize: Float = axisSize / 2

    /**刻度文本和刻度之间的偏移距离*/
    var labelXOffset = 2 * dp

    var labelYOffset = 2 * dp

    /**是否绘制网格线*/
    var drawGridLine: Boolean = true

    /**获取当前索引对应的绘制轴线类型
     * [lineProtrudeSize]
     * [lineSecondarySize]
     * [lineSize]
     * */
    fun getAxisLineType(index: Int, scale: Float): Int {
        val loseStep = 0.3f
        var gainScale = 10

        /*if (scale < 1f) {
            val step = ((1 - scale) / loseStep).floor().toInt()

            if (step >= 1) {
                gainScale *= step
                if (index % (10 * gainScale) == 0) {
                    return LINE_TYPE_PROTRUDE
                }
                if (index % (5 * gainScale) == 0) {
                    return LINE_TYPE_SECONDARY
                }
                if (index % (1 * gainScale) == 0) {
                    return LINE_TYPE_NORMAL
                }
                return LINE_TYPE_NONE
            }
        }*/
        if (index % 10 == 0) {
            return LINE_TYPE_PROTRUDE
        }
        if (index % 5 == 0) {
            return LINE_TYPE_SECONDARY
        }
        return LINE_TYPE_NORMAL
    }

    /**获取正向坐标需要绘制刻度的像素点坐标位置, 未映射后的坐标*/
    abstract fun getPlusPixelList(canvasViewBox: CanvasViewBox): List<Float>

    /**获取负向坐标需要绘制刻度的像素点坐标位置, 未映射后的坐标*/
    abstract fun getMinusPixelList(canvasViewBox: CanvasViewBox): List<Float>
}