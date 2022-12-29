package com.angcyo.canvas.graphics

import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.data.toMm
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataPathItem
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.canvas.utils.isSvgContent
import com.angcyo.svg.Svg

/**
 * Svg解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/23
 */
class SvgGraphicsParser : PathGraphicsParser() {

    override fun parse(bean: CanvasProjectItemBean, canvasView: ICanvasView?): DataItem? {
        if (bean.mtype == CanvasConstant.DATA_TYPE_SVG) {
            val data = bean.data
            if (!data.isNullOrEmpty()) {
                val item = DataPathItem(bean)
                item.updatePaint()

                if (data.isSvgContent()) {
                    //svg标签数据
                    val sharpDrawable =
                        Svg.loadSvgPathDrawable(data, -1, null, item.itemPaint, 0, 0)
                    if (sharpDrawable != null) {
                        //
                        if (bean.width == null) {
                            bean.width = sharpDrawable.pathBounds.width().toMm()
                        }
                        if (bean.height == null) {
                            bean.height = sharpDrawable.pathBounds.height().toMm()
                        }
                        //
                        item.addDataPath(sharpDrawable.pathList.flipEngravePath(bean))
                        createPathDrawable(item, canvasView) ?: return null
                        initDataModeWithPaintStyle(bean, item.itemPaint)
                        return item
                    }
                } else {
                    //svg纯路径数据
                    return parsePathItem(bean, data, canvasView)
                }
            }
        }
        return super.parse(bean, canvasView)
    }
}