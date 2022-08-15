package com.angcyo.library.ex

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/22
 */

//<editor-fold desc="base">

val _tempRect = Rect()
val _tempRectF = RectF()
val _tempMatrix = Matrix()

fun emptyRectF() = RectF(0f, 0f, 0f, 0f)

fun Rect.toRectF() = RectF(this)

fun RectF.isChanged(other: RectF): Boolean {
    return left != other.left || top != other.top || right != other.right || bottom != other.bottom
}

fun RectF.isSizeChanged(other: RectF): Boolean {
    return width() != other.width() || height() != other.height()
}

/**矩形没有大小*/
fun RectF.isNoSize() = width() == 0f || height() == 0f

fun Rect.isNoSize() = width() == 0 || height() == 0

/**[this]矩形是否在[rect]矩形的外面*/
fun RectF.isOutOf(rect: RectF): Boolean {
    if (left < rect.left && right < rect.left) {
        return true
    }
    if (top < rect.top && bottom < rect.top) {
        return true
    }
    if (right > rect.right && left > rect.right) {
        return true
    }
    if (bottom > rect.bottom && top > rect.bottom) {
        return true
    }
    return false
}

//</editor-fold desc="base">

//<editor-fold desc="rect size">

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

/**缩放一个矩形
 * [scaleX] [scaleY] 宽高缩放的比例
 * [pivotX] [pivotY] 缩放的轴点
 * */
fun RectF.scale(scaleX: Float, scaleY: Float, pivotX: Float = left, pivotY: Float = top): RectF {
    val matrix = Matrix()
    matrix.setScale(scaleX, scaleY, pivotX, pivotY)
    matrix.mapRect(this)
    return this
}

/**将矩形平移*/
fun RectF.translate(dx: Float = 0f, dy: Float = 0f): RectF {
    offset(dx, dy)
    return this
}

/**缩放一个矩形
 * [degrees] 旋转的角度
 * [pivotX] [pivotY] 旋转的轴点
 * */
fun RectF.rotate(
    degrees: Float,
    pivotX: Float = centerX(),
    pivotY: Float = centerY(),
    result: RectF = this
): RectF {
    val matrix = Matrix()
    matrix.setRotate(degrees, pivotX, pivotY)
    matrix.mapRect(result, this)
    return result
}

/**将矩形信息, 旋转到[Path]*/
fun RectF.rotateToPath(
    degrees: Float,
    pivotX: Float = centerX(),
    pivotY: Float = centerY(),
    result: Path = Path()
): Path {
    val matrix = _tempMatrix
    matrix.reset()
    matrix.setRotate(degrees, pivotX, pivotY)
    result.rewind()
    result.addRect(this, Path.Direction.CW)
    result.transform(matrix)
    return result
}

/**
 * 在矩形中间点, 缩放
 * */
fun RectF.scaleFromCenter(scaleX: Float, scaleY: Float): RectF {
    val matrix = Matrix()
    matrix.setScale(scaleX, scaleY)
    val tempRectF = RectF()
    matrix.mapRect(tempRectF, this)
    matrix.postTranslate(
        this.width() / 2 - tempRectF.width() / 2,
        this.height() / 2 - tempRectF.height() / 2
    )
    matrix.mapRect(this)
    return this
}

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
    matrix.reset()
    matrix.postRotate(rotate, originCenterX, originCenterY) //旋转矩形
    matrix.mapRect(rotateRect, this)

    if (width() < 0) {
        rotateRect.set(rotateRect.right, rotateRect.top, rotateRect.left, rotateRect.bottom)
    }
    if (height() < 0) {
        rotateRect.set(rotateRect.left, rotateRect.bottom, rotateRect.right, rotateRect.top)
    }

    //旋转后的矩形中点就是调整后的矩形需要偏移的dx,dy
    val dx = rotateRect.centerX() - centerX()
    val dy = rotateRect.centerY() - centerY()

    offset(dx, dy)
}

/**[adjustScaleSize]*/
fun RectF.limitMaxWidthHeight(
    maxWidth: Float,
    maxHeight: Float,
    adjustType: Int = ADJUST_TYPE_CENTER
): RectF = adjustScaleSize(maxWidth, maxHeight, adjustType)

/**限制矩形的最小宽高*/
fun RectF.limitMinWidthHeight(
    minWidth: Float,
    minHeight: Float,
    adjustType: Int = ADJUST_TYPE_CENTER
): RectF {
    val width = width()
    val height = height()

    if (width < minWidth || height < minHeight) {
        //小于范围, 等比缩放

        val scaleX = minWidth / width
        val scaleY = minHeight / height

        val newWidth: Float
        val newHeight: Float
        if (scaleX > scaleY) {
            //按照高度缩放
            newHeight = minHeight
            newWidth = newHeight * (width / height)
        } else {
            newWidth = minWidth
            newHeight = newWidth * (height / width)
        }

        adjustSize(newWidth, newHeight, adjustType)
    }
    return this
}

/**将一个矩形等比缩放到指定限制的宽高
 * [com.angcyo.canvas.utils.CanvasUtilsKt.limitMaxWidthHeight]
 * [android.graphics.Matrix.setRectToRect]*/
fun RectF.adjustScaleSize(
    maxWidth: Float,
    maxHeight: Float,
    adjustType: Int = ADJUST_TYPE_CENTER
): RectF {
    val width = width()
    val height = height()

    if (width > maxWidth || height > maxHeight) {
        //超出范围, 等比缩放

        val scaleX = maxWidth / width
        val scaleY = maxHeight / height

        val newWidth: Float
        val newHeight: Float
        if (scaleX > scaleY) {
            //按照高度缩放
            newHeight = maxHeight
            newWidth = newHeight * (width / height)
        } else {
            newWidth = maxWidth
            newHeight = newWidth * (height / width)
        }

        adjustSize(newWidth, newHeight, adjustType)
    }
    return this
}

/**将当前的矩形临时设置成[rect], [block]执行完成之后恢复*/
public inline fun <T> RectF.withSave(rect: RectF, block: () -> T): T? {
    return withSave(rect.left, rect.top, rect.right, rect.bottom, block)
}

public inline fun <T> RectF.withSave(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    block: () -> T
): T? {
    val old = RectF(this)
    set(left, top, right, bottom)
    return try {
        block()
    } finally {
        set(old)
    }
}

//</editor-fold desc="rect size">

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