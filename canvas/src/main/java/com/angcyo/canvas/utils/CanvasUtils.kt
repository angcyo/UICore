package com.angcyo.canvas.utils

import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.items.PictureTextItem
import com.angcyo.gcode.GCodeDrawable
import com.angcyo.gcode.GCodeHelper
import com.angcyo.library.app
import com.angcyo.library.ex.*
import com.angcyo.svg.Svg
import com.angcyo.svg.Svg.loadSvgPathDrawable
import com.pixplicity.sharp.SharpDrawable
import kotlin.math.max

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */

//<editor-fold desc="canvas">

/**创建一个画笔*/
fun createPaint(color: Int = Color.GRAY, style: Paint.Style = Paint.Style.STROKE) =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        this.style = style
        strokeWidth = 1f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

fun createTextPaint(color: Int = Color.BLACK, textSize: Float = 12 * dp) =
    TextPaint(createPaint(color, Paint.Style.FILL)).apply {
        this.textSize = textSize
        this.textAlign = Paint.Align.LEFT
    }

/**[StaticLayout]*/
fun createStaticLayout(
    source: CharSequence,
    paint: TextPaint,
    width: Int,
    align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL // Layout.Alignment.ALIGN_OPPOSITE
): StaticLayout {
    val layout: StaticLayout
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        layout = StaticLayout.Builder.obtain(
            source,
            0,
            source.length,
            paint,
            width
        ).setAlignment(align).build()
    } else {
        layout = StaticLayout(
            source,
            0,
            source.length,
            paint,
            width,
            align,
            1f,
            0f,
            false
        )
    }
    return layout
}

//</editor-fold desc="canvas">

//<editor-fold desc="Other">

val Int.isTextBold: Boolean
    get() = have(PictureTextItem.TEXT_STYLE_BOLD)

val Int.isUnderLine: Boolean
    get() = have(PictureTextItem.TEXT_STYLE_UNDER_LINE)

val Int.isDeleteLine: Boolean
    get() = have(PictureTextItem.TEXT_STYLE_DELETE_LINE)

val Int.isTextItalic: Boolean
    get() = have(PictureTextItem.TEXT_STYLE_ITALIC)

fun Layout.getMaxLineWidth(): Float {
    var width = 0f
    for (line in 0 until lineCount) {
        width = max(width, getLineWidth(line))
    }
    return width
}

/**等比限制最大的宽高
 * [com.angcyo.library.ex.RectExKt.adjustScaleSize]*/
fun limitMaxWidthHeight(
    width: Float,
    height: Float,
    maxWidth: Float,
    maxHeight: Float,
    result: FloatArray = _tempValues
): FloatArray {
    result[0] = width
    result[1] = height
    if (width > maxWidth || height > maxHeight) {
        //超出范围, 等比缩放

        val scaleX = maxWidth / width
        val scaleY = maxHeight / height

        if (scaleX > scaleY) {
            //按照高度缩放
            result[1] = maxHeight
            result[0] *= scaleY
        } else {
            result[0] = maxWidth
            result[1] *= scaleX
        }
    }
    return result
}

/**保留小数点后几位*/
fun Float.canvasDecimal(digit: Int = 2, fadedUp: Boolean = true): String {
    return this.toDouble().decimal(digit, fadedUp)
}

/**机器雕刻的色彩数据可视化*/
fun ByteArray.toEngraveBitmap(width: Int, height: Int): Bitmap {
    val channelBitmap =
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(channelBitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = Paint.Style.FILL
        strokeWidth = 1f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    val bytes = this
    for (y in 0 until height) {
        for (x in 0 until width) {
            val value: Int = bytes[y * width + x].toHexInt()
            paint.color = Color.argb(255, value, value, value)
            canvas.drawCircle(x.toFloat(), y.toFloat(), 1f, paint)//绘制圆点
        }
    }
    return channelBitmap
}

/**从图片中, 获取雕刻需要用到的像素信息*/
fun Bitmap.engraveColorBytes(channelType: Int = Color.RED): ByteArray {
    return colorChannel(channelType) { color, channelValue ->
        if (color == Color.TRANSPARENT) {
            0xFF //255 白色像素, 白色在纸上不雕刻, 在金属上雕刻
        } else {
            channelValue
        }
    }
}

/**
 * 是否进入雕刻模式, 此时只有画布能响应手势
 * */
fun CanvasDelegate.engraveMode(enable: Boolean = true) {
    disableTouchFlag(CanvasDelegate.TOUCH_FLAG_MULTI_SELECT, enable)
    controlHandler.enable = !enable
    controlRenderer.drawControlPoint = !enable
    refresh()
}

/**扩展*/
fun GCodeHelper.parseGCode(gCodeText: String?): GCodeDrawable? =
    parseGCode(gCodeText, createPaint(Color.BLACK))

/**扩展*/
fun parseSvg(svgText: String?): SharpDrawable? = if (svgText.isNullOrEmpty()) {
    null
} else {
    loadSvgPathDrawable(svgText, -1, null, createPaint(Color.BLACK), 0, 0)
}

//</editor-fold desc="Other">

//<editor-fold desc="Svg">

/**加载[assets]中的Svg[SharpDrawable]*/
fun loadAssetsSvg(assetsName: String): Pair<String?, SharpDrawable?>? {
    val svg = app().readAssets(assetsName)
    return try {
        svg to Svg.loadSvgDrawable(svg!!)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**只读取[SVG]中的[Path]数据
 * [com.angcyo.svg.StylePath]
 * [com.pixplicity.sharp.SharpDrawable.pathList]*/
fun loadAssetsSvgPath(
    assetsName: String,
    color: Int = Color.BLACK, // Color.BLACK 黑色边
    drawStyle: Paint.Style? = null, //Paint.Style.STROKE //描边
    viewWidth: Int = 0,
    viewHeight: Int = 0,
): Pair<String?, SharpDrawable?>? {
    val svg = app().readAssets(assetsName)
    return try {
        svg to Svg.loadSvgPathDrawable(svg!!, color, drawStyle, null, viewWidth, viewHeight)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun loadAssetsSvgPath(
    assetsName: String,
    paint: Paint, //createPaint(Color.BLACK, Paint.Style.STROKE),
    viewWidth: Int = 0,
    viewHeight: Int = 0,
): Pair<String?, SharpDrawable?>? {
    val svg = app().readAssets(assetsName)
    return try {
        svg to Svg.loadSvgPathDrawable(
            svg!!,
            paint.color,
            paint.style,
            paint,
            viewWidth,
            viewHeight
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**从[svg]字符串中加载SVG [SharpDrawable]*/
fun loadTextSvgPath(
    svg: String,
    color: Int = Color.BLACK,
    viewWidth: Int = 0,
    viewHeight: Int = 0,
): SharpDrawable? {
    return try {
        Svg.loadSvgPathDrawable(svg, color, null, null, viewWidth, viewHeight)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**从[svg]字符串中加载SVG [SharpDrawable]*/
fun loadTextSvgPath(
    svg: String,
    paint: Paint,// = createPaint(Color.BLACK, Paint.Style.STROKE)
    viewWidth: Int = 0,
    viewHeight: Int = 0,
): SharpDrawable? {
    return try {
        Svg.loadSvgPathDrawable(svg, paint.color, paint.style, paint, viewWidth, viewHeight)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

//</editor-fold desc="Svg">
