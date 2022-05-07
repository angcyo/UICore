package com.angcyo.library.ex

import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/22
 */

//<editor-fold desc="rect sie">

/**按照矩形中点调整*/
const val ADJUST_TYPE_CENTER = 0

/**按照矩形左上调整*/
const val ADJUST_TYPE_LT = 0x011

/**按照矩形右上调整*/
const val ADJUST_TYPE_RT = 0x21

/**按照矩形右下调整*/
const val ADJUST_TYPE_RB = 0x22

/**按照矩形左下调整*/
const val ADJUST_TYPE_LB = 0x12

val _tempRectF = RectF()
val _tempMatrix = Matrix()

/**矩形没有大小*/
fun RectF.isNoSize() = width() == 0f || height() == 0f

fun Rect.isNoSize() = width() == 0 || height() == 0

/**调整矩形大小*/
fun RectF.adjustSize(width: Float, height: Float, adjustType: Int = ADJUST_TYPE_CENTER) {
    val w = width()
    val h = height()

    val ws = w - width
    val hs = h - height

    when (adjustType) {
        ADJUST_TYPE_CENTER -> {
            inset(ws / 2, hs / 2)
        }
        ADJUST_TYPE_LT -> {
            right -= ws
            bottom -= hs

            /*if (isFlipHorizontal) {
                left -= ws
            } else {
                right -= ws
            }
            if (isFlipVertical) {
                top -= hs
            } else {
                bottom -= hs
            }*/
        }
        ADJUST_TYPE_LB -> {
            right -= ws
            top += hs
        }
        ADJUST_TYPE_RT -> {
            left += ws
            bottom -= hs
        }
        ADJUST_TYPE_RB -> {
            left += ws
            top += hs
        }
    }
}

/**调整矩形大小, 并且保证旋转点一致*/
fun RectF.adjustSizeWithRotate(
    width: Float,
    height: Float,
    rotate: Float,
    adjustType: Int = ADJUST_TYPE_CENTER
) {
    //如果有旋转角度, 要确保旋转前后的锚点坐标一直, 体验才好
    //原始矩形旋转的中心点坐标
    val originCenterX = centerX()
    val originCenterY = centerY()
    //左上角固定, 调整矩形宽高
    adjustSize(width, height, adjustType)

    //按照原始的旋转中点坐标, 旋转调整后的矩形
    val rotateRect = _tempRectF
    val matrix = _tempMatrix
    _tempMatrix.reset()
    matrix.postRotate(rotate, originCenterX, originCenterY)
    matrix.mapRect(rotateRect, this)

    //旋转后的矩形中点就是调整后的矩形需要偏移的x,y
    offset(rotateRect.centerX() - centerX(), rotateRect.centerY() - centerY())
}

//</editor-fold desc="rect sie">

//<editor-fold desc="rect flip">

/**矩形是否翻转了*/
val RectF.isFlipHorizontal: Boolean
    get() = left > right

val RectF.isFlipVertical: Boolean
    get() = top > bottom

/**翻转后, 对应的左上右下的坐标*/
val RectF.flipLeft: Float
    get() = if (isFlipHorizontal) right else left

val RectF.flipRight: Float
    get() = if (isFlipHorizontal) left else right

val RectF.flipTop: Float
    get() = if (isFlipHorizontal) bottom else top

val RectF.flipBottom: Float
    get() = if (isFlipHorizontal) top else bottom

/**翻转后的矩形坐标修正*/
fun RectF.adjustFlipRect(result: RectF): RectF {
    val l = min(left, right)
    val t = min(top, bottom)

    val r = max(left, right)
    val b = max(top, bottom)

    result.set(l, t, r, b)
    return result
}

//</editor-fold desc="rect flip">