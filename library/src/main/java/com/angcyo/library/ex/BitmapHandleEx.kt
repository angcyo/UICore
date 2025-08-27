package com.angcyo.library.ex

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.core.graphics.createBitmap
import com.angcyo.library.component.hawk.LibHawkKeys
import kotlin.math.max
import kotlin.math.min

/** 图片处理, 一些简单的算法
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/24
 */

/**彩色颜色对应的灰度值, [0~255]
 * @return 返回的是灰度值*/
fun Int.toGrayInt(): Int {
    val color = this
    val r = Color.red(color)
    val g = Color.green(color)
    val b = Color.blue(color)

    var value = (r + g + b) / 3
    value = max(0, min(value, 255)) //限制0~255
    return value
}

/**彩色转成灰度颜色
 * @return 返回的是灰度颜色值*/
@ColorInt
fun Int.toGrayColorInt(): Int {
    val value = toGrayInt()
    val alpha = Color.alpha(this)
    return Color.argb(alpha, value, value, value)
}

/**将按比例缩小的版本加载到内存中
 * [android.graphics.BitmapFactory.Options.inSampleSize]
 * https://developer.android.com/topic/performance/graphics/load-bitmap?hl=zh-cn#load-bitmap*/
fun calculateInSampleSize(bitmapWidth: Int, bitmapHeight: Int, reqWidth: Int, reqHeight: Int): Int {
    // Raw height and width of image
    var inSampleSize = 1

    if (bitmapHeight > reqHeight || bitmapWidth > reqWidth) {

        val halfHeight: Int = bitmapHeight / 2
        val halfWidth: Int = bitmapWidth / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

/**[toGrayHandle], 支持更多参数
 * [invert] 是否反色
 * [contrast] 对比度 [-1~1]
 * [brightness] 亮度 [-1~1]
 * */
@Deprecated("请使用jni算法[BitmapHandle]库")
fun Bitmap.toGrayHandle(
    invert: Boolean = false,
    contrast: Float = 0f,
    brightness: Float = 0f,
    alphaBgColor: Int = Color.TRANSPARENT,
    alphaThreshold: Int = LibHawkKeys.alphaThreshold
): Bitmap {
    val width = width
    val height = height
    val result = createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val color = getPixel(x, y)
            val alpha = Color.alpha(color)

            if (alpha < alphaThreshold) {
                //透明颜色
                result.setPixel(x, y, alphaBgColor)
            } else {
                //灰度值
                var grayValue = color.toGrayInt()
                grayValue = ((contrast + 1) * grayValue + brightness * 255).toInt()
                grayValue = clamp(grayValue, 0, 255)
                if (invert) {
                    grayValue = 255 - grayValue
                }
                //灰度颜色
                val targetColor = Color.argb(alpha, grayValue, grayValue, grayValue)
                result.setPixel(x, y, targetColor)
            }
        }
    }
    return result
}

/**将图片转灰度, 并且返回一张没有透明像素的图片
 * [alphaBgColor] 透明像素时的替换颜色*/
fun Bitmap.toGrayHandleAlpha(
    alphaBgColor: Int = Color.TRANSPARENT,
    alphaThreshold: Int = LibHawkKeys.alphaThreshold
): Array<Bitmap> {
    val width = width
    val height = height

    val resultBitmap = createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)
    val alphaBitmap = createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)

    val result = arrayOf(resultBitmap, alphaBitmap)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val color = getPixel(x, y)
            val alpha = Color.alpha(color)

            if (alpha < alphaThreshold) {
                //透明颜色
                resultBitmap.setPixel(x, y, alphaBgColor)
                alphaBitmap.setPixel(x, y, Color.WHITE)//默认白色背景
            } else {
                val r = Color.red(color)
                val g = Color.green(color)
                val b = Color.blue(color)

                var value = (r + g + b) / 3
                value = max(0, min(value, 255)) //限制0~255

                resultBitmap.setPixel(x, y, Color.rgb(value, value, value))
                alphaBitmap.setPixel(x, y, Color.rgb(r, g, b))
            }
        }
    }
    return result
}

/**将图片转黑白
 * [threshold] 阈值, [0~255] [黑色~白色] 大于这个值的都是白色
 * [invert] 反色, 是否要将黑白颜色颠倒
 * [alphaBgColor] 透明像素时的替换颜色
 * [thresholdChannelColor] 颜色阈值判断的通道支持[Color.RED] [Color.GREEN] [Color.BLUE] [Color.GRAY]
 * [alphaThreshold] 透明颜色的阈值, 当颜色的透明值小于此值时, 视为透明
 * [alphaBgColor] 如果是透明颜色, 则使用此值替代
 * */
