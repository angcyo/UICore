package com.angcyo.canvas.graphics

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.withScale
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.canvas.utils.isLineShape
import com.angcyo.library.annotation.CallPoint
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

    companion object {
        /**是否需要使用gcode填充path*/
        fun isNeedGCodeFill(bean: CanvasProjectItemBean?): Boolean {
            bean ?: return false
            return !bean.isLineShape() &&
                    bean.gcodeFillStep > 0
        }
    }

    /**解析数据*/
    @CallPoint
    fun parse(bean: CanvasProjectItemBean, canvasView: ICanvasView?): DataItem? = null

    //---

    /**当绘制内容包裹在[ScalePictureDrawable]中*/
    fun wrapScalePictureDrawable(width: Int, height: Int, block: Canvas.() -> Unit) =
        ScalePictureDrawable(withPicture(width, height) {
            block()
        })

    /**支持翻转属性的[wrapScalePictureDrawable]
     * [flipX] 是否水平翻转
     * [flipY] 是否垂直翻转
     * */
    fun wrapFlipScalePictureDrawable(
        flipX: Boolean?,
        flipY: Boolean?,
        width: Int,
        height: Int,
        block: Canvas.() -> Unit
    ) = ScalePictureDrawable(withPicture(width, height) {
        val scaleX = if (flipX == true) -1f else 1f
        val scaleY = if (flipY == true) -1f else 1f
        withScale(scaleX, scaleY, width / 2f, height / 2f, block)
    })

    /**包裹一个bitmap对象*/
    fun wrapBitmapDrawable(item: DataItem, bitmap: Bitmap) {
        val width = bitmap.width
        val height = bitmap.height
        item.renderDrawable = wrapScalePictureDrawable(width, height) {
            val rect = acquireTempRectF()
            rect.set(0f, 0f, width.toFloat(), height.toFloat())
            drawBitmap(bitmap, null, rect, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isAntiAlias = true
                isFilterBitmap = true
            })
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