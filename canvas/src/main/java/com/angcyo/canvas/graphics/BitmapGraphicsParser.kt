package com.angcyo.canvas.graphics

import com.angcyo.canvas.data.ItemDataBean
import com.angcyo.canvas.data.toMm
import com.angcyo.canvas.items.data.DataBitmapItem
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.canvas.utils.parseGCode
import com.angcyo.gcode.GCodeHelper
import com.angcyo.library.ex.toBitmapOfBase64

/**
 * 图片数据解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */
class BitmapGraphicsParser : IGraphicsParser {

    override fun parse(bean: ItemDataBean): DataItem? {
        if (bean.mtype == CanvasConstant.DATA_TYPE_BITMAP && !bean.imageOriginal.isNullOrEmpty()) {
            try {
                val originBitmap = bean.imageOriginal?.toBitmapOfBase64()
                originBitmap?.let {
                    bean.width = originBitmap.width.toMm()
                    bean.height = originBitmap.height.toMm()
                }
                if (bean.imageFilter == CanvasConstant.DATA_MODE_GCODE) {
                    //图片转成了GCode
                    val gcode = bean.data ?: bean.src
                    if (gcode.isNullOrEmpty()) {
                        //bean.src gcode数据
                        return null
                    } else {
                        val gcodeDrawable = GCodeHelper.parseGCode(gcode) ?: return null
                        val item = DataBitmapItem(bean)
                        item.originBitmap = originBitmap
                        item.gCodeDrawable = gcodeDrawable

                        val bound = gcodeDrawable.gCodeBound
                        val width = bound.width().toInt()
                        val height = bound.height().toInt()
                        item.drawable = wrapScalePictureDrawable(width, height) {
                            gcodeDrawable.setBounds(0, 0, width, height)
                            gcodeDrawable.draw(this)
                        }

                        bean._dataMode = CanvasConstant.DATA_MODE_GCODE

                        return item
                    }
                } else {
                    //其他
                    val item = DataBitmapItem(bean)
                    item.originBitmap = originBitmap
                    item.modifyBitmap = bean.src?.toBitmapOfBase64()

                    val bitmap = item.modifyBitmap ?: item.originBitmap ?: return null
                    wrapBitmap(item, bitmap)

                    bean._dataMode = if (bean.imageFilter == CanvasConstant.DATA_MODE_DITHERING) {
                        CanvasConstant.DATA_MODE_DITHERING
                    } else {
                        CanvasConstant.DATA_MODE_BLACK_WHITE
                    }

                    return item
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return super.parse(bean)
    }

}