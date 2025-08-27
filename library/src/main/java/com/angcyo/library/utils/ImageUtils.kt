package com.angcyo.library.utils

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import com.angcyo.library.app

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/21
 */


/**
 * 快速模糊
 *
 * 先缩小原图，对小图进行模糊，再放大回原先尺寸
 *
 * @param src    源图片
 * @param scale  缩放比例(0...1)
 * @param radius 模糊半径
 * @return 模糊后的图片
 */
fun fastBlur(src: Bitmap, scale: Float, radius: Float): Bitmap? {
    return fastBlur(src, scale, radius, false)
}

/**
 * 快速模糊图片
 *
 * 先缩小原图，对小图进行模糊，再放大回原先尺寸
 *
 * @param src     源图片
 * @param scale   缩放比例(0...1)
 * @param radius  模糊半径
 * @param recycle 是否回收
 * @return 模糊后的图片
 */
fun fastBlur(
    src: Bitmap,
    scale: Float,
    radius: Float,
    recycle: Boolean
): Bitmap? {
    if (isEmptyBitmap(src)) return null
    val width = src.width
    val height = src.height
    val scaleWidth = (width * scale + 0.5f).toInt()
    val scaleHeight = (height * scale + 0.5f).toInt()
    if (scaleWidth == 0 || scaleHeight == 0) return null
    var scaleBitmap: Bitmap? = Bitmap.createScaledBitmap(src, scaleWidth, scaleHeight, true)
    val paint =
        Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    val canvas = Canvas()
    val filter = PorterDuffColorFilter(
        Color.TRANSPARENT, PorterDuff.Mode.SRC_ATOP
    )
    paint.colorFilter = filter
    canvas.scale(scale, scale)
    canvas.drawBitmap(scaleBitmap!!, 0f, 0f, paint)
    scaleBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        renderScriptBlur(
            app(),
            scaleBitmap,
            radius
        )
    } else {
        stackBlur(
            scaleBitmap,
            radius.toInt(),
            recycle
        )
    }
    if (scale == 1f || scaleBitmap == null) return scaleBitmap
    val ret = Bitmap.createScaledBitmap(scaleBitmap, width, height, true)
    if (!scaleBitmap.isRecycled) scaleBitmap.recycle()
    if (recycle && !src.isRecycled) src.recycle()
    return ret
}

fun isEmptyBitmap(src: Bitmap?): Boolean {
    return src == null || src.width == 0 || src.height == 0
}

/**
 * renderScript模糊图片
 * <p>API大于17</p>
 *
 * @param context 上下文
 * @param src     源图片
 * @param radius  模糊度(1...25)
 * @return 模糊后的图片
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
fun renderScriptBlur(
    context: Context?,
    src: Bitmap?,
    radius: Float
): Bitmap? {
    var radius = radius
    if (isEmptyBitmap(src)) return null
    var rs: RenderScript? = null
    try {
        rs = RenderScript.create(context)
        rs.messageHandler = RenderScript.RSMessageHandler()
        val input = Allocation.createFromBitmap(
            rs,
            src,
            Allocation.MipmapControl.MIPMAP_NONE,
            Allocation.USAGE_SCRIPT
        )
        val output = Allocation.createTyped(rs, input.type)
        val blurScript =
            ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        if (radius > 25) {
            radius = 25.0f
        } else if (radius <= 0) {
            radius = 1.0f
        }
        blurScript.setInput(input)
        blurScript.setRadius(radius)
        blurScript.forEach(output)
        output.copyTo(src)
    } finally {
        rs?.destroy()
    }
    return src
}

/**
 * stack模糊图片
 *
 * @param src     源图片
 * @param radius  模糊半径
 * @param recycle 是否回收
 * @return stack模糊后的图片
 */
fun stackBlur(src: Bitmap, radius: Int, recycle: Boolean): Bitmap? {
    val ret: Bitmap
    ret = if (recycle) {
        src
    } else {
        src.copy(src.config ?: Bitmap.Config.ARGB_8888, true)
    }
    if (radius < 1) {
        return null
    }
    val w = ret.width
    val h = ret.height
    val pix = IntArray(w * h)
    ret.getPixels(pix, 0, w, 0, 0, w, h)
    val wm = w - 1
    val hm = h - 1
    val wh = w * h
    val div = radius + radius + 1
    val r = IntArray(wh)
    val g = IntArray(wh)
    val b = IntArray(wh)
    var rsum: Int
    var gsum: Int
    var bsum: Int
    var x: Int
    var y: Int
    var i: Int
    var p: Int
    var yp: Int
    var yi: Int
    var yw: Int
    val vmin = IntArray(Math.max(w, h))
    var divsum = div + 1 shr 1
    divsum *= divsum
    val dv = IntArray(256 * divsum)
    i = 0
    while (i < 256 * divsum) {
        dv[i] = i / divsum
        i++
    }
    yi = 0
    yw = yi
    val stack = Array(div) { IntArray(3) }
    var stackpointer: Int
    var stackstart: Int
    var sir: IntArray
    var rbs: Int
    val r1 = radius + 1
    var routsum: Int
    var goutsum: Int
    var boutsum: Int
    var rinsum: Int
    var ginsum: Int
    var binsum: Int
    y = 0
    while (y < h) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        boutsum = rsum
        goutsum = boutsum
        routsum = goutsum
        binsum = routsum
        ginsum = binsum
        rinsum = ginsum
        i = -radius
        while (i <= radius) {
            p = pix[yi + Math.min(wm, Math.max(i, 0))]
            sir = stack[i + radius]
            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff
            rbs = r1 - Math.abs(i)
            rsum += sir[0] * rbs
            gsum += sir[1] * rbs
            bsum += sir[2] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
            i++
        }
        stackpointer = radius
        x = 0
        while (x < w) {
            r[yi] = dv[rsum]
            g[yi] = dv[gsum]
            b[yi] = dv[bsum]
            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum
            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]
            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]
            if (y == 0) {
                vmin[x] = Math.min(x + radius + 1, wm)
            }
            p = pix[yw + vmin[x]]
            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff
            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]
            rsum += rinsum
            gsum += ginsum
            bsum += binsum
            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer % div]
            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]
            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]
            yi++
            x++
        }
        yw += w
        y++
    }
    x = 0
    while (x < w) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        boutsum = rsum
        goutsum = boutsum
        routsum = goutsum
        binsum = routsum
        ginsum = binsum
        rinsum = ginsum
        yp = -radius * w
        i = -radius
        while (i <= radius) {
            yi = Math.max(0, yp) + x
            sir = stack[i + radius]
            sir[0] = r[yi]
            sir[1] = g[yi]
            sir[2] = b[yi]
            rbs = r1 - Math.abs(i)
            rsum += r[yi] * rbs
            gsum += g[yi] * rbs
            bsum += b[yi] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
            if (i < hm) {
                yp += w
            }
            i++
        }
        yi = x
        stackpointer = radius
        y = 0
        while (y < h) {
            // Preserve alpha channel: ( 0xff000000 & pix[yi] )
            pix[yi] =
                -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum
            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]
            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]
            if (x == 0) {
                vmin[y] = Math.min(y + r1, hm) * w
            }
            p = x + vmin[y]
            sir[0] = r[p]
            sir[1] = g[p]
            sir[2] = b[p]
            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]
            rsum += rinsum
            gsum += ginsum
            bsum += binsum
            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer]
            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]
            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]
            yi += w
            y++
        }
        x++
    }
    ret.setPixels(pix, 0, w, 0, 0, w, h)
    return ret
}