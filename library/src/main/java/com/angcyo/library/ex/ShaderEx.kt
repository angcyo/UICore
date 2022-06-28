package com.angcyo.library.ex

import android.graphics.*
import android.graphics.Shader.TileMode

/**
 * 着色器
 * [android.graphics.Shader]
 *
 * https://www.jianshu.com/p/1efcc9c9f286
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/22
 */

/**
 * [BitmapShader]
 * 平铺模式有三种：
 * Shader.TileMode.CLAMP：如果着色器超出原始边界范围，会复制边缘颜色。
 * Shader.TileMode.MIRROR：横向和纵向的重复着色器的图像，交替镜像图像是相邻的图像总是接合。这个官方的说明可能不太好理解，说白了，就是图像不停翻转来平铺，直到平铺完毕。
 * Shader.TileMode.REPEAT： 横向和纵向的重复着色器的图像。
 * */
fun bitmapShader(
    bitmap: Bitmap,
    tileX: TileMode = TileMode.REPEAT,
    tileY: TileMode = TileMode.REPEAT
): BitmapShader {
    return BitmapShader(bitmap, tileX, tileY)
}

/**[LinearGradient]
 * [x0] 表示渐变的起始点x坐标
 * [y0] 表示渐变的起始点y坐标
 * [x1] 表示渐变的终点x坐标
 * [y1] 表示渐变的终点y坐标
 * [colors] 表示渐变的颜色数组
 * [positions] 用来指定颜色数组的相对位置,可以为null，为null是表示颜色均匀分布
 * [tile] 表示平铺模式
 * */
fun linearGradientShader(
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    colors: IntArray,
    positions: FloatArray? = null,
    tile: TileMode = TileMode.REPEAT
): LinearGradient {
    return LinearGradient(x0, y0, x1, y1, colors, positions, tile)
}

fun linearGradientShader(
    rect: RectF,
    colors: IntArray,
    positions: FloatArray? = null,
    tile: TileMode = TileMode.REPEAT
): LinearGradient {
    return linearGradientShader(
        rect.left,
        rect.top,
        rect.right,
        rect.bottom,
        colors,
        positions,
        tile
    )
}

/**径向渐变着色器
 * 从中心点向外扩散的渐变
 *
 * [centerX]：渐变中心x坐标
 * [centerY]：渐变中心y坐标
 * [radius]:渐变圆的半径
 * [colors]：分布在渐变圆中心到边缘的颜色
 * [stops]：取值0-1之间，用来指定颜色数组的相对位置,可以为null，为null是表示颜色均匀分布
 * [tile] 表示平铺模式
 *
 * */
fun radialGradientShader(
    centerX: Float,
    centerY: Float,
    radius: Float,
    centerColor: Long,
    edgeColor: Long,
    tileMode: TileMode = TileMode.REPEAT
): RadialGradient {
    return RadialGradient(centerX, centerY, radius, centerColor, edgeColor, tileMode)
}

/**
 * [SweepGradient]
 * 绕着一个中心点进行扫描的渐变着色器
 * */
fun sweepGradientShader(
    cx: Float,
    cy: Float,
    colors: LongArray,
    positions: FloatArray? = null
): SweepGradient {
    return SweepGradient(cx, cy, colors, positions)
}

fun sweepGradientShader(cx: Float, cy: Float, startColor: Int, endColor: Int): SweepGradient {
    return SweepGradient(cx, cy, startColor, endColor)
}