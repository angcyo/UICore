package com.angcyo.library.ex

import android.graphics.*
import android.os.Build
import kotlin.math.atan2


/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/03/19
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**
 *
 * https://blog.csdn.net/cquwentao/article/details/51436852
 * @param progress [0,1]
 * */

/**获取[Path]指定进度[progress]时的点坐标*/
fun Path.getProgressPosition(progress: Float): FloatArray {
    val pathMeasure = PathMeasure(this, false)

    val floatArray = floatArrayOf()
    pathMeasure.getPosTan(progress, floatArray, null)

    return floatArray
}

/**获取[Path]指定进度[progress]时的点角度*/
fun Path.getProgressAngle(progress: Float): Float {
    val pathMeasure = PathMeasure(this, false)

    val floatArray = floatArrayOf()
    pathMeasure.getPosTan(progress, null, floatArray)

    //利用反正切函数得到角度
    return fixAngle((atan2(floatArray[1], floatArray[0]) * 180F / Math.PI).toFloat())
}

/**
 * 调整角度，使其在0 ~ 360之间
 *
 * @param rotation 当前角度
 * @return 调整后的角度
 */
private fun fixAngle(rotation: Float): Float {
    var result = rotation
    val angle = 360f
    if (result < 0) {
        result += angle
    }
    if (result > angle) {
        result %= angle
    }
    return result
}

/**
 * 获取[Path]指定进度[progress]之前的[Path]
 * @param progress [0,1]
 * */
fun Path.getProgressPath(progress: Float, dst: Path = Path()): Path {

    dst.reset()
    // 硬件加速的BUG
    dst.lineTo(0f, 0f)
    val pathMeasure = PathMeasure(this, false)

    //参数startWithMoveTo表示起始点是否使用moveTo方法，通常为True，保证每次截取的Path片段都是正常的、完整的。
    pathMeasure.getSegment(0f, progress * pathMeasure.length, dst, true)

    return dst
}

/**
 * 简单的缩放一个矩形
 * */
fun RectF.scale(scaleX: Float, scaleY: Float): RectF {
    val matrix = Matrix()
    matrix.setScale(scaleX, scaleY)
    matrix.mapRect(this)
    return this
}

/**缩放一个矩形
 * [scaleX] [scaleY] 宽高缩放的比例
 * [pivotX] [pivotY] 缩放的轴点
 * */
fun RectF.scale(scaleX: Float, scaleY: Float, pivotX: Float, pivotY: Float): RectF {
    val matrix = Matrix()
    matrix.setScale(scaleX, scaleY, pivotX, pivotY)
    matrix.mapRect(this)
    return this
}

/**缩放一个矩形
 * [degrees] 旋转的角度
 * [pivotX] [pivotY] 旋转的轴点
 * */
fun RectF.rotate(degrees: Float, pivotX: Float, pivotY: Float): RectF {
    val matrix = Matrix()
    matrix.setRotate(degrees, pivotX, pivotY)
    matrix.mapRect(this)
    return this
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

val _computeBounds: RectF = RectF()
val _clipRegion: Region = Region()
val _pathRegion: Region = Region()
val _tempPath: Path = Path()
val _resultPath: Path = Path()

/**判断点是否在[Path]内, path是否包含点
 * [clipRect] 需要裁剪的矩形区域, 限制点位只在这个区域内有效*/
fun Path.contains(x: Int, y: Int, clipRect: RectF? = null): Boolean {
    val _clipRect = if (clipRect == null) {
        val rectF = _computeBounds
        computeBounds(rectF, true)
        rectF
    } else {
        clipRect
    }
    _clipRegion.setEmpty()
    _clipRegion.set(
        _clipRect.left.toInt(),
        _clipRect.top.toInt(),
        _clipRect.right.toInt(),
        _clipRect.bottom.toInt()
    )
    val rectRegion = _clipRegion

    _pathRegion.setEmpty()
    _pathRegion.setPath(this, rectRegion)
    return _pathRegion.contains(x, y)
}

/**判断矩形是否在[Path]内, path是否包含矩形*/
fun Path.contains(rect: RectF): Boolean {
    _tempPath.reset()
    _tempPath.addRect(rect, Path.Direction.CW)
    return this.contains(_tempPath)
}

/**判断路径是否和[rect]相交*/
fun Path.intersect(rect: RectF): Boolean {
    _tempPath.reset()
    _tempPath.addRect(rect, Path.Direction.CW)
    return this.intersect(_tempPath)
}

/**判断矩形是否在[Path]内, path是否包含矩形*/
fun Path.contains(path: Path): Boolean {
    val result = _resultPath
    result.reset()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        result.op(this, path, Path.Op.REVERSE_DIFFERENCE)
    } else {
        return false
    }
    return result.isEmpty
}

/**判断2个Path是否相交*/
fun Path.intersect(path: Path): Boolean {
    val result = _resultPath
    result.reset()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        result.op(this, path, Path.Op.INTERSECT)
    } else {
        return false
    }
    return !result.isEmpty
}