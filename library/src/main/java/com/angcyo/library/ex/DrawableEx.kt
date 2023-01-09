package com.angcyo.library.ex

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.util.DisplayMetrics
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.graphics.drawable.DrawableCompat
import com.angcyo.library.L
import com.angcyo.library.app


/**
 * Created by angcyo on ：2017/12/15 15:01
 * 修改备注：
 * Version: 1.0.0
 */

/**将Drawable放在inRect的指定点centerPoint的位置*/
fun Drawable.getBoundsWith(centerPoint: Point, inRect: Rect) = Rect().apply {
    left = inRect.left + centerPoint.x - intrinsicWidth / 2
    top = inRect.top + centerPoint.y - intrinsicHeight / 2
    right = left + centerPoint.x + intrinsicWidth / 2
    bottom = top + centerPoint.y + intrinsicHeight / 2
}

fun Drawable?.color(filterColor: Int) = this?.run { filterDrawable(filterColor) }

/**
 * 颜色过滤
 */
fun Drawable?.colorFilter(@ColorInt color: Int): Drawable? {
    if (this == null) {
        return null
    }
    val wrappedDrawable = DrawableCompat.wrap(this).mutate()
    wrappedDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    return wrappedDrawable
}

/**
 * tint颜色
 */
fun Drawable?.tintDrawable(@ColorInt color: Int): Drawable? {
    if (this == null) {
        return null
    }
    if (color == undefined_color) {
        return this
    }
    val wrappedDrawable = DrawableCompat.wrap(this).mutate()
    DrawableCompat.setTint(wrappedDrawable, color)
    return wrappedDrawable
}

/**
 * 根据版本, 自动选择方法
 */
fun Drawable?.filterDrawable(@ColorInt color: Int): Drawable? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        this?.tintDrawable(color)
    } else {
        this?.colorFilter(color)
    }
}

fun Drawable?.copy(res: Resources? = null) = this?.mutate()?.constantState?.newDrawable(res)

fun Drawable?.tintDrawableColor(color: Int): Drawable? {

    if (this == null) {
        return this
    }

    val wrappedDrawable =
        DrawableCompat.wrap(this).mutate()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        DrawableCompat.setTint(wrappedDrawable, color)
    } else {
        wrappedDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    return wrappedDrawable
}

/**初始化bounds*/
fun Drawable.initBounds(width: Int = undefined_int, height: Int = undefined_int): Drawable {
    if (bounds.isNoSize()) {
        val w = if (width == undefined_int) minimumWidth else width
        val h = if (height == undefined_int) minimumHeight else height
        bounds.set(0, 0, w, h)
    }
    return this
}

fun Drawable.setBounds(width: Int = undefined_int, height: Int = undefined_int): Drawable {
    val w = if (width == undefined_int) minimumWidth else width
    val h = if (height == undefined_int) minimumHeight else height
    bounds.set(0, 0, w, h)
    return this
}

/**复制[Drawable]*/
fun Drawable.copyDrawable(): Drawable? {
    var drawable = this
    var result: Drawable? = null
    if (drawable is TransitionDrawable) {
        val transitionDrawable = drawable
        drawable = transitionDrawable.getDrawable(transitionDrawable.numberOfLayers - 1)
    }
    val bounds = drawable.bounds
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        val width = bounds.width()
        val height = bounds.height()
        if (width == 0 || height == 0) {
            return drawable
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)
        result = BitmapDrawable(app().resources, bitmap)
        result.setBounds(bounds)
    } else {
        val constantState = drawable.mutate().constantState
        if (constantState != null) {
            result = constantState.newDrawable()
            result.bounds = bounds
        }
    }
    return result
}

/**转换成[BitmapDrawable]*/
fun Drawable.toBitmapDrawable(
    outWidth: Int = -1,
    outHeight: Int = -1,
    bgColor: Int = Color.TRANSPARENT
): BitmapDrawable {
    val drawable = BitmapDrawable()
    drawable.setTargetDensity(DisplayMetrics.DENSITY_MEDIUM)
    drawable.bitmap = toBitmap(outWidth, outHeight, bgColor)
    return drawable
}

fun Drawable.getByBitmap(): Bitmap? {
    if (this is BitmapDrawable) {
        return bitmap
    }
    return toBitmap(-1, -1, Color.TRANSPARENT)
}

/**[androidx.core.graphics.drawable.DrawableKt.toBitmap]
 * [outWidth] [outHeight] 缩放到的宽高, 大于0生效
 * [bgColor] 底色*/
fun Drawable.toBitmap(
    outWidth: Int = -1,
    outHeight: Int = -1,
    bgColor: Int = Color.TRANSPARENT
): Bitmap? {

    if (outWidth <= 0 && outHeight <= 0 && bgColor == Color.TRANSPARENT && this is BitmapDrawable) {
        return bitmap
    }

    // 获取 drawable 长宽
    val width: Int = if (minimumWidth <= 0) outWidth else minimumWidth
    val height: Int = if (minimumHeight <= 0) outHeight else minimumHeight

    if (width == 0 || height == 0) {
        return null
    }

    setBounds(0, 0, width, height)

    // 获取drawable的颜色格式
    val config =
        if (opacity == PixelFormat.OPAQUE) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888

    L.v("createBitmap:$width $height")
    // 创建bitmap
    val bitmap = Bitmap.createBitmap(width, height, config)

    // 创建bitmap画布
    val canvas = Canvas(bitmap)
    if (bgColor != Color.TRANSPARENT) {
        canvas.drawColor(bgColor)
    }
    // 将drawable 内容画到画布中
    draw(canvas)

    val result = if (outWidth > 0 || outHeight > 0) {
        val width2 = if (outWidth > 0) outWidth else width
        val height2 = if (outHeight > 0) outHeight else height

        // 创建操作图片用的Matrix对象
        val matrix = Matrix()
        // 计算缩放比例
        val sx = width2 * 1f / width
        val sy = height2 * 1f / height
        // 设置缩放比例
        matrix.postScale(sx, sy)

        val bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
        bitmap2
    } else {
        bitmap
    }

    result.density = DisplayMetrics.DENSITY_MEDIUM

    return result
}

fun colorDrawable(@ColorRes resId: Int): ColorDrawable = ColorDrawable(_color(resId))

//<editor-fold desc="Picture">

/**[Picture]*/
fun withPicture(width: Int, height: Int, block: Canvas.() -> Unit): Picture {
    return Picture().apply {
        val canvas = beginRecording(width, height)
        canvas.block()
        //结束
        endRecording()
    }
}

//</editor-fold desc="Picture">
