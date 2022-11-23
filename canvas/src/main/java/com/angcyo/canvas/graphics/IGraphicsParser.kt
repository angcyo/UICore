package com.angcyo.canvas.graphics

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.withPicture

/**
 * 解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */
interface IGraphicsParser {

    /**解析数据*/
    fun parse(bean: CanvasProjectItemBean): DataItem? = null

    //---

    /**当绘制内容包裹在[ScalePictureDrawable]中*/
    fun wrapScalePictureDrawable(width: Int, height: Int, block: Canvas.() -> Unit) =
        ScalePictureDrawable(withPicture(width, height) {
            block()
        })

    /**包裹一个bitmap对象*/
    fun wrapBitmap(item: DataItem, bitmap: Bitmap) {
        val width = bitmap.width
        val height = bitmap.height
        item.drawable = wrapScalePictureDrawable(width, height) {
            val rect = acquireTempRectF()
            rect.set(0f, 0f, width.toFloat(), height.toFloat())
            drawBitmap(bitmap, null, rect, Paint(Paint.ANTI_ALIAS_FLAG))
            rect.release()
        }
    }

    /**简单的初始化一下数据模式*/
    fun initDataModeWithPaintStyle(bean: CanvasProjectItemBean, paint: Paint) {
        bean._dataMode = if (paint.style == Paint.Style.STROKE) {
            CanvasConstant.DATA_MODE_GCODE
        } else {
            CanvasConstant.DATA_MODE_BLACK_WHITE
        }
    }

}