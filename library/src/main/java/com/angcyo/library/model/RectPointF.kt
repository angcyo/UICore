package com.angcyo.library.model

import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import com.angcyo.library.component.pool.acquireTempMatrix
import com.angcyo.library.component.pool.acquireTempPath
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.mapPoint
import com.angcyo.library.ex.toRectF

/**
 * 矩形4个点的坐标
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/29
 */
data class RectPointF(
    /**旋转后的左上*/
    val leftTop: PointF = PointF(),
    /**旋转后的左下*/
    val leftBottom: PointF = PointF(),
    /**旋转后的右上*/
    val rightTop: PointF = PointF(),
    /**旋转后的右下*/
    val rightBottom: PointF = PointF(),
    /**原始的矩形, 未旋转*/
    val originRectF: RectF = RectF(),
    /**旋转的角度*/
    val originRotate: Float = 0f,
)

/**[Rect]*/
fun Rect.toFourPoint(rotate: Float = 0f): RectPointF = toRectF().toFourPoint(rotate)

/**转换成矩形的4个点坐标*/
fun RectF.toRectPoint(rotate: Float = 0f): RectPointF = toFourPoint(rotate)

/**
 * 将矩形转换成4个点的坐标
 * [rotate] 矩形需要旋转的角度
 * */
fun RectF.toFourPoint(rotate: Float = 0f): RectPointF {
    val matrix = acquireTempMatrix()
    matrix.reset()
    matrix.setRotate(rotate, centerX(), centerY())

    //赋值
    val result = RectPointF(originRotate = rotate)
    result.originRectF.set(this)

    //4个点
    result.leftTop.set(left, top)
    result.leftBottom.set(left, bottom)
    result.rightTop.set(right, top)
    result.rightBottom.set(right, bottom)

    //旋转
    matrix.mapPoint(result.leftTop, result.leftTop)
    matrix.mapPoint(result.leftBottom, result.leftBottom)
    matrix.mapPoint(result.rightTop, result.rightTop)
    matrix.mapPoint(result.rightBottom, result.rightBottom)

    matrix.release()
    return result
}

/**转换成[Path]*/
fun RectPointF.toPath(result: Path = acquireTempPath()): Path {
    val matrix = acquireTempMatrix()
    matrix.reset()
    matrix.setRotate(originRotate, originRectF.centerX(), originRectF.centerY())

    result.rewind()
    result.addRect(originRectF, Path.Direction.CW)
    result.transform(matrix)

    matrix.release()
    return result
}

fun RectPointF.toRect(result: RectF): RectF {
    val left = minOf(leftTop.x, rightTop.x, rightBottom.x, leftBottom.x)
    val top = minOf(leftTop.y, rightTop.y, rightBottom.y, leftBottom.y)
    val right = maxOf(leftTop.x, rightTop.x, rightBottom.x, leftBottom.x)
    val bottom = maxOf(leftTop.y, rightTop.y, rightBottom.y, leftBottom.y)
    result.set(left, top, right, bottom)
    return result
}

/**返回矩形4个角的点坐标*/
fun RectPointF.toCornersValues(
    result: FloatArray = floatArrayOf(
        0f,
        0f,
        0f,
        0f,
        0f,
        0f,
        0f,
        0f
    )
): FloatArray {
    result[0] = leftTop.x
    result[1] = leftTop.y

    result[2] = rightTop.x
    result[3] = rightTop.y

    result[4] = rightBottom.x
    result[5] = rightBottom.y

    result[6] = leftBottom.x
    result[7] = leftBottom.y

    return result
}