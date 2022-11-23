package com.angcyo.canvas.graphics

import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.data.toMm
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataPathItem
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.gcode.GCodeHelper

/**
 * GCode解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/23
 */
class GCodeGraphicsParser : PathGraphicsParser() {

    override fun parse(bean: CanvasProjectItemBean): DataItem? {
        if (bean.mtype == CanvasConstant.DATA_TYPE_GCODE) {
            val data = bean.data
            if (!data.isNullOrEmpty()) {
                val item = DataPathItem(bean)
                item.updatePaint()

                val gCodeDrawable = GCodeHelper.parseGCode(data, item.paint)
                if (gCodeDrawable != null) {
                    //
                    if (bean._width == 0f) {
                        bean.width = gCodeDrawable.gCodeBound.width().toMm()
                    }
                    if (bean._height == 0f) {
                        bean.height = gCodeDrawable.gCodeBound.height().toMm()
                    }
                    //
                    item.addDataPath(gCodeDrawable.gCodePath)
                    item.drawable = createPathDrawable(item) ?: return null

                    initDataModeWithPaintStyle(bean, item.paint)
                    return item
                }
            }
        }
        return super.parse(bean)
    }
}