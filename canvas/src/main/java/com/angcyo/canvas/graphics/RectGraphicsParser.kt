package com.angcyo.canvas.graphics

import android.graphics.Path
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataShapeItem
import com.angcyo.laserpacker.LPDataConstant
import com.angcyo.laserpacker.bean.LPElementBean
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.unit.toPixel

/**
 * 圆角矩形解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/23
 */
class RectGraphicsParser : PathGraphicsParser() {

    override fun parse(bean: LPElementBean, canvasView: ICanvasView?): DataItem? {
        if (bean.mtype == LPDataConstant.DATA_TYPE_RECT) {
            val item = DataShapeItem(bean)
            item.updatePaint()

            val dataWidth = bean.width.toPixel()
            val dataHeight = bean.height.toPixel()
            val rx = bean.rx.toPixel()
            val ry = bean.ry.toPixel()

            //path
            val dataPath = Path()
            val tempRect = acquireTempRectF()
            tempRect.set(0f, 0f, dataWidth, dataHeight)
            dataPath.addRoundRect(tempRect, rx, ry, Path.Direction.CW)
            tempRect.release()
            //
            item.addDataPath(dataPath.flipEngravePath(bean))

            createPathDrawable(item, canvasView) ?: return null
            initDataModeWithPaintStyle(bean, item.itemPaint)
            return item
        }
        return super.parse(bean, canvasView)
    }
}