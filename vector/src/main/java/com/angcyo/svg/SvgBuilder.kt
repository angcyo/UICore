package com.angcyo.svg

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Path.FillType
import android.graphics.RectF
import androidx.core.graphics.scale
import androidx.core.graphics.scaleMatrix
import com.angcyo.library.annotation.ConfigProperty
import com.angcyo.library.annotation.DSL
import com.angcyo.library.component.hawk.LibLpHawkKeys
import com.angcyo.library.ex.bitmapInt
import com.angcyo.library.ex.decimal
import com.angcyo.library.ex.getScaleX
import com.angcyo.library.ex.getScaleY
import com.angcyo.library.ex.getSkewX
import com.angcyo.library.ex.getSkewY
import com.angcyo.library.ex.getTranslateX
import com.angcyo.library.ex.getTranslateY
import com.angcyo.library.ex.isNil
import com.angcyo.library.ex.nowTimeString
import com.angcyo.library.ex.toBase64Data
import com.angcyo.library.ex.toHexColorString
import com.angcyo.library.unit.IValueUnit
import com.angcyo.library.unit.toRectUnit
import com.angcyo.library.unit.toUnitFromPixel
import com.angcyo.toSVGStrokeContentVectorStr

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2025/02/18
 *
 * 用来输出svg xml文档数据
 * - SVG：可缩放矢量图形 https://developer.mozilla.org/zh-CN/docs/Web/SVG
 * - SVG 元素参考 https://developer.mozilla.org/zh-CN/docs/Web/SVG/Element
 * - SVG 属性参考 https://developer.mozilla.org/zh-CN/docs/Web/SVG/Attribute
 */
class SvgBuilder {

    companion object {
        /**[SvgBuilder.svgHeaderAnnotation]*/
        var customSvgHeaderAnnotation: String? = null
    }

    /**svg xml头部*/
    @ConfigProperty
    var svgHeader: String = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"

    /** [svgHeader]头部下的注释描述字符串*/
    @ConfigProperty
    var svgHeaderAnnotation: String? =
        "\n<!-- Created with angcyo (https://www.github.com/angcyo) -->\n"

    /**额外放在svg中根节点的属性*/
    @ConfigProperty
    var attributes: Map<String, Any?>? = null

    /**浮点小数点位数*/
    @ConfigProperty
    var digits: Int = 15

    @ConfigProperty
    var version: Int = 1

    @ConfigProperty
    var author: String = "angcyo"

    private val buffer: StringBuffer = StringBuffer()
    private var _isEnd: Boolean = true