@Deprecated("请使用jni算法[BitmapHandle]库")
fun Bitmap.toBlackWhiteHandle(
    threshold: Int = LibHawkKeys.grayThreshold,
    invert: Boolean = false,
    thresholdChannelColor: Int = Color.GRAY,
    alphaBgColor: Int = Color.TRANSPARENT,
    alphaThreshold: Int = LibHawkKeys.alphaThreshold,
): Bitmap {
    val width = width
    val height = height
    val result = createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)

    for (y in 0 until height) {
        for (x in 0 until width) {
            var color = getPixel(x, y)
            val alpha = Color.alpha(color)
            if (alpha < alphaThreshold) {
                //透明颜色
                color = alphaBgColor
            }

            if (color == Color.TRANSPARENT) {
                //依旧是透明
                result.setPixel(x, y, color)
            } else {
                var value = when (thresholdChannelColor) {
                    Color.RED -> Color.red(color)
                    Color.GREEN -> Color.green(color)
                    Color.BLUE -> Color.blue(color)
                    else -> color.toGrayInt()
                }
                value = max(0, min(value, 255)) //限制0~255

                value = if (value >= threshold) {
                    //白色
                    if (invert) {
                        0x00
                    } else {
                        0xff
                    }
                } else {
                    //黑色
                    if (invert) {
                        0xff
                    } else {
                        0x00
                    }
                }

                result.setPixel(x, y, Color.argb(alpha, value, value, value))
            }
        }
    }
    return result
}

/**额外返回一张没有透明背景的图片*/
fun Bitmap.toBlackWhiteHandleAlpha(
    threshold: Int = 120,
    invert: Boolean = false,
    alphaBgColor: Int = Color.TRANSPARENT,
    alphaThreshold: Int = LibHawkKeys.alphaThreshold,
): Array<Bitmap> {
    val width = width
    val height = height

    val resultBitmap = createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)
    val alphaBitmap = createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)

    val result = arrayOf(resultBitmap, alphaBitmap)

    for (y in 0 until height) {
        for (x in 0 until width) {
            var color = getPixel(x, y)

            val alpha = Color.alpha(color)
            if (alpha < alphaThreshold) {
                //透明颜色
                color = alphaBgColor
            }

            if (color == Color.TRANSPARENT) {
                //依旧是透明
                resultBitmap.setPixel(x, y, color)
                alphaBitmap.setPixel(x, y, Color.WHITE)//默认白色背景
            } else {
                val r = Color.red(color)
                val g = Color.green(color)
                val b = Color.blue(color)

                var value = (r + g + b) / 3
                value = max(0, min(value, 255)) //限制0~255

                value = if (value >= threshold) {
                    //白色
                    if (invert) {
                        0x00
                    } else {
                        0xff
                    }
                } else {
                    //黑色
                    if (invert) {
                        0xff
                    } else {
                        0x00
                    }
                }

                resultBitmap.setPixel(x, y, Color.rgb(value, value, value))
                alphaBitmap.setPixel(x, y, Color.rgb(r, g, b))
            }
        }
    }
    return result
}

