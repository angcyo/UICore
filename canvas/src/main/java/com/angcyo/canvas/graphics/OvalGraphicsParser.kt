package com.angcyo.canvas.graphics

import android.graphics.Path
import android.graphics.RectF
import android.os.Build
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataShapeItem
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.library.unit.toPixel
import com.angcyo.vector.VectorWriteHandler

/**
 * 椭圆解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/23
 */
class OvalGraphicsParser : PathGraphicsParser() {

    override fun parse(bean: CanvasProjectItemBean, canvasView: ICanvasView?): DataItem? {
        if (bean.mtype == CanvasConstant.DATA_TYPE_OVAL) {
            val item = DataShapeItem(bean)
            item.updatePaint()

            //rx ry 支持
            if (bean._width == 0f) {
                bean.width = bean.rx * 2
            }
            if (bean._height == 0f) {
                bean.height = bean.ry * 2
            }

            val dataWidth = bean.width.toPixel()
            val dataHeight = bean.height.toPixel()

            //path
            val dataPath = Path()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dataPath.addOval(
                    0f,
                    0f,
                    dataWidth,
                    dataHeight,
                    VectorWriteHandler.DEFAULT_PATH_DIRECTION
                )
            } else {
                dataPath.addOval(
                    RectF(0f, 0f, dataWidth, dataHeight),
                    VectorWriteHandler.DEFAULT_PATH_DIRECTION
                )
            }
            item.addDataPath(dataPath.flipEngravePath(bean))

            createPathDrawable(item, canvasView) ?: return null

            initDataModeWithPaintStyle(bean, item.itemPaint)

            return item
        }
        return super.parse(bean, canvasView)
    }
}