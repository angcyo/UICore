package com.angcyo.canvas.utils

import android.graphics.*
import android.text.Layout
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.items.data.DataTextItem.Companion.TEXT_STYLE_BOLD
import com.angcyo.canvas.items.data.DataTextItem.Companion.TEXT_STYLE_DELETE_LINE
import com.angcyo.canvas.items.data.DataTextItem.Companion.TEXT_STYLE_ITALIC
import com.angcyo.canvas.items.data.DataTextItem.Companion.TEXT_STYLE_UNDER_LINE
import com.angcyo.canvas.items.renderer.BaseItemRenderer
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

/**排序规则从上到下, 从左到右
 * 比较它的两个参数的顺序。
 * 如果两个参数相等，则返回零;
 * 如果第一个参数小于第二个参数，则返回负数;
 * 如果第一个参数大于第二个参数，则返回正数;
 * 从小到大的自然排序
 * */
fun List<BaseItemRenderer<*>>.engraveSort(): List<BaseItemRenderer<*>> {
    //return sortedBy { it.getRotateBounds().top }
    return sortedWith { left, right ->
        val leftBounds = left.getRotateBounds()
        val rightBounds = right.getRotateBounds()
        if (leftBounds.top == rightBounds.top) {
            leftBounds.left.compareTo(rightBounds.left)
        } else {
            leftBounds.top.compareTo(rightBounds.top)
        }
    }
}

//</editor-fold desc="canvas">

//<editor-fold desc="Other">

val Int.isTextBold: Boolean
    get() = have(TEXT_STYLE_BOLD)

val Int.isUnderLine: Boolean
    get() = have(TEXT_STYLE_UNDER_LINE)

val Int.isDeleteLine: Boolean
    get() = have(TEXT_STYLE_DELETE_LINE)

val Int.isTextItalic: Boolean
    get() = have(TEXT_STYLE_ITALIC)

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

fun Double.canvasDecimal(digit: Int = 2, fadedUp: Boolean = true): String {
    return decimal(digit, fadedUp)
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
