package com.angcyo.canvas.graphics

import android.graphics.Bitmap
import android.graphics.Paint
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.data.toPaintStyle
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.library.unit.toMm
import com.angcyo.qrcode.createBarCode
import com.angcyo.qrcode.createQRCode
import com.google.zxing.BarcodeFormat

/**
 * 二维码/条形码 解析器
 *
 * [com.google.zxing.BarcodeFormat.QR_CODE]
 * [com.google.zxing.BarcodeFormat.CODE_128]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/22
 */
class CodeGraphicsParser : IGraphicsParser {

    override fun parse(bean: CanvasProjectItemBean, canvasView: ICanvasView?): DataItem? {
        if (!bean.text.isNullOrEmpty()) {
            if (bean.mtype == CanvasConstant.DATA_TYPE_QRCODE) {
                bean.text?.createQRCode()?.let { bitmap ->
                    bean.coding = "${BarcodeFormat.QR_CODE}".lowercase()
                    return handleBitmap(bean, bitmap.flipEngraveBitmap(bean))
                }
            } else if (bean.mtype == CanvasConstant.DATA_TYPE_BARCODE) {
                bean.text?.createBarCode()?.let { bitmap ->
                    bean.coding = "${BarcodeFormat.CODE_128}".lowercase()
                    return handleBitmap(bean, bitmap.flipEngraveBitmap(bean))
                }
            }
        }
        return super.parse(bean, canvasView)
    }

    /**处理图片*/
    fun handleBitmap(bean: CanvasProjectItemBean, bitmap: Bitmap): DataItem {
        val item = DataItem(bean)
        wrapBitmapDrawable(item, bitmap)

        if (bean._width <= 0) {
            bean.width = bitmap.width.toMm()
        }
        if (bean._height <= 0) {
            bean.height = bitmap.height.toMm()
        }

        if (bean.paintStyle.toPaintStyle() == Paint.Style.STROKE) {
            //GCode
            bean._dataMode = CanvasConstant.DATA_MODE_GCODE
        } else {
            bean._dataMode = CanvasConstant.DATA_MODE_BLACK_WHITE
        }

        return item
    }

}