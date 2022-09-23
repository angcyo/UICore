package com.angcyo.canvas.graphics

import android.graphics.Bitmap
import android.graphics.Paint
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.data.ItemDataBean
import com.angcyo.canvas.data.toMm
import com.angcyo.canvas.data.toPaintStyleInt
import com.angcyo.canvas.items.data.DataItemRenderer
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.canvas.utils.ShapesHelper
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.toBase64Data

/**
 * 扩展方法
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */

//region ---图/文---

/**添加一个[bitmap]数据渲染*/
fun CanvasDelegate.addBitmapRender(bitmap: Bitmap?): DataItemRenderer? {
    bitmap ?: return null
    val bean = ItemDataBean()
    bean.mtype = CanvasConstant.DATA_TYPE_BITMAP
    bean.imageOriginal = bitmap.toBase64Data()
    return GraphicsHelper.renderItemData(this, bean)
}

/**添加一个[text]数据渲染*/
fun CanvasDelegate.addTextRender(text: CharSequence?): DataItemRenderer? {
    text ?: return null
    val bean = ItemDataBean()
    bean.mtype = CanvasConstant.DATA_TYPE_TEXT
    bean.text = "$text"
    bean.paintStyle = Paint.Style.FILL.toPaintStyleInt()
    return GraphicsHelper.renderItemData(this, bean)
}

/**添加一个二维码数据渲染*/
fun CanvasDelegate.addQRTextRender(text: CharSequence?): DataItemRenderer? {
    text ?: return null
    val bean = ItemDataBean()
    bean.mtype = CanvasConstant.DATA_TYPE_QRCODE
    bean.text = "$text"
    return GraphicsHelper.renderItemData(this, bean)
}

/**添加一个条形码数据渲染*/
fun CanvasDelegate.addBarTextRender(text: CharSequence?): DataItemRenderer? {
    text ?: return null
    val bean = ItemDataBean()
    bean.mtype = CanvasConstant.DATA_TYPE_BARCODE
    bean.text = "$text"
    return GraphicsHelper.renderItemData(this, bean)
}

//endregion ---图/文---

//region ---形状---

/**添加一根线
 * [length] 线的长度
 * */
fun CanvasDelegate.addLineRender(@Pixel length: Float = ShapesHelper.defaultWidth): DataItemRenderer? {
    val bean = ItemDataBean()
    bean.mtype = CanvasConstant.DATA_TYPE_LINE
    bean.width = length.toMm()
    return GraphicsHelper.renderItemData(this, bean)
}

//endregion ---形状---

//region ---矢量---

//endregion ---矢量---