package com.angcyo.canvas.utils

import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.angcyo.canvas.items.PictureTextItem
import com.angcyo.library.ex.*
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */

//<editor-fold desc="临时变量">

/**临时对象, 用来存储[Matrix]矩阵值*/
val _tempValues = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)

/**临时对象, 用来存储坐标点位值*/
val _tempPoints = floatArrayOf(0f, 0f)

val _tempPoint = PointF()

/**临时对象, 用来存储矩形坐标*/
val _tempRectF = emptyRectF()

/**临时对象, 用来存储[Matrix]*/
val _tempMatrix = Matrix()

//</editor-fold desc="临时变量">

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

//<editor-fold desc="Matrix">

/**当前矩阵, 偏移的x*/
fun Matrix.getTranslateX(): Float {
    getValues(_tempValues)
    return _tempValues[Matrix.MTRANS_X]
}

fun Matrix.getTranslateY(): Float {
    getValues(_tempValues)
    return _tempValues[Matrix.MTRANS_Y]
}

fun Matrix.setTranslateValue(x: Float, y: Float) {
    getValues(_tempValues)
    _tempValues[Matrix.MTRANS_X] = x
    _tempValues[Matrix.MTRANS_Y] = y
    setValues(_tempValues)
}

fun Matrix.setScaleValue(x: Float, y: Float) {
    getValues(_tempValues)
    _tempValues[Matrix.MSCALE_X] = x
    _tempValues[Matrix.MSCALE_Y] = y
    setValues(_tempValues)
}

/**当前矩阵, 缩放的比例. 默认是1f
 * 比如1.2f 2.0f*/
fun Matrix.getScaleX(): Float {
    getValues(_tempValues)
    return _tempValues[Matrix.MSCALE_X]
}

/**默认是1f*/
fun Matrix.getScaleY(): Float {
    getValues(_tempValues)
    return _tempValues[Matrix.MSCALE_Y]
}

/**获取旋转的角度, 非弧度
 * https://stackoverflow.com/questions/12256854/get-the-rotate-value-from-matrix-in-android
 * [0~180°]
 * [-180°~0]
 *
 * */
fun Matrix.getRotateDegrees(): Float {
    getValues(_tempValues)
/*//    // translation is simple
 * [0~-180°]
 * [180°~0]
//    val tx = _tempValues[Matrix.MTRANS_X]
//    val ty = _tempValues[Matrix.MTRANS_Y]
//
//    // calculate real scale
//    val scalex: Float = _tempValues[Matrix.MSCALE_X]
//    val skewy: Float = _tempValues[Matrix.MSKEW_Y]
//    val rScale = sqrt((scalex * scalex + skewy * skewy).toDouble()).toFloat()

    // calculate the degree of rotation
    val rAngle = Math.round(
        atan2(
            _tempValues[Matrix.MSKEW_X].toDouble(),
            _tempValues[Matrix.MSCALE_X].toDouble()
        ) * (180 / Math.PI)
    ).toFloat()
    return rAngle*/

    val degrees = atan2(
        _tempValues[Matrix.MSKEW_X],
        _tempValues[Matrix.MSCALE_X]
    ) * (180 / Math.PI)

    return -degrees.roundToLong().toFloat()
}

/**[PointF]*/
fun Matrix.mapPoint(x: Float, y: Float): PointF {
    _tempPoints[0] = x
    _tempPoints[1] = y
    mapPoints(_tempPoints, _tempPoints)
    _tempPoint.x = _tempPoints[0]
    _tempPoint.y = _tempPoints[1]
    return _tempPoint
}

/**[PointF]
 * [point] 入参
 * @return 返回值*/
fun Matrix.mapPoint(point: PointF): PointF {
    return mapPoint(point.x, point.y)
}

/**
 * [point] 入参
 * [result] 返回值*/
fun Matrix.mapPoint(point: PointF, result: PointF): PointF {
    _tempPoints[0] = point.x
    _tempPoints[1] = point.y
    mapPoints(_tempPoints, _tempPoints)
    result.x = _tempPoints[0]
    result.y = _tempPoints[1]
    return result
}

/**[RectF]*/
fun Matrix.mapRectF(rect: RectF, result: RectF = _tempRectF): RectF {
    mapRect(result, rect)
    return result
}

fun Matrix.mapXValueList(xList: List<Float>): List<Float> {
    val src = FloatArray(xList.size * 2)
    val dst = FloatArray(src.size)

    xList.forEachIndexed { index, x ->
        src[index * 2] = x
        src[index * 2 + 1] = 0f
    }

    mapPoints(dst, src)
    val result = mutableListOf<Float>()
    for (i in xList.indices) {
        result.add(dst[i * 2])
    }
    return result
}

fun Matrix.mapYValueList(yList: List<Float>): List<Float> {
    val src = FloatArray(yList.size * 2)
    val dst = FloatArray(src.size)

    yList.forEachIndexed { index, y ->
        src[index * 2] = 0f
        src[index * 2 + 1] = y
    }

    mapPoints(dst, src)
    val result = mutableListOf<Float>()
    for (i in yList.indices) {
        result.add(dst[i * 2])
    }
    return result
}

//</editor-fold desc="Matrix">

//<editor-fold desc="Other">

val Int.isTextBold: Boolean
    get() = have(PictureTextItem.TEXT_STYLE_BOLD)

val Int.isUnderLine: Boolean
    get() = have(PictureTextItem.TEXT_STYLE_UNDER_LINE)

val Int.isDeleteLine: Boolean
    get() = have(PictureTextItem.TEXT_STYLE_DELETE_LINE)

val Int.isTextItalic: Boolean
    get() = have(PictureTextItem.TEXT_STYLE_ITALIC)

/**将[value]限制在[min] [max]之间*/
fun clamp(value: Float, min: Float, max: Float): Float = min(max(value, min), max)

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
fun Bitmap.engraveColorBytes(): ByteArray {
    return colorChannel { color, channelValue ->
        if (color == Color.TRANSPARENT) {
            255 //白色像素, 不雕刻
        } else {
            channelValue
        }
    }
}

//</editor-fold desc="Other">
