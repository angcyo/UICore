package com.angcyo.canvas.graphics

import com.angcyo.canvas.data.ItemDataBean
import com.angcyo.canvas.data.toPixel
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataShapeItem
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.library.ex.adjustWidthHeight
import com.pixplicity.sharp.Sharp

/**
 * 爱心解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/23
 */
class LoveGraphicsParser : PathGraphicsParser() {

    override fun parse(bean: ItemDataBean): DataItem? {
        if (bean.mtype == CanvasConstant.DATA_TYPE_LOVE) {
            val item = DataShapeItem(bean)
            item.updatePaint()

            val dataWidth = bean.width.toPixel()
            val dataHeight = bean.height.toPixel()

            val lovePath =
                Sharp.loadPath("M12 21.593c-5.63-5.539-11-10.297-11-14.402 0-3.791 3.068-5.191 5.281-5.191 1.312 0 4.151.501 5.719 4.457 1.59-3.968 4.464-4.447 5.726-4.447 2.54 0 5.274 1.621 5.274 5.181 0 4.069-5.136 8.625-11 14.402")

            //path
            val dataPath = lovePath.adjustWidthHeight(dataWidth, dataHeight)
            //
            item.addDataPath(dataPath)

            item.drawable = createPathDrawable(item)
            return item
        }
        return super.parse(bean)
    }
}