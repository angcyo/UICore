package com.angcyo.canvas.graphics

import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataPathItem
import com.angcyo.laserpacker.LPDataConstant
import com.angcyo.laserpacker.bean.LPElementBean
import com.angcyo.library.unit.toMm
import com.angcyo.library.utils.isSvgContent
import com.angcyo.svg.Svg

/**
 * Svg解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/23
 */
class SvgGraphicsParser : PathGraphicsParser() {

    override fun parse(bean: LPElementBean, canvasView: ICanvasView?): DataItem? {
        if (bean.mtype == LPDataConstant.DATA_TYPE_SVG) {
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
                            val width = sharpDrawable.pathBounds.width()
                            bean.width = width.toMm()
                            sharpDrawable.sharpPicture?.let {
                                bean.scaleX = it.bounds.width() / width
                            }
                        }
                        if (bean.height == null) {
                            val height = sharpDrawable.pathBounds.height()
                            bean.height = height.toMm()
                            sharpDrawable.sharpPicture?.let {
                                bean.scaleY = it.bounds.height() / height
                            }
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