package com.angcyo.canvas.graphics

import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataShapeItem
import com.angcyo.canvas.utils.ShapesHelper
import com.angcyo.laserpacker.LPDataConstant
import com.angcyo.laserpacker.bean.LPElementBean
import com.angcyo.library.unit.toPixel

/**
 * 多边形解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/23
 */
class PolygonGraphicsParser : PathGraphicsParser() {

    override fun parse(bean: LPElementBean, canvasView: ICanvasView?): DataItem? {
        if (bean.mtype == LPDataConstant.DATA_TYPE_POLYGON && bean.side >= 3) {
            val item = DataShapeItem(bean)
            item.updatePaint()

            val dataWidth = bean.width.toPixel()
            val dataHeight = bean.height.toPixel()
            val side = bean.side //边数

            //path
            val dataPath = ShapesHelper.polygonPath(side, dataWidth, dataHeight)
            //
            item.addDataPath(dataPath.flipEngravePath(bean))

            createPathDrawable(item, canvasView) ?: return null
            initDataModeWithPaintStyle(bean, item.itemPaint)
            return item
        }
        return super.parse(bean, canvasView)
    }

}