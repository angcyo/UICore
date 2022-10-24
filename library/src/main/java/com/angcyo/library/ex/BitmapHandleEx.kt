package com.angcyo.library.ex

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.max
import kotlin.math.min

/** 图片处理, 一些简单的算法
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/24
 */

/**将图片转灰度
 * [alphaBgColor] 透明像素时的替换颜色*/
fun Bitmap.toGrayHandle(alphaBgColor: Int = Color.TRANSPARENT): Bitmap {
    val width = width
    val height = height
    val result = Bitmap.createBitmap(width, height, config)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val color = getPixel(x, y)

            if (color == Color.TRANSPARENT) {
                //透明颜色
                result.setPixel(x, y, alphaBgColor)
            } else {
                val r = Color.red(color)
                val g = Color.green(color)
                val b = Color.blue(color)

                var value = (r + g + b) / 3
                value = max(0, min(value, 255)) //限制0~255
                result.setPixel(x, y, Color.rgb(value, value, value))
            }
        }
    }
    return result
}

/**将图片转灰度, 并且返回一张没有透明像素的图片
 * [alphaBgColor] 透明像素时的替换颜色*/
fun Bitmap.toGrayHandleAlpha(alphaBgColor: Int = Color.TRANSPARENT): Array<Bitmap> {
    val width = width
    val height = height

    val resultBitmap = Bitmap.createBitmap(width, height, config)
    val alphaBitmap = Bitmap.createBitmap(width, height, config)

    val result = arrayOf(resultBitmap, alphaBitmap)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val color = getPixel(x, y)

            if (color == Color.TRANSPARENT) {
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
 * [alphaBgColor] 透明像素时的替换颜色*/
fun Bitmap.toBlackWhiteHandle(
    threshold: Int = 128,
    invert: Boolean = false,
    alphaBgColor: Int = Color.TRANSPARENT
): Bitmap {
    val width = width
    val height = height
    val result = Bitmap.createBitmap(width, height, config)

    for (y in 0 until height) {
        for (x in 0 until width) {
            var color = getPixel(x, y)

            if (color == Color.TRANSPARENT) {
                //透明颜色
                color = alphaBgColor
            }

            if (color == Color.TRANSPARENT) {
                //依旧是透明
                result.setPixel(x, y, color)
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

                result.setPixel(x, y, Color.rgb(value, value, value))
            }
        }
    }
    return result
}

/**额外返回一张没有透明背景的图片*/
fun Bitmap.toBlackWhiteHandleAlpha(
    threshold: Int = 120,
    invert: Boolean = false,
    alphaBgColor: Int = Color.TRANSPARENT
): Array<Bitmap> {
    val width = width
    val height = height

    val resultBitmap = Bitmap.createBitmap(width, height, config)
    val alphaBitmap = Bitmap.createBitmap(width, height, config)

    val result = arrayOf(resultBitmap, alphaBitmap)

    for (y in 0 until height) {
        for (x in 0 until width) {
            var color = getPixel(x, y)

            if (color == Color.TRANSPARENT) {
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
 * 逐行扫描 清除边界空白
 *
 * @param margin 边距留多少个像素
 * @param color 需要移除的边界颜色值
 * @return 清除边界后的Bitmap
 */
fun Bitmap.trimEdgeColor(color: Int = Color.TRANSPARENT, margin: Int = 0): Bitmap {
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
        getPixels(widthPixels, 0, width, 0, y, width, 1)
        isStop = false
        for (pix in widthPixels) {
            if (pix != color) {
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
        getPixels(widthPixels, 0, width, 0, y, width, 1)
        isStop = false
        for (pix in widthPixels) {
            if (pix != color) {
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
        getPixels(widthPixels, 0, 1, x, 0, 1, height)
        isStop = false
        for (pix in widthPixels) {
            if (pix != color) {
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
        getPixels(widthPixels, 0, 1, x, 0, 1, height)
        isStop = false
        for (pix in widthPixels) {
            if (pix != color) {
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