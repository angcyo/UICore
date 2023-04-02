package com.angcyo.canvas.graphics

import com.angcyo.canvas.LinePath
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataShapeItem
import com.angcyo.laserpacker.LPDataConstant
import com.angcyo.laserpacker.bean.LPElementBean
import com.angcyo.library.unit.toPixel

/**
 * 线段解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/23
 */
class LineGraphicsParser : PathGraphicsParser() {

    override fun parse(bean: LPElementBean, canvasView: ICanvasView?): DataItem? {
        if (bean.mtype == LPDataConstant.DATA_TYPE_LINE) {
            val item = DataShapeItem(bean)
            item.updatePaint()

            val lineLength = bean.width.toPixel()
            bean.height = 0f//去掉高度属性, 防止自动雕刻时渲染异常

            //path
            val linePath = LinePath()
            linePath.moveTo(0f, 0f)
            linePath.lineTo(lineLength, 0f)
            item.addDataPath(linePath.flipEngravePath(bean))

            createPathDrawable(item, canvasView) ?: return null

            //initDataMode(bean, item.paint)
            /*bean._dataMode = if (item.paint.style == Paint.Style.STROKE) {
                //线条描边, 使用黑白画
                CanvasConstant.DATA_MODE_BLACK_WHITE
            } else {
                CanvasConstant.DATA_MODE_GCODE
            }*/
            bean._dataMode = LPDataConstant.DATA_MODE_GCODE //强制使用GCode
            return item
        }
        return super.parse(bean, canvasView)
    }
}