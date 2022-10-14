package com.angcyo.canvas.graphics

import com.angcyo.canvas.LinePath
import com.angcyo.canvas.data.ItemDataBean
import com.angcyo.canvas.data.toPixel
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataShapeItem
import com.angcyo.canvas.utils.CanvasConstant

/**
 * 线段解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/23
 */
class LineGraphicsParser : PathGraphicsParser() {

    override fun parse(bean: ItemDataBean): DataItem? {
        if (bean.mtype == CanvasConstant.DATA_TYPE_LINE) {
            val item = DataShapeItem(bean)
            item.updatePaint()

            val lineLength = bean.width.toPixel()

            //path
            val linePath = LinePath()
            linePath.moveTo(0f, 0f)
            linePath.lineTo(lineLength, 0f)
            item.addDataPath(linePath)

            item.drawable = createPathDrawable(item)

            //initDataMode(bean, item.paint)
            bean._dataMode = CanvasConstant.DATA_MODE_GCODE //强制使用GCode
            return item
        }
        return super.parse(bean)
    }
}