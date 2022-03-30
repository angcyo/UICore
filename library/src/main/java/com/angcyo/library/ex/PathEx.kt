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

/**判断点是否在[Path]内*/
fun Path.contains(x: Int, y: Int): Boolean {
    val rectF = RectF()
    computeBounds(rectF, true)
    val rectRegion =
        Region(rectF.left.toInt(), rectF.top.toInt(), rectF.right.toInt(), rectF.bottom.toInt())
    val region = Region()
    region.setPath(this, rectRegion)
    return region.contains(x, y)
}

/**判断矩形是否在[Path]内*/
fun Path.contains(rectF: RectF): Boolean {
    val path = Path()
    path.reset()
    path.addRect(rectF, Path.Direction.CW)

    val result = Path()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        result.op(this, path, Path.Op.REVERSE_DIFFERENCE)
    } else {
        return false
    }

    return result.isEmpty
}