    /**
     * 写入[viewBox]属性
     * [writeUnitTransform] 是否将[boundsUnit]缩放比例写入`transform`
     * 通常在生成雕刻数据时, 才需要使用此属性
     *
     * viewBox 属性允许指定一个给定的一组图形伸展以适应特定的容器元素。
     *
     * viewBox 属性的值是一个包含 4 个参数的列表 `min-x`, `min-y`, `width` and `height`，以空格或者逗号分隔开，在用户空间中指定一个矩形区域映射到给定的元素，查看属性preserveAspectRatio。
     *
     * 不允许宽度和高度为负值，0 则禁用元素的呈现。
     * https://developer.mozilla.org/zh-CN/docs/Web/SVG/Attribute/viewBox
     *
     * mac/windows上 1mm->3.7777px
     * 1mm = 1/25.4 * 96 px ≈ 3.779527559 px
     * */
    fun writeViewBox(
        bounds: RectF?,
        boundsUnit: IValueUnit? = IValueUnit.MM_UNIT,
        boundsMm: RectF? = null,
        writeProperty: Boolean = false,
        writeUnitTransform: Boolean = false
    ) {
        buffer.append(svgHeader)
        (customSvgHeaderAnnotation ?: svgHeaderAnnotation)?.let { it ->
            if (it.contains("<!")) {
                buffer.append(it)
            } else {
                buffer.append("<!--$it-->")
            }
        }
        buffer.append("<svg xmlns=\"http://www.w3.org/2000/svg\" ")
        buffer.append("xmlns:xlink=\"http://www.w3.org/1999/xlink\" ")
        buffer.append("xmlns:acy=\"https://www.github.com/angcyo\" ")

        if (bounds != null) {
            buffer.append(
                "viewBox=\"${formatValue(bounds.left)} ${formatValue(bounds.top)} " +
                        "${formatValue(bounds.width())} ${formatValue(bounds.height())}\" "
            )
            boundsMm ?: bounds.toRectUnit(boundsUnit)
        }

        if (boundsMm != null && writeProperty) {
            buffer.append("acy:x=\"${formatValue(boundsMm.left)}${IValueUnit.MM_UNIT.suffix}\" ")
            buffer.append("acy:y=\"${formatValue(boundsMm.top)}${IValueUnit.MM_UNIT.suffix}\" ")
            buffer.append("acy:width=\"${formatValue(boundsMm.width())}${IValueUnit.MM_UNIT.suffix}\" ")
            buffer.append("acy:height=\"${formatValue(boundsMm.height())}${IValueUnit.MM_UNIT.suffix}\" ")
        }
        if (boundsUnit != null) {
            if (writeUnitTransform) {
                val scale = 1.toUnitFromPixel(boundsUnit)
                writeTransform(sx = scale, sy = scale)
            }
        }

        buffer.append("acy:author=\"$author\" acy:version=\"$version\" acy:build=\"${nowTimeString()}\" ")
        attributes?.forEach { (key, value) ->
            if (key.contains(":")) {
                buffer.append("$key=\"$value\" ")
            } else if (value != null) {
                buffer.append("acy:$key=\"$value\" ")
            }
        }
        buffer.append(">")
        _isEnd = false
    }

    /**结束*/
    fun writeEnd() {
        if (!_isEnd) {
            buffer.append("</svg>")
        }
    }

    //region --元素--

    /**
     * 写入[line]元素
     *
     * https://developer.mozilla.org/zh-CN/docs/Web/SVG/Element/line
     * */
    fun writeLine(
        x1: Float? = null,
        y1: Float? = null,
        x2: Float? = null,
        y2: Float? = null,
        transform: Matrix? = null,
        fillRule: String = "evenodd",
        fill: Boolean? = null,
        fillColor: Int? = null,
        stroke: Boolean? = null,
        strokeColor: Int? = null,
        strokeWidth: Float? = null,
        id: String? = null,
        name: String? = null
    ) {
        buffer.append("<line ")
        writeId(id = id, name = name)
        x1?.let { buffer.append("x1=\"${formatValue(it)}\" ") }
        y1?.let { buffer.append("y1=\"${formatValue(it)}\" ") }
        x2?.let { buffer.append("x2=\"${formatValue(it)}\" ") }
        y2?.let { buffer.append("y2=\"${formatValue(it)}\" ") }
        writeStyle(
            fillRule = fillRule,
            fill = fill,
            fillColor = fillColor,
            stroke = stroke,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth
        )
        writeTransform(transform = transform)
        buffer.append(" />")
    }

    /**
     * 写入[oval]元素
     *
     * https://developer.mozilla.org/zh-CN/docs/Web/SVG/Element/ellipse
     **/
    fun writeOval(
        cx: Float? = null,
        cy: Float? = null,
        rx: Float? = null,
        ry: Float? = null,
        transform: Matrix? = null,
        fillRule: String = "evenodd",
        fill: Boolean? = null,
        fillColor: Int? = null,
        stroke: Boolean? = null,
        strokeColor: Int? = null,
        strokeWidth: Float? = null,
        id: String? = null,
        name: String? = null
    ) {
        buffer.append("<ellipse ")
        writeId(id = id, name = name)
        cx?.let { buffer.append("cx=\"${formatValue(it)}\" ") }
        cy?.let { buffer.append("cy=\"${formatValue(it)}\" ") }
        rx?.let { buffer.append("rx=\"${formatValue(it)}\" ") }
        ry?.let { buffer.append("ry=\"${formatValue(it)}\" ") }
        writeStyle(
            fillRule = fillRule,
            fill = fill,
            fillColor = fillColor,
            stroke = stroke,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth
        )
        writeTransform(transform = transform)
        buffer.append(" />")
    }

