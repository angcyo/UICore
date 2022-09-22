package com.angcyo.canvas.graphics

import com.angcyo.canvas.data.ItemDataBean
import com.angcyo.canvas.items.DataBitmapItem
import com.angcyo.canvas.items.DataItem
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.canvas.utils.parseGCode
import com.angcyo.gcode.GCodeHelper
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
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
                if (bean.imageFilter == CanvasConstant.DATA_MODE_GCODE) {
                    //图片转成了GCode
                    if (bean.src.isNullOrEmpty()) {
                        //bean.src gcode数据
                        return null
                    } else {
                        val gcodeDrawable = GCodeHelper.parseGCode(bean.src) ?: return null
                        val item = DataBitmapItem(bean)
                        item.originBitmap = bean.imageOriginal?.toBitmapOfBase64()
                        item.gCodeDrawable = gcodeDrawable

                        val bound = gcodeDrawable.gCodeBound
                        val width = bound.width().toInt()
                        val height = bound.height().toInt()
                        item.drawable = wrapScalePictureDrawable(width, height) {
                            gcodeDrawable.setBounds(0, 0, width, height)
                            gcodeDrawable.draw(this)
                        }
                        return item
                    }
                } else {
                    //其他
                    val item = DataBitmapItem(bean)
                    item.originBitmap = bean.imageOriginal?.toBitmapOfBase64()
                    item.modifyBitmap = bean.src?.toBitmapOfBase64()

                    val bitmap = item.modifyBitmap ?: item.originBitmap ?: return null
                    val width = bitmap.width
                    val height = bitmap.height
                    item.drawable = wrapScalePictureDrawable(width, height) {
                        val rect = acquireTempRectF()
                        rect.set(0f, 0f, width.toFloat(), height.toFloat())
                        drawBitmap(bitmap, null, rect, null)
                        rect.release()
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