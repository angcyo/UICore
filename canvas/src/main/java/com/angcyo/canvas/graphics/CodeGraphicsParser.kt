package com.angcyo.canvas.graphics

import android.graphics.Bitmap
import com.angcyo.canvas.data.ItemDataBean
import com.angcyo.canvas.data.toMm
import com.angcyo.canvas.items.DataItem
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.qrcode.createBarCode
import com.angcyo.qrcode.createQRCode

/**
 * 二维码/条形码 解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/22
 */
class CodeGraphicsParser : IGraphicsParser {

    override fun parse(bean: ItemDataBean): DataItem? {
        if (!bean.text.isNullOrEmpty()) {
            if (bean.mtype == CanvasConstant.DATA_TYPE_QRCODE) {
                bean.text?.createQRCode()?.let { bitmap ->
                    return handleBitmap(bean, bitmap)
                }
            } else if (bean.mtype == CanvasConstant.DATA_TYPE_BARCODE) {
                bean.text?.createBarCode()?.let { bitmap ->
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