    /**
     * 写入[rect]元素
     *
     * https://developer.mozilla.org/zh-CN/docs/Web/SVG/Element/rect
     **/
    fun writeRect(
        x: Float? = null,
        y: Float? = null,
        width: Float,
        height: Float,
        rx: Float? = null,
        ry: Float? = null,
        transform: Matrix? = null,
        fillRule: String = "evenodd",
        fill: Boolean? = null,
        fillColor: Int? = null,
        stroke: Boolean? = null,
        strokeColor: Int? = null,
        strokeWidth: Float? = null,
        id: String? = null,
        name: String? = null
    ) {
        buffer.append("<rect ")
        writeId(id = id, name = name)
        x?.let { buffer.append("x=\"${formatValue(it)}\" ") }
        y?.let { buffer.append("y=\"${formatValue(it)}\" ") }
        buffer.append("width=\"${formatValue(width)}\" ")
        buffer.append("height=\"${formatValue(height)}\" ")
        rx?.let { buffer.append("rx=\"${formatValue(it)}\" ") }
        ry?.let { buffer.append("ry=\"${formatValue(it)}\" ") }
        writeStyle(
            fillRule = fillRule,
            fill = fill,
            fillColor = fillColor,
            stroke = stroke,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth
        )
        writeTransform(transform = transform)
        buffer.append(" />")
    }

    /**[writeSvgPath]*/
    fun writeUiPath(
        path: Path? = null,
        pathList: List<Path>? = null,
        fill: Boolean? = null,
        fillColor: Int? = null,
        stroke: Boolean? = null,
        strokeColor: Int? = null,
        strokeWidth: Float? = null,
        transform: Matrix? = null,
        id: String? = null,
        name: String? = null,
        pathStep: Float? = null,
        tolerance: Float? = null
    ) {
        var svgPath: String? = null
        var pathFillType: FillType = FillType.EVEN_ODD
        //--
        val list = if (path != null) {
            listOf(path)
        } else {
            pathList
        }
        if (!isNil(list)) {
            svgPath = list!!.toSVGStrokeContentVectorStr {
                it.enableVectorRadiansSample = LibLpHawkKeys.enableVectorRadiansSample
                it.unit = null
                it.pathStep = pathStep ?: it.pathStep
                it.pathSampleStepRadians = pathStep ?: it.pathSampleStepRadians
                it.pathTolerance = tolerance ?: it.pathTolerance
                it.decimal = digits
            }
            pathFillType = list.first().fillType
        }
        //--
        if (!isNil(svgPath)) {
            writeSvgPath(
                svgPath,
                fillRule = if (pathFillType == FillType.EVEN_ODD) "evenodd" else "nonzero",
                fill = fill,
                fillColor = fillColor,
                stroke = stroke,
                strokeColor = strokeColor,
                strokeWidth = strokeWidth,
                transform = transform,
                id = id,
                name = name
            )
        }
    }

    /**
     * 写入[path]元素
     *
     * path 元素是用来定义形状的通用元素。所有的基本形状都可以用 path 元素来创建。
     *
     * - fill-rule https://developer.mozilla.org/zh-CN/docs/Web/SVG/Attribute/fill-rule
     *
     * https://developer.mozilla.org/zh-CN/docs/Web/SVG/Element/path
     */
    fun writeSvgPath(
        svgPath: String?,
        fillRule: String = "evenodd",
        fill: Boolean? = null,
        fillColor: Int? = null,
        stroke: Boolean? = null,
        strokeColor: Int? = null,
        strokeWidth: Float? = null,
        transform: Matrix? = null,
        id: String? = null,
        name: String? = null
    ) {
        if (isNil(svgPath)) {
            return
        }
        buffer.append("<path d=\"$svgPath\" ")
        writeId(id = id, name = name)
        writeStyle(
            fillRule = fillRule,
            fill = fill,
            fillColor = fillColor,
            stroke = stroke,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth
        )
        writeTransform(transform = transform)
        buffer.append("/>")
    }

