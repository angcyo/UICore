package com.angcyo.canvas.graphics

import android.graphics.Bitmap
import com.angcyo.canvas.data.ItemDataBean
import com.angcyo.canvas.data.toMm
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.utils.CanvasConstant
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

    override fun parse(bean: ItemDataBean): DataItem? {
        if (!bean.text.isNullOrEmpty()) {
            if (bean.mtype == CanvasConstant.DATA_TYPE_QRCODE) {
                bean.text?.createQRCode()?.let { bitmap ->
                    bean.coding = "${BarcodeFormat.QR_CODE}".lowercase()
                    return handleBitmap(bean, bitmap)
                }
            } else if (bean.mtype == CanvasConstant.DATA_TYPE_BARCODE) {
                bean.text?.createBarCode()?.let { bitmap ->
                    bean.coding = "${BarcodeFormat.CODE_128}".lowercase()
                    return handleBitmap(bean, bitmap)
                }
            }
        }
        return super.parse(bean)
    }

    /**处理图片*/
    fun handleBitmap(bean: ItemDataBean, bitmap: Bitmap): DataItem {
        val item = DataItem(bean)
        wrapBitmap(item, bitmap)
        bean.width = bitmap.width.toMm()
        bean.height = bitmap.height.toMm()
        return item
    }

}