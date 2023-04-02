package com.angcyo.canvas.graphics

import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataShapeItem
import com.angcyo.canvas.utils.ShapesHelper
import com.angcyo.laserpacker.LPDataConstant
import com.angcyo.laserpacker.bean.LPElementBean
import com.angcyo.library.unit.toPixel

/**
 * 星星解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/23
 */
class PentagramGraphicsParser : PathGraphicsParser() {

    override fun parse(bean: LPElementBean, canvasView: ICanvasView?): DataItem? {
        if (bean.mtype == LPDataConstant.DATA_TYPE_PENTAGRAM && bean.side >= 3) {
            val item = DataShapeItem(bean)
            item.updatePaint()

            val dataWidth = bean.width.toPixel()
            val dataHeight = bean.height.toPixel()
            val side = bean.side //边数
            val depth = bean.depth //深度, 决定内圈的半径 (内圈半径 = 固定外圈半径 * (1 - [depth] / 100))

            //path
            val dataPath = ShapesHelper.pentagramPath(side, dataWidth, dataHeight, depth)
            //
            item.addDataPath(dataPath.flipEngravePath(bean))

            createPathDrawable(item, canvasView) ?: return null
            initDataModeWithPaintStyle(bean, item.itemPaint)
            return item
        }
        return super.parse(bean, canvasView)
    }
}