    /**
     * 写入[image]图片元素
     * https://developer.mozilla.org/zh-CN/docs/Web/SVG/Element/image
     *
     * 所有坐标系, 默认都是以[viewBox]为参考
     *
     * ## 属性
     * - x：图像水平方向上到原点的距离。
     * - y：图像竖直方向上到原点的距离。
     * - width：图像宽度。和 HTML <img> 不同，该属性是必需的。
     * - height：图像高度。和 HTML <img> 不同，该属性是必需的。
     * - href 和 xlink:href已弃用：指向图像文件的 URL。
     * - preserveAspectRatio：控制图像的缩放比例。
     * - crossorigin：定义 CORS 请求的凭据标志。
     * - decoding：向浏览器提供关于是否应该同步或异步执行图像解码的提示。
     *
     * ```
     * """
     * <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
     * <image width="$w" height="$h" xlink:href="data:image/png;base64,$encoded_img"/>
     * </svg>
     * """
     * ```
     * [scaleImage] 图片放大倍数, 1.0: 不放大; 10: 放大10倍;
     * 在使用[scaleImage]属性时, [image]必须要是[transform]后的图片, 否则具有缩放属性,宽高会对不上
     *
     * [invertScaleImageMatrix] 是否反转缩放图片的矩阵, 通常在正常情况下都是需要的, 但是雕刻图片数据时, 并不需要反向缩放
     * 默认[scaleImage]有值时, 就会反转
     * 在生成雕刻数据时, 建议不反转, 因为雕刻数据在转成GCode时, 算法会处理
     **/
    fun writeImage(
        image: Bitmap?,
        transform: Matrix? = null,
        id: String? = null,
        name: String? = null,
        scaleImage: Float? = null,
        invertScaleImageMatrix: Boolean? = null,
        x: Float? = null,
        y: Float? = null,
        width: Float? = null,
        height: Float? = null
    ) {
        if (image != null) {
            if (scaleImage != null && scaleImage != 1f) {
                val newWidth = image.width * scaleImage
                val newHeight = image.height * scaleImage
                val scaledImage = image.scale(newWidth.bitmapInt(), newHeight.bitmapInt())

                // 矩阵反向缩放
                invertScaleImageMatrix ?: let { scaleImage != 1f }
                if (invertScaleImageMatrix == true) {
                    val scaleInvertMatrix =
                        scaleMatrix(sx = 1f / scaleImage, sy = 1f / scaleImage)
                    transform?.postConcat(scaleInvertMatrix)
                }
                writeBase64Image(
                    base64Image = scaledImage.toBase64Data(),
                    transform = transform,
                    id = id,
                    name = name,
                    x = x,
                    y = y,
                    width = width,
                    height = height
                )
            } else {
                writeBase64Image(
                    base64Image = image.toBase64Data(),
                    transform = transform,
                    id = id,
                    name = name,
                    x = x,
                    y = y,
                    width = width,
                    height = height
                )
            }
        }
    }

    /**
     * [writeImage]
     * [x].[y].[width].[height] 支持mm单位, 所以需要字符串. 可以不指定.
     * [x].[y] 不指定时, 则使用[transform]中的`tx/ty`值
     * [transform] 会影响[x].[y].[width].[height]的数值
     *
     * > SVG 2 之前的规范定义了xlink:href属性，现在该属性已被href属性废弃。如果您需要支持早期的浏览器版本，
     * > 除了href属性之外，还可以使用已弃用的xlink:href属性作为后备，例如 <use href="some-id" xlink:href="some-id" x="5" y="5" /> 。
     * https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/href
     * */
    fun writeBase64Image(
        base64Image: String?,
        x: Float? = null,
        y: Float? = null,
        width: Float? = null,
        height: Float? = null,
        transform: Matrix? = null,
        id: String? = null,
        name: String? = null
    ) {
        if (!isNil(base64Image)) {
            buffer.append("<image ")
            writeId(id = id, name = name)
            //--
            if (x != null) {
                buffer.append("x=\"${formatValue(x)}\" ")
            }
            if (y != null) {
                buffer.append("y=\"${formatValue(y)}\" ")
            }
            //--
            if (width != null) {
                buffer.append("width=\"${formatValue(width)}\" ")
            }
            if (height != null) {
                buffer.append("height=\"${formatValue(height)}\" ")
            }
            writeTransform(transform = transform)
            buffer.append("href=\"$base64Image\" />")
        }
    }

