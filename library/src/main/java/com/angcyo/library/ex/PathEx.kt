package com.angcyo.library.ex

import android.graphics.*
import android.os.Build
import androidx.core.graphics.withTranslation
import com.angcyo.library.component.hawk.LibHawkKeys
import com.angcyo.library.component.pool.*
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min


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

/**获取[Path]指定进度[progress]时的点坐标
 * [progress] [0~1] */
fun Path.getProgressPosition(
    progress: Float,
    result: FloatArray = floatArrayOf(0f, 0f)
): FloatArray {
    val pathMeasure = PathMeasure(this, false)
    pathMeasure.getPosTan(progress * pathMeasure.length, result, null)
    return result
}

/**获取[Path]指定进度[progress]时的点角度
 * [progress] [0~1]*/
fun Path.getProgressAngle(progress: Float): Float {
    val pathMeasure = PathMeasure(this, false)

    val floatArray = floatArrayOf(0f, 0f)
    pathMeasure.getPosTan(progress * pathMeasure.length, null, floatArray)

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

/**判断点是否在[Path]内, path是否包含点
 * [clipRect] 需要裁剪的矩形区域, 限制点位只在这个区域内有效*/
fun Path.contains(x: Int, y: Int, clipRect: RectF? = null): Boolean {
    val _clipRect = if (clipRect == null) {
        val rectF = acquireTempRectF()
        computePathBounds(rectF)
        rectF
    } else {
        clipRect
    }

    //限制一下矩形区域
    val clipRegion = acquireTempRegion()
    clipRegion.setEmpty()
    clipRegion.set(
        _clipRect.left.toInt(),
        _clipRect.top.toInt(),
        _clipRect.right.toInt(),
        _clipRect.bottom.toInt()
    )
    if (clipRect == null) {
        _clipRect.release()
    }

    //碰撞范围
    val pathRegion = acquireTempRegion()
    pathRegion.setEmpty()
    pathRegion.setPath(this, clipRegion)

    //点是否在范围内
    val result = pathRegion.contains(x, y)

    clipRegion.release()
    pathRegion.release()

    return result
}

/**判断矩形是否在[Path]内, path是否包含矩形
 * 如果[rect]的边框正好在path的边界上, 返回值也是true
 * */
fun Path.contains(rect: RectF): Boolean {
    val tempPath = acquireTempPath()
    tempPath.reset()
    tempPath.addRect(rect, Path.Direction.CW)
    val result = contains(tempPath)
    tempPath.release()
    return result
}

/**[rect]是否溢出了
 * 如果[rect]的边框正好在path的边界上, 则未溢出
 * */
fun Path.overflow(rect: RectF): Boolean {
    val tempPath = acquireTempPath()
    tempPath.reset()
    tempPath.addRect(rect, Path.Direction.CW)
    val result = overflow(tempPath)
    tempPath.release()
    return result
}

/**判断路径是否和[rect]相交*/
fun Path.intersect(rect: RectF): Boolean {
    val tempPath = acquireTempPath()
    tempPath.reset()
    tempPath.addRect(rect, Path.Direction.CW)
    val result = intersect(tempPath)
    tempPath.release()
    return result
}

/**判断矩形是否在[Path]内, path是否包含矩形
 * 判断[this]是否完全包含[path]
 * */
fun Path.contains(path: Path): Boolean {
    val tempPath = acquireTempPath()
    tempPath.reset()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        tempPath.op(this, path, Path.Op.REVERSE_DIFFERENCE)
    } else {
        tempPath.release()
        return false
    }
    val result = tempPath.isEmpty
    tempPath.release()
    return result
}

/**[path]溢出[this]*/
fun Path.overflow(path: Path): Boolean {
    val tempPath = acquireTempPath()
    tempPath.reset()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        tempPath.op(path, this, Path.Op.DIFFERENCE)
    } else {
        tempPath.release()
        return false
    }
    val result = tempPath.isEmpty
    tempPath.release()
    return !result
}

