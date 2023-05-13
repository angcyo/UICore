package com.angcyo.library.ex

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Picture
import com.angcyo.library.annotation.Pixel
import kotlin.math.max
import kotlin.math.min

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/04/11
 */

private val _overrideMatrix = Matrix()

/**创建一个输出等比指定大小的矩阵
 * [overrideWidth] 输出的宽度
 * [overrideHeight] 输出的高度
 * 宽高同时指定时, 则任意比例缩放. 只指定1个时, 等比缩放
 * */
fun createOverrideMatrix(
    originWidth: Float,
    originHeight: Float,
    overrideWidth: Float?,
    overrideHeight: Float? = null,
    result: Matrix = _overrideMatrix
): Matrix {
    var sx = 1f
    var sy = 1f
    //覆盖大小需要进行的缩放

    if (overrideWidth != null && overrideHeight != null) {
        //任意比例
        if (originWidth > 0) {
            sx = overrideWidth / originWidth
        }
        if (originHeight > 0) {
            sy = overrideHeight / originHeight
        }
    } else if (overrideWidth != null || overrideHeight != null) {
        //等比
        val overrideSize = overrideWidth ?: overrideHeight
        if (overrideSize != null) {
            if (originWidth > 0) {
                sx = overrideSize / originWidth
            }
            if (originHeight > 0) {
                sy = overrideSize / originHeight
            }

            if (originWidth > 0 && originHeight > 0) {
                sx = min(sx, sy)
                sy = sx//等比
            }
        }
    } else {
        //no op
    }
    result.setScale(sx, sy)
    return result
}

/**创建一个输出指定大小的[Canvas] [Picture]
 * [createOverrideMatrix]
 * */
fun createOverridePictureCanvas(
    @Pixel
    originWidth: Float,
    @Pixel
    originHeight: Float,
    @Pixel
    overrideWidth: Float?, /*要缩放到的宽度*/
    @Pixel
    overrideHeight: Float? = null,
    @Pixel
    minWidth: Float = 1f, /*最小宽度*/
    @Pixel
    minHeight: Float = 1f,
    block: Canvas.() -> Unit
): Picture {
    val scaleMatrix = createOverrideMatrix(originWidth, originHeight, overrideWidth, overrideHeight)
    //目标输出的大小
    val width = originWidth * scaleMatrix.getScaleX()
    val height = originHeight * scaleMatrix.getScaleY()

    //最小宽高
    val w = max(width, minWidth).toInt()
    val h = max(height, minHeight).toInt()

    return withPicture(w, h) {
        concat(scaleMatrix)
        block()
    }
}

/**创建一个输出指定大小的[Canvas] [Bitmap]
 * [createOverrideMatrix]
 * */
fun createOverrideBitmapCanvas(
    @Pixel
    originWidth: Float,
    @Pixel
    originHeight: Float,
    @Pixel
    overrideWidth: Float?, /*要缩放到的宽度*/
    @Pixel
    overrideHeight: Float? = null,
    @Pixel
    minWidth: Float = 1f, /*最小宽度*/
    @Pixel
    minHeight: Float = 1f,
    block: Canvas.() -> Unit
): Bitmap {
    val scaleMatrix = createOverrideMatrix(originWidth, originHeight, overrideWidth, overrideHeight)
    //目标输出的大小
    val width = originWidth * scaleMatrix.getScaleX()
    val height = originHeight * scaleMatrix.getScaleY()

    //最小宽高
    val w = max(width, minWidth).toInt()
    val h = max(height, minHeight).toInt()

    return withBitmap(w, h) {
        concat(scaleMatrix)
        block()
    }
}

/**创建一个输出指定大小的[Canvas] [Bitmap]
 * [createOverrideMatrix]
 */
fun createOverrideBitmapCanvas(
    originWidth: Float,
    originHeight: Float,
    overrideWidth: Float?,
    overrideHeight: Float? = null,
    init: Matrix.() -> Unit = {},
    block: Canvas.() -> Unit
): Bitmap? {
    val matrix = createOverrideMatrix(originWidth, originHeight, overrideWidth, overrideHeight)
    //目标输出的大小
    val width = originWidth * matrix.getScaleX()
    val height = originHeight * matrix.getScaleY()

    val bitmapWidth = width.ceilInt()
    val bitmapHeight = height.ceilInt()
    if (bitmapWidth <= 0 || bitmapHeight <= 0) {
        return null
    }

    return withBitmap(bitmapWidth, bitmapHeight) {
        matrix.init()
        concat(matrix)
        block()
    }
}

/**创建一个[Picture]对象*/
fun withPicture(width: Int, height: Int, block: Canvas.() -> Unit): Picture {
    return Picture().apply {
        val canvas = beginRecording(width, height)
        canvas.block()
        //结束
        endRecording()
    }
}

/**创建一个[Bitmap]对象*/
fun withBitmap(
    width: Int,
    height: Int,
    config: Bitmap.Config = Bitmap.Config.ARGB_8888,
    block: Canvas.() -> Unit
): Bitmap {
    return Bitmap.createBitmap(width, height, config).apply {
        val canvas = Canvas(this)
        canvas.block()
    }
}

/**[withBitmap]*/
fun withBitmapPaint(
    bitmap: Bitmap,
    config: Bitmap.Config = Bitmap.Config.ARGB_8888,
    block: Canvas.(Paint) -> Unit
) = withBitmap(bitmap.width, bitmap.height, config) {
    val paint = createPaint()
    block(this, paint)
}

/**将文本的中点绘制在指定的位置*/
fun Canvas.drawTextCenter(text: String, cx: Float, cy: Float, paint: Paint) {
    val textWidth = paint.measureText(text)
    val textHeight = paint.textHeight()

    val x = cx - textWidth / 2
    val y = cy + textHeight / 2 - paint.descent()

    drawText(text, x, y, paint)
}