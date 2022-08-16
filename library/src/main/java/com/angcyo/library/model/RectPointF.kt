package com.angcyo.library.model

import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import com.angcyo.library.ex._tempMatrix
import com.angcyo.library.ex._tempPath
import com.angcyo.library.ex.mapPoint
import com.angcyo.library.ex.toRectF

/**
 * 矩形4个点的坐标
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/29
 */
data class RectPointF(
    /**左上*/
    val leftTop: PointF = PointF(),
    /**左下*/
    val leftBottom: PointF = PointF(),
    /**右上*/
    val rightTop: PointF = PointF(),
    /**右下*/
    val rightBottom: PointF = PointF(),
    /**原始的矩形*/
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
    _tempMatrix.reset()
    _tempMatrix.setRotate(rotate, centerX(), centerY())

    //赋值
    val result = RectPointF(originRotate = rotate)
    result.originRectF.set(this)

    //4个点
    result.leftTop.set(left, top)
    result.leftBottom.set(left, bottom)
    result.rightTop.set(right, top)
    result.rightBottom.set(right, bottom)

    //旋转
    _tempMatrix.mapPoint(result.leftTop, result.leftTop)
    _tempMatrix.mapPoint(result.leftBottom, result.leftBottom)
    _tempMatrix.mapPoint(result.rightTop, result.rightTop)
    _tempMatrix.mapPoint(result.rightBottom, result.rightBottom)

    return result
}

/**转换成[Path]*/
fun RectPointF.toPath(result: Path = _tempPath): Path {
    _tempMatrix.reset()
    _tempMatrix.setRotate(originRotate, originRectF.centerX(), originRectF.centerY())

    result.rewind()
    result.addRect(originRectF, Path.Direction.CW)
    result.transform(_tempMatrix)

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