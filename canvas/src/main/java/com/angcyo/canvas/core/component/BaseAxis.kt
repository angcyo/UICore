package com.angcyo.canvas.core.component

import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.InchValueUnit
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
        const val LINE_TYPE_NORMAL = 0x001

        //主要刻度, 占满整个轴
        const val LINE_TYPE_PROTRUDE = 0x002

        //次要刻度, 大小为轴的一半
        const val LINE_TYPE_SECONDARY = 0x004

        //这根线是否需要绘制Label标签
        const val LINE_TYPE_DRAW_LABEL = 0x010

        //这根线是否需要绘制网格线
        const val LINE_TYPE_DRAW_GRID = 0x100
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
    fun getAxisLineType(canvasViewBox: CanvasViewBox, index: Int, scale: Float): Int {
        var result = LINE_TYPE_NONE
        val loseStep = 0.25f

        if (scale < 1f) {
            //坐标系缩放后
            val step = ((1 - scale) / loseStep).floor().toInt() + 1
            if (index % (10 * step) == 0) {
                //主要刻度
                result = LINE_TYPE_PROTRUDE or LINE_TYPE_DRAW_LABEL or LINE_TYPE_DRAW_GRID
                return result
            }
            if (index % (5 * step) == 0) {
                //次要刻度
                result = LINE_TYPE_SECONDARY
                if (scale >= 0.2f) {
                    result = result or LINE_TYPE_DRAW_GRID
                }
                if (canvasViewBox.valueUnit is InchValueUnit) {
                    result = result or LINE_TYPE_DRAW_LABEL
                }
                return result
            }
            if (index % (1 * step) == 0) {
                result = LINE_TYPE_NORMAL
                if (scale >= 0.2f) {
                    result = result or LINE_TYPE_DRAW_GRID
                }
                return result
            }
            return result
        }

        //坐标系放大后
        if (index % 10 == 0) {
            result = LINE_TYPE_PROTRUDE or LINE_TYPE_DRAW_GRID
            result = result or LINE_TYPE_DRAW_LABEL
        } else if (index % 5 == 0) {
            result = LINE_TYPE_SECONDARY or LINE_TYPE_DRAW_GRID
            if (scale >= 2f || canvasViewBox.valueUnit is InchValueUnit) {
                result = result or LINE_TYPE_DRAW_LABEL
            }
        } else {
            result = LINE_TYPE_NORMAL or LINE_TYPE_DRAW_GRID
            if (scale >= 5f || (canvasViewBox.valueUnit is InchValueUnit && scale >= 3f)) {
                result = result or LINE_TYPE_DRAW_LABEL
            }
        }

        return result
    }

    val plusList = mutableListOf<AxisPoint>()
    val minusList = mutableListOf<AxisPoint>()

    /**获取正向坐标需要绘制刻度的像素点坐标位置, 未映射后的坐标*/
    abstract fun getPlusPixelList(canvasViewBox: CanvasViewBox): List<AxisPoint>

    /**获取负向坐标需要绘制刻度的像素点坐标位置, 未映射后的坐标*/
    abstract fun getMinusPixelList(canvasViewBox: CanvasViewBox): List<AxisPoint>
}

/**枚举*/
fun BaseAxis.eachAxisPixelList(block: (index: Int, axisPoint: AxisPoint) -> Unit) {
    plusList.forEachIndexed(block)
    minusList.forEachIndexed(block)
}