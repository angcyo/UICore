package com.angcyo.canvas.graphics

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.core.IRenderer
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.data.toMm
import com.angcyo.canvas.data.toPaintStyleInt
import com.angcyo.canvas.items.data.DataItemRenderer
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.canvas.utils.ShapesHelper
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.flip
import com.angcyo.library.ex.toBase64Data

/**
 * 扩展方法
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */

//region ---图/文---

fun Bitmap?.toBitmapItemData(action: CanvasProjectItemBean.() -> Unit = {}): CanvasProjectItemBean? {
    this ?: return null
    val bean = CanvasProjectItemBean()
    bean.mtype = CanvasConstant.DATA_TYPE_BITMAP
    bean.imageOriginal = toBase64Data()
    bean.action()
    return bean
}

/**添加一个[bitmap]数据渲染*/
fun CanvasDelegate.addBitmapRender(
    bitmap: Bitmap?,
    action: CanvasProjectItemBean.() -> Unit = {}
): DataItemRenderer? {
    bitmap ?: return null
    val bean = bitmap.toBitmapItemData(action) ?: return null
    bean.action()
    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

fun CharSequence?.toTextItemData(): CanvasProjectItemBean? {
    this ?: return null
    val bean = CanvasProjectItemBean()
    bean.mtype = CanvasConstant.DATA_TYPE_TEXT
    bean.text = "$this"
    bean.paintStyle = Paint.Style.FILL.toPaintStyleInt()
    return bean
}

/**添加一个[text]数据渲染
 *
 * [CanvasConstant.DATA_TYPE_TEXT]
 * [CanvasConstant.DATA_TYPE_BARCODE]
 * [CanvasConstant.DATA_TYPE_QRCODE]
 * */
fun CanvasDelegate.addTextRender(
    text: CharSequence?,
    type: Int = CanvasConstant.DATA_TYPE_TEXT
): DataItemRenderer? {
    text ?: return null
    val bean = CanvasProjectItemBean()
    bean.mtype = type
    bean.text = "$text"
    bean.charSpacing = 0.5f //默认字间距
    bean.lineSpacing = bean.charSpacing //默认行间距
    bean.paintStyle = Paint.Style.FILL.toPaintStyleInt()
    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

//endregion ---图/文---

//region ---形状---

/**添加一根线
 * [length] 线的长度
 * */
fun CanvasDelegate.addLineRender(@Pixel length: Float = ShapesHelper.defaultWidth): DataItemRenderer? {
    val bean = CanvasProjectItemBean()
    bean.mtype = CanvasConstant.DATA_TYPE_LINE
    bean.width = length.toMm()
    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

/**添加一个椭圆, 宽高相等时, 就是圆了*/
fun CanvasDelegate.addOvalRender(
    @Pixel width: Float = ShapesHelper.defaultWidth,
    @Pixel height: Float = ShapesHelper.defaultHeight
): DataItemRenderer? {
    val bean = CanvasProjectItemBean()
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
    val bean = CanvasProjectItemBean()
    bean.mtype = CanvasConstant.DATA_TYPE_RECT
    bean.width = width.toMm()
    bean.height = height.toMm()
    bean.paintStyle = Paint.Style.STROKE.toPaintStyleInt()

    //bean.rx = 0f
    //bean.ry = 0f

    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

/**添加一个多边形, 支持任意边数
 * [com.angcyo.canvas.data.CanvasProjectItemBean.side]*/
fun CanvasDelegate.addPolygonRender(
    @Pixel width: Float = ShapesHelper.defaultWidth,
    @Pixel height: Float = ShapesHelper.defaultHeight
): DataItemRenderer? {
    val bean = CanvasProjectItemBean()
    bean.mtype = CanvasConstant.DATA_TYPE_POLYGON
    bean.width = width.toMm()
    bean.height = height.toMm()
    bean.paintStyle = Paint.Style.STROKE.toPaintStyleInt()

    //多边形的边数[3-50]
    //bean.side = 3

    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

/**添加一个星星, 支持任意边数, 和深度
 * [com.angcyo.canvas.data.CanvasProjectItemBean.side]
 * [com.angcyo.canvas.data.CanvasProjectItemBean.depth]
 * */
fun CanvasDelegate.addPentagramRender(
    @Pixel width: Float = ShapesHelper.defaultWidth,
    @Pixel height: Float = ShapesHelper.defaultHeight
): DataItemRenderer? {
    val bean = CanvasProjectItemBean()
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
    val bean = CanvasProjectItemBean()
    bean.mtype = CanvasConstant.DATA_TYPE_LOVE
    bean.width = width.toMm()
    bean.height = height.toMm()
    bean.paintStyle = Paint.Style.STROKE.toPaintStyleInt()
    return GraphicsHelper.addRenderItemDataBean(this, bean)
}

//endregion ---形状---

//region ---矢量---

/**SVG数据转[CanvasProjectItemBean]*/
fun String?.toSvgItemData(): CanvasProjectItemBean? {
    this ?: return null
    val bean = CanvasProjectItemBean()
    bean.mtype = CanvasConstant.DATA_TYPE_SVG
    bean.data = this
    bean.paintStyle = Paint.Style.STROKE.toPaintStyleInt()
    return bean
}

/**添加一个SVG渲染*/
fun CanvasDelegate.addSvgRender(svg: String?) =
    GraphicsHelper.addRenderItemDataBean(this, svg.toSvgItemData())

/**GCode数据转[CanvasProjectItemBean]*/
fun String?.toGCodeItemData(): CanvasProjectItemBean? {
    this ?: return null
    val bean = CanvasProjectItemBean()
    bean.mtype = CanvasConstant.DATA_TYPE_GCODE
    bean.data = this
    bean.paintStyle = Paint.Style.STROKE.toPaintStyleInt()
    return bean
}

/**添加一个GCode渲染*/
fun CanvasDelegate.addGCodeRender(gcode: String?) =
    GraphicsHelper.addRenderItemDataBean(this, gcode.toGCodeItemData())

//endregion ---矢量---

//region ---算法---

/**翻转图片*/
fun Bitmap.flipEngraveBitmap(bean: CanvasProjectItemBean) = flip(bean._flipScaleX, bean._flipScaleY)

/**翻转Path*/
fun Path.flipEngravePath(bean: CanvasProjectItemBean) = flip(bean._flipScaleX, bean._flipScaleY)

fun List<Path>.flipEngravePath(bean: CanvasProjectItemBean) =
    flip(bean._flipScaleX, bean._flipScaleY)

/**数据索引
 * [com.angcyo.canvas.data.CanvasProjectItemBean.index]*/
val IRenderer.dataItemIndex: Int?
    get() = if (this is DataItemRenderer) {
        this.rendererItem?.dataBean?.index
    } else {
        null
    }

//endregion ---算法---