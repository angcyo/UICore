package com.angcyo.canvas.graphics

import android.graphics.Path
import com.angcyo.canvas.data.ItemDataBean
import com.angcyo.canvas.data.toPixel
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataPathItem
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release

/**
 * 圆角矩形解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/23
 */
class RectGraphicsParser : PathGraphicsParser() {

    override fun parse(bean: ItemDataBean): DataItem? {
        if (bean.mtype == CanvasConstant.DATA_TYPE_RECT) {
            val item = DataPathItem(bean)
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
            item.addDataPath(dataPath)

            item.drawable = createPathDrawable(item)
            return item
        }
        return super.parse(bean)
    }
}