/**判断2个Path是否相交*/
fun Path.intersect(path: Path): Boolean {
    val tempPath = acquireTempPath()
    tempPath.reset()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        tempPath.op(this, path, Path.Op.INTERSECT)
    } else {
        tempPath.release()
        return false
    }
    val result = !tempPath.isEmpty
    tempPath.release()
    return result
}

/**路径的长度*/
fun Path.length(): Float {
    return PathMeasure(this, false).length
}

/**用一系列线段近似 Path 。这将返回包含点组件的数组的 float[]。每个点按顺序有三个组成部分。
 * [android.graphics.Path.approximate]*/
fun Path.approximate2(acceptableError: Float = 0.5f): FloatArray {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //Android 8 support
        approximate(acceptableError)
    } else {
        val result = mutableListOf<Float>()
        eachPath(acceptableError) { index, ratio, contourIndex, posArray ->
            result.add(ratio)
            result.add(posArray[0])
            result.add(posArray[1])
        }
        result.toFloatArray()
    }
}

/**枚举路径上所有的点
 * [step] 枚举的步长
 *
 * [contourIndex] 第几段路径
 * [index] 当前段回调次数
 * [ratio] 当前段路径比例
 * [posArray] 路径坐标
 * */
fun Path.eachPath(
    step: Float = 1f,
    posArray: FloatArray = _tempPoints,
    block: (index: Int, ratio: Float, contourIndex: Int, posArray: FloatArray) -> Unit
) {
    val pathMeasure = PathMeasure(this, false)
    var position = 0f
    var length = pathMeasure.length
    var index = 0
    var contourIndex = 0

    //func
    fun _each() {
        while (position <= length) {
            pathMeasure.getPosTan(position, posArray, null)
            val ratio = position / length
            block(index++, ratio, contourIndex, posArray)

            if (position == length) {
                break
            }
            position += step
            if (position > length) {
                position = length
            }
        }
    }

    //first
    _each()

    //下一个轮廓, 如果有
    while (pathMeasure.nextContour()) {
        contourIndex++
        index = 0
        position = 0f
        length = pathMeasure.length
        _each()
    }
}

/**枚举路径上指定长度的一段一段路径
 * [len] 多长为一段
 * */
fun Path.eachSegment(len: Float, block: (index: Int, ratio: Float, path: Path) -> Unit) {
    val pathMeasure = PathMeasure(this, false)
    var startPosition = 0f
    var endPosition = len
    var length = pathMeasure.length
    var index = 0

    //func
    fun _each() {
        if (endPosition >= length) {
            //直接结束
            val path = Path()
            pathMeasure.getSegment(startPosition, length, path, true)
            block(index, 1f, path)
        } else {
            while (endPosition <= length) {
                val path = Path()
                pathMeasure.getSegment(startPosition, endPosition, path, true)
                val ratio = endPosition / length
                block(index++, ratio, path)

                if (endPosition == length) {
                    break
                }
                startPosition = endPosition
                endPosition += len
                if (endPosition > length) {
                    endPosition = length
                }
            }
        }
    }

    //first
    _each()

    //下一个轮廓, 如果有
    while (pathMeasure.nextContour()) {
        index = 0
        startPosition = 0f
        endPosition = len
        length = pathMeasure.length
        _each()
    }
}

/**计算[Path]的bounds
 * [exact] 是否需要确切的bounds, true:此方法使用读取path中的所有点坐标进行bounds计算
 * */
fun Path.computePathBounds(
    bounds: RectF = RectF(),
    exact: Boolean = LibHawkKeys.enablePathBoundsExact
): RectF {
    if (exact) {
        computeExactBounds(bounds)
    } else {
        computeBounds(bounds, true)
    }
    return bounds
}

/** [acceptableError] 误差级别*/
fun Path.computeExactBounds(bounds: RectF = RectF(), acceptableError: Float = 0.5f): RectF {
    val pos: FloatArray = approximate2(acceptableError)

    var i = 0
    while (i < pos.size) {
        if (i == 0) bounds[pos[i + 1], pos[i + 2], pos[i + 1]] =
            pos[i + 2] else bounds.union(pos[i + 1], pos[i + 2])
        i += 3
    }

    return bounds
}

