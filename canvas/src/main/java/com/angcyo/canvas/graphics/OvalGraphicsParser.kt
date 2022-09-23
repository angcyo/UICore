package com.angcyo.canvas.graphics

import android.graphics.Path
import com.angcyo.canvas.data.ItemDataBean
import com.angcyo.canvas.data.toPixel
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataShapeItem
import com.angcyo.canvas.utils.CanvasConstant

/**
 * 椭圆解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/23
 */
class OvalGraphicsParser : PathGraphicsParser() {

    override fun parse(bean: ItemDataBean): DataItem? {
        if (bean.mtype == CanvasConstant.DATA_TYPE_OVAL) {
            val item = DataShapeItem(bean)
            item.updatePaint()

            //rx ry 支持
            if (bean.width == 0f) {
                bean.width = bean.rx * 2
            }
            if (bean.height == 0f) {
                bean.height = bean.ry * 2
            }

            val dataWidth = bean.width.toPixel()
            val dataHeight = bean.height.toPixel()

            //path
            val dataPath = Path()
            dataPath.addOval(0f, 0f, dataWidth, dataHeight, Path.Direction.CW)
            item.addDataPath(dataPath)

            item.drawable = createPathDrawable(item)
            return item
        }
        return super.parse(bean)
    }
}