    /**
     * 写入[group]元素
     *
     * https://developer.mozilla.org/zh-CN/docs/Web/SVG/Element/g
     **/
    fun writeGroup(
        action: (SvgBuilder) -> Unit,
        fillRule: String? = null,
        fill: Boolean? = null,
        fillColor: Int? = null,
        stroke: Boolean? = null,
        strokeColor: Int? = null,
        strokeWidth: Float? = null,
        id: String? = null,
        name: String? = null,
        transform: Matrix? = null
    ) {
        buffer.append("<g ")
        writeId(id = id, name = name)
        writeStyle(
            fillRule = fillRule,
            fill = fill,
            fillColor = fillColor,
            stroke = stroke,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth
        )
        writeTransform(transform = transform)
        buffer.append(">")
        val subBuilder = SvgBuilder().apply { this.digits = this@SvgBuilder.digits }
        action(subBuilder)
        buffer.append(subBuilder.build())
        buffer.append("</g>")
    }

    //endregion --元素--

    fun writeId(id: String? = null, name: String? = null) {
        if (!isNil(id)) {
            buffer.append("id=\"$id\" ")
        }
        if (!isNil(name)) {
            buffer.append("name=\"$name\" ")
        }
    }

    fun writeStyle(
        fillRule: String? = "evenodd",
        fill: Boolean? = null,
        fillColor: Int? = null,
        stroke: Boolean? = null,
        strokeColor: Int? = null,
        strokeWidth: Float? = null
    ) {
        if (fill == true) {
            buffer.append(
                "fill=\"${
                    (fillColor ?: Color.BLACK).toHexColorString(alpha = false)
                }\" "
            )
            if (fillRule != null) {
                buffer.append("fill-rule=\"$fillRule\" ")
            }
        } else if (fill == false) {
            buffer.append("fill=\"none\" ")
        }

        if (stroke == true) {
            buffer.append(
                "stroke=\"${
                    (strokeColor ?: Color.BLACK).toHexColorString(alpha = false)
                }\" "
            )
            strokeWidth?.let { buffer.append("stroke-width=\"$it\" ") }
        } else {
            buffer.append("stroke-width=\"0\" ")
        }
    }

    fun writeTransform(
        transform: Matrix? = null,
        tx: Float? = null,
        ty: Float? = null,
        sx: Float? = null,
        sy: Float? = null,
        kx: Float? = null,
        ky: Float? = null
    ) {
        if (transform == null && tx == null && ty == null && sx == null && sy == null && kx == null && ky == null) {
            return
        }

        buffer.append("transform=\"")
        val tX = tx ?: transform?.getTranslateX() ?: 0.0
        val tY = ty ?: transform?.getTranslateY() ?: 0.0
        val sX = sx ?: transform?.getScaleX() ?: 1.0
        val sY = sy ?: transform?.getScaleY() ?: 1.0
        val kX = kx ?: transform?.getSkewX() ?: 0.0
        val kY = ky ?: transform?.getSkewY() ?: 0.0
        buffer.append(
            "matrix(${formatValue(sX)} ${formatValue(kY)} ${formatValue(kX)} ${
                formatValue(sY)
            } ${formatValue(tX)} ${formatValue(tY)})"
        )
        buffer.append("\" ")
    }

    fun formatValue(value: Any?): String {
        return if (value is Number) value.toFloat()
            .decimal(digits, true, false, true) else value.toString()
    }

    fun build(): String {
        writeEnd()
        return buffer.toString()
    }
}

@DSL
fun svgBuilder(action: SvgBuilder.() -> Unit): String {
    val builder = SvgBuilder()
    action(builder)
    return builder.build()
}