/**色彩通道转换[Bitmap]可视化对象*/
fun ByteArray.toChannelBitmap(width: Int, height: Int, channelType: Int = Color.RED): Bitmap {
    val channelBitmap =
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(channelBitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = if (channelType == Color.TRANSPARENT) Color.BLACK else channelType
        this.style = Paint.Style.FILL
        strokeWidth = 1f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    val bytes = this
    for (y in 0 until height) {
        for (x in 0 until width) {
            val value: Int = bytes[y * width + x].toHexInt()
            paint.color = Color.argb(
                if (channelType == Color.TRANSPARENT) value else 255,
                if (channelType == Color.RED) value else 0,
                if (channelType == Color.GREEN) value else 0,
                if (channelType == Color.BLUE) value else 0
            )
            canvas.drawCircle(x.toFloat(), y.toFloat(), 1f, paint)//绘制圆点
        }
    }
    return channelBitmap
}

/**
 * 逐行扫描 清除边界空白, 裁边之前建议黑白画处理
 *
 * @param bwThreshold //黑白画处理, 去掉杂质的干扰黑色, 负数关闭黑白画处理
 * @param margin 边距留多少个像素
 * @param colors 需要移除的边界颜色值集合
 * @return 清除边界后的Bitmap
 */
@Deprecated("请使用jni算法[BitmapHandle]库")
fun Bitmap.trimEdgeColor(
    bwThreshold: Int = 200,
    colors: List<Int> = listOf(Color.TRANSPARENT, Color.WHITE),
    margin: Int = 0
): Bitmap {
    val scanPixelBitmap = if (bwThreshold >= 0) {
        toBlackWhiteHandle(bwThreshold)
    } else {
        this
    }

    var blank = margin

    val height = height
    val width = width
    var widthPixels = IntArray(width)
    var isStop: Boolean

    var top = 0
    var left = 0
    var right = 0
    var bottom = 0

    //top
    for (y in 0 until height) {
        scanPixelBitmap.getPixels(widthPixels, 0, width, 0, y, width, 1)
        isStop = false
        for (pix in widthPixels) {
            if (!colors.contains(pix)) {
                top = y
                isStop = true
                break
            }
        }
        if (isStop) {
            break
        }
    }

    //bottom
    for (y in height - 1 downTo 0) {
        scanPixelBitmap.getPixels(widthPixels, 0, width, 0, y, width, 1)
        isStop = false
        for (pix in widthPixels) {
            if (!colors.contains(pix)) {
                bottom = y
                isStop = true
                break
            }
        }
        if (isStop) {
            break
        }
    }

    //left
    widthPixels = IntArray(height)
    for (x in 0 until width) {
        scanPixelBitmap.getPixels(widthPixels, 0, 1, x, 0, 1, height)
        isStop = false
        for (pix in widthPixels) {
            if (!colors.contains(pix)) {
                left = x
                isStop = true
                break
            }
        }
        if (isStop) {
            break
        }
    }
    //right
    for (x in width - 1 downTo 1) {
        scanPixelBitmap.getPixels(widthPixels, 0, 1, x, 0, 1, height)
        isStop = false
        for (pix in widthPixels) {
            if (!colors.contains(pix)) {
                right = x
                isStop = true
                break
            }
        }
        if (isStop) {
            break
        }
    }
    if (blank < 0) {
        blank = 0
    }
    //
    left = if (left - blank > 0) left - blank else 0
    top = if (top - blank > 0) top - blank else 0
    right = if (right + blank > width - 1) width - 1 else right + blank
    bottom = if (bottom + blank > height - 1) height - 1 else bottom + blank
    //
    return Bitmap.createBitmap(this, left, top, right - left, bottom - top)
}

//---

/**枚举图片上的每个像素点, x, y 宽高的坐标
 *
 * [orientation] 枚举的方向
 *     [LinearLayout.HORIZONTAL] 横向枚举, 从上往下, 从左到右
 *     [LinearLayout.VERTICAL] 纵向枚举, 从左往右, 从上到下
 * */
fun Bitmap.eachPixel(
    orientation: Int = LinearLayout.HORIZONTAL,
    action: (x: Int, y: Int, color: Int) -> Unit = { _, _, _ -> }
) {
    val width = width
    val height = height

    if (orientation == LinearLayout.VERTICAL) {
        for (x in 0 until width) {
            for (y in 0 until height) {
                val color = getPixel(x, y)
                action(x, y, color)
            }
        }
    } else {
        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = getPixel(x, y)
                action(x, y, color)
            }
        }
    }
}


/**枚举通道颜色
 * [channelType] 通道类型 [Color.RED] [Color.GREEN] [Color.BLUE]*/
fun Bitmap.eachColorChannel(
    channelType: Int = Color.RED,
    action: (wIndex: Int, hIndex: Int, color: Int) -> Unit = { _, _, _ -> }
) {
    eachPixel { x, y, color ->
        val channelColor = when (channelType) {
            Color.RED -> Color.red(color)
            Color.GREEN -> Color.green(color)
            Color.BLUE -> Color.blue(color)
            Color.TRANSPARENT -> Color.alpha(color)
            else -> 0xFF
        }
        action(x, y, channelColor)
    }
}

/**给图片添加一个背景颜色*/
fun Bitmap.addBgColor(bgColor: Int): Bitmap {
    val bitmap = createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(bgColor)
    canvas.drawBitmap(this, null, Rect(0, 0, width, height), Paint(Paint.ANTI_ALIAS_FLAG))
    return bitmap
}