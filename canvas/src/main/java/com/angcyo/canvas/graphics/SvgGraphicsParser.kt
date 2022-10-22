package com.angcyo.canvas.graphics

import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.data.toMm
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataPathItem
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.svg.Svg

/**
 * Svg解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/23
 */
class SvgGraphicsParser : PathGraphicsParser() {

    override fun parse(bean: CanvasProjectItemBean): DataItem? {
        if (bean.mtype == CanvasConstant.DATA_TYPE_SVG) {
            val data = bean.data
            if (!data.isNullOrEmpty()) {
                val item = DataPathItem(bean)
                item.updatePaint()

                val sharpDrawable = Svg.loadSvgPathDrawable(data, -1, null, item.paint, 0, 0)
                if (sharpDrawable != null) {
                    //
                    if (bean.width == 0f) {
                        bean.width = sharpDrawable.pathBounds.width().toMm()
                    }
                    if (bean.height == 0f) {
                        bean.height = sharpDrawable.pathBounds.height().toMm()
                    }
                    //
                    item.addDataPath(sharpDrawable.pathList)
                    item.drawable = createPathDrawable(item)
                    initDataMode(bean, item.paint)
                    return item
                }
            }
        }
        return super.parse(bean)
    }
}