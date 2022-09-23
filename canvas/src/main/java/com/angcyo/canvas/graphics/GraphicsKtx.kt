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
    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

/**添加一个[text]数据渲染*/
fun CanvasDelegate.addTextRender(text: CharSequence?): DataItemRenderer? {
    text ?: return null
    val bean = ItemDataBean()
    bean.mtype = CanvasConstant.DATA_TYPE_TEXT
    bean.text = "$text"
    bean.paintStyle = Paint.Style.FILL.toPaintStyleInt()
    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

/**添加一个二维码数据渲染*/
fun CanvasDelegate.addQRTextRender(text: CharSequence?): DataItemRenderer? {
    text ?: return null
    val bean = ItemDataBean()
    bean.mtype = CanvasConstant.DATA_TYPE_QRCODE
    bean.text = "$text"
    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

/**添加一个条形码数据渲染*/
fun CanvasDelegate.addBarTextRender(text: CharSequence?): DataItemRenderer? {
    text ?: return null
    val bean = ItemDataBean()
    bean.mtype = CanvasConstant.DATA_TYPE_BARCODE
    bean.text = "$text"
    return GraphicsHelper.addRenderItemDataBean(this, bean)
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
    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

/**添加一个椭圆, 宽高相等时, 就是圆了*/
fun CanvasDelegate.addOvalRender(
    @Pixel width: Float = ShapesHelper.defaultWidth,
    @Pixel height: Float = ShapesHelper.defaultHeight
): DataItemRenderer? {
    val bean = ItemDataBean()
    bean.mtype = CanvasConstant.DATA_TYPE_OVAL
    bean.width = width.toMm()
    bean.height = height.toMm()
    bean.paintStyle = Paint.Style.STROKE.toPaintStyleInt()

    bean.rx = width / 2
    bean.ry = height / 2

    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

/**添加一个矩形, 支持圆角*/
fun CanvasDelegate.addRectRender(
    @Pixel width: Float = ShapesHelper.defaultWidth,
    @Pixel height: Float = ShapesHelper.defaultHeight
): DataItemRenderer? {
    val bean = ItemDataBean()
    bean.mtype = CanvasConstant.DATA_TYPE_RECT
    bean.width = width.toMm()
    bean.height = height.toMm()
    bean.paintStyle = Paint.Style.STROKE.toPaintStyleInt()

    //bean.rx = 0f
    //bean.ry = 0f

    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

/**添加一个多边形, 支持任意边数
 * [com.angcyo.canvas.data.ItemDataBean.side]*/
fun CanvasDelegate.addPolygonRender(
    @Pixel width: Float = ShapesHelper.defaultWidth,
    @Pixel height: Float = ShapesHelper.defaultHeight
): DataItemRenderer? {
    val bean = ItemDataBean()
    bean.mtype = CanvasConstant.DATA_TYPE_POLYGON
    bean.width = width.toMm()
    bean.height = height.toMm()
    bean.paintStyle = Paint.Style.STROKE.toPaintStyleInt()

    //多边形的边数[3-50]
    //bean.side = 3

    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

/**添加一个星星, 支持任意边数, 和深度
 * [com.angcyo.canvas.data.ItemDataBean.side]
 * [com.angcyo.canvas.data.ItemDataBean.depth]
 * */
fun CanvasDelegate.addPentagramRender(
    @Pixel width: Float = ShapesHelper.defaultWidth,
    @Pixel height: Float = ShapesHelper.defaultHeight
): DataItemRenderer? {
    val bean = ItemDataBean()
    bean.mtype = CanvasConstant.DATA_TYPE_PENTAGRAM
    bean.width = width.toMm()
    bean.height = height.toMm()
    bean.paintStyle = Paint.Style.STROKE.toPaintStyleInt()

    //多边形的边数[3-50]
    bean.side = 5
    //星星的深度[1-100]
    //bean.depth = 40

    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

/**添加一个爱心*/
fun CanvasDelegate.addLoveRender(
    @Pixel width: Float = ShapesHelper.defaultWidth,
    @Pixel height: Float = ShapesHelper.defaultHeight
): DataItemRenderer? {
    val bean = ItemDataBean()
    bean.mtype = CanvasConstant.DATA_TYPE_LOVE
    bean.width = width.toMm()
    bean.height = height.toMm()
    bean.paintStyle = Paint.Style.STROKE.toPaintStyleInt()
    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

//endregion ---形状---

//region ---矢量---

//endregion ---矢量---