/**计算一组[Path]的bounds*/
fun List<Path>.computeBounds(
    bounds: RectF = RectF(),
    exact: Boolean = LibHawkKeys.enablePathBoundsExact
): RectF {
    if (isEmpty()) {
        return bounds
    }
    var left: Float? = null
    var top: Float? = null
    var right: Float? = null
    var bottom: Float? = null
    val pathRect = acquireTempRectF()
    for (path in this) {
        path.computePathBounds(pathRect, exact)
        left = min(left ?: pathRect.left, pathRect.left)
        top = min(top ?: pathRect.top, pathRect.top)
        right = max(right ?: pathRect.right, pathRect.right)
        bottom = max(bottom ?: pathRect.bottom, pathRect.bottom)
    }
    bounds.set(left ?: 0f, top ?: 0f, right ?: 0f, bottom ?: 0f)
    pathRect.release()
    return bounds
}

/**[Path]转[Bitmap]*/
fun Path.toBitmap(
    paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }
): Bitmap? {
    val pathRect = RectF()
    computePathBounds(pathRect)
    val width = max(1, pathRect.width().toInt())
    val height = max(1, pathRect.height().toInt())
    return bitmapCanvas(width, height) {
        withTranslation(pathRect.left, pathRect.top) {
            drawPath(this@toBitmap, paint)
        }
    }
}

/**List<Path>转[Bitmap]*/
fun List<Path>.toBitmap(
    paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }
): Bitmap? {
    val pathRect = RectF()
    computeBounds(pathRect, true)
    return bitmapCanvas(pathRect.width().toInt(), pathRect.height().toInt()) {
        withTranslation(pathRect.left, pathRect.top) {
            forEach {
                drawPath(it, paint)
            }
        }
    }
}

/**
 * 添加一个二阶贝塞尔曲线, 1个控制点
 * [c1x] [c1y] 控制点
 * [endX] [endY] 结束点
 * */
fun Path.bezier(c1x: Float, c1y: Float, endX: Float, endY: Float): Path {
    quadTo(c1x, c1y, endX, endY)
    return this
}

fun Path.bezier(cPoint: PointF, endPoint: PointF): Path {
    return bezier(cPoint.x, cPoint.y, endPoint.x, endPoint.y)
}

/**
 * 添加一个三阶贝塞尔曲线, 2个控制点
 * [c1x] [c1y] 控制点1
 * [c2x] [c2y] 控制点2
 * [endX] [endY] 结束点
 * */
fun Path.bezier(c1x: Float, c1y: Float, c2x: Float, c2y: Float, endX: Float, endY: Float): Path {
    cubicTo(c1x, c1y, c2x, c2y, endX, endY)
    return this
}

fun Path.bezier(c1Point: PointF, c2Point: PointF, endPoint: PointF): Path {
    return bezier(c1Point.x, c1Point.y, c2Point.x, c2Point.y, endPoint.x, endPoint.y)
}

/**调整path的宽高*/
fun Path.adjustWidthHeight(newWidth: Float, newHeight: Float, result: Path = Path()): Path {
    //path实际的宽高
    val pathBounds = acquireTempRectF()
    computePathBounds(pathBounds)
    val pathWidth = pathBounds.width()
    val pathHeight = pathBounds.height()

    //
    val matrix = acquireTempMatrix()
    val scaleX = newWidth / pathWidth
    val scaleY = newHeight / pathHeight
    matrix.setScale(scaleX, scaleY, pathBounds.left, pathBounds.top)

    //
    transform(matrix, result)

    //release
    pathBounds.release()
    matrix.release()

    return result
}

/**将[Path]进行转换
 * [rotateBounds] 需要平移到的left,top
 * [rotate] path需要旋转的角度
 * */
fun Path.transform(rotateBounds: RectF, rotate: Float): Path {
    val pathBounds = acquireTempRectF()
    computeBounds(pathBounds, true)
    val targetPath = Path(this)

    //旋转的支持
    if (rotate != 0f) {
        val matrix = acquireTempMatrix()
        matrix.reset()
        matrix.postRotate(rotate, rotateBounds.centerX(), rotateBounds.centerY())
        matrix.mapRect(pathBounds, pathBounds)
        targetPath.transform(matrix)
        matrix.release()
    }

    pathBounds.release()
    return targetPath
}


/**水平/垂直翻转路径*/
fun Path.flip(scaleX: Float, scaleY: Float): Path {
    if (scaleX == 1f && scaleY == 1f) {
        return this
    }
    val pathBounds = acquireTempRectF()
    computeBounds(pathBounds, true)
    val targetPath = Path(this)

    val matrix = acquireTempMatrix()
    matrix.reset()
    //平移到左上角0,0, 然后按照中心点缩放/翻转
    matrix.setTranslate(-pathBounds.left, -pathBounds.top)
    matrix.postScale(scaleX, scaleY, pathBounds.width() / 2, pathBounds.height() / 2)
    targetPath.transform(matrix)

    matrix.release()
    pathBounds.release()
    return targetPath
}

/**将[Path]进行缩放,旋转,并且平移到指定目标
 * [pathList] 未缩放旋转的原始路径数据
 * [bounds] 未旋转时的bounds, 用来实现缩放和平移
 * [rotate] 旋转角度, 配合[bounds]实现平移
 * */
fun List<Path>.transform(bounds: RectF, rotate: Float): List<Path> {
    val newPathList = mutableListOf<Path>()

    val matrix = acquireTempMatrix()
    matrix.reset()
    val rotateBounds = acquireTempRectF()//旋转后的Bounds
    if (rotate != 0f) {
        matrix.setRotate(rotate, bounds.centerX(), bounds.centerY())
    }
    matrix.mapRectF(bounds, rotateBounds)

    //平移到左上角0,0, 然后缩放, 旋转
    var pathBounds = acquireTempRectF()
    pathBounds = computeBounds(pathBounds)
    matrix.reset()
    matrix.setTranslate(-pathBounds.left, -pathBounds.top)

    //缩放
    val pathWidth = pathBounds.width()
    val pathHeight = pathBounds.height()
    val scaleX = if (pathWidth == 0f) {
        1f
    } else {
        bounds.width() / pathWidth
    }
    val scaleY = if (pathHeight == 0f) {
        1f
    } else {
        bounds.height() / pathHeight
    }
    matrix.postScale(scaleX, scaleY, 0f, 0f)

    //旋转
    if (rotate != 0f) {
        matrix.postRotate(rotate, bounds.width() / 2, bounds.height() / 2)
    }

    for (path in this) {
        val newPath = Path(path)
        newPath.transform(matrix)
        newPathList.add(newPath)
    }

    //再次偏移到目标位置中心点重合的位置
    pathBounds = newPathList.computeBounds(pathBounds)
    matrix.reset()
    matrix.setTranslate(
        rotateBounds.centerX() - pathBounds.centerX(),
        rotateBounds.centerY() - pathBounds.centerY()
    )
    for (path in newPathList) {
        path.transform(matrix)
    }

    //release
    pathBounds.release()
    rotateBounds.release()
    matrix.release()

    return newPathList
}

/**水平/垂直翻转路径*/
fun List<Path>.flip(scaleX: Float, scaleY: Float): List<Path> {
    if (scaleX == 1f && scaleY == 1f) {
        return this
    }
    val newPathList = mutableListOf<Path>()

    var pathBounds = acquireTempRectF()
    pathBounds = computeBounds(pathBounds)

    val matrix = acquireTempMatrix()
    matrix.reset()
    //平移到左上角0,0, 然后按照中心点缩放/翻转
    matrix.setTranslate(-pathBounds.left, -pathBounds.top)
    matrix.postScale(scaleX, scaleY, pathBounds.width() / 2, pathBounds.height() / 2)

    for (path in this) {
        val newPath = Path(path)
        newPath.transform(matrix)
        newPathList.add(newPath)
    }

    //release
    matrix.release()
    pathBounds.release()

    return newPathList
}