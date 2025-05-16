package com.angcyo.library.ex

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.scaleMatrix
import com.angcyo.library.L
import com.angcyo.library.component.PictureRenderDrawable
import com.angcyo.library.component.hawk.LibHawkKeys
import com.angcyo.library.component.pool.*
import com.angcyo.library.unit.IValueUnit
import com.angcyo.library.unit.toPixelFromUnit
import com.angcyo.library.unit.toUnitFromPixel
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


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

fun Path.contains(point: PointF, clipRect: RectF? = null): Boolean {
    if (!point.x.isFinite() || !point.y.isFinite()) {
        L.w("无效的点坐标:$point")
        return false
    }
    return contains(point.x.roundToInt(), point.y.roundToInt(), clipRect)
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
fun Path.approximate2(acceptableError: Float = LibHawkKeys.pathBoundsAcceptableError): FloatArray {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //Android 8 support
        approximate(acceptableError)
    } else {
        val result = mutableListOf<Float>()
        eachPath(acceptableError) { index, ratio, contourIndex, posArray, _ ->
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
    step: Float = LibHawkKeys._pathAcceptableError,
    posArray: FloatArray = _tempPos,
    tanArray: FloatArray = _tempTan,
    block: (index: Int, ratio: Float, contourIndex: Int, posArray: FloatArray, tanArray: FloatArray) -> Unit
) {
    val pathMeasure = PathMeasure(this, false)
    var position = 0f //路径上的问题只
    var length = pathMeasure.length //路径总长度
    var index = 0 //当前段回调次数
    var contourIndex = 0 //当前第几段

    //func
    fun _each() {
        while (length > 0 && position <= length) {
            pathMeasure.getPosTan(position, posArray, tanArray)
            val ratio = position / length
            if (tanArray.size > 2) {
                //计算角度,弧度单位
                tanArray[2] = atan2(tanArray[1], tanArray[0])
            }
            block(index++, ratio, contourIndex, posArray, tanArray)

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
fun Path.computeExactBounds(
    bounds: RectF = RectF(),
    acceptableError: Float = LibHawkKeys.pathBoundsAcceptableError
): RectF {
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
fun List<Path>.computePathBounds(
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

/**直接旋转[Path]
 * [degrees] 旋转的角度, 非弧度
 * [anchorPoint] 旋转的锚点, 不指定则使用[Path]的中心点
 * */
fun Path.rotate(degrees: Float, anchorPoint: PointF? = null): Path {
    //旋转的支持
    if (degrees.isRotated()) {
        val px: Float
        val py: Float
        if (anchorPoint == null) {
            val pathBounds = acquireTempRectF()
            computePathBounds(pathBounds)
            px = pathBounds.centerX()
            py = pathBounds.centerY()
            pathBounds.release()
        } else {
            px = anchorPoint.x
            py = anchorPoint.y
        }
        val matrix = acquireTempMatrix()
        matrix.reset()
        matrix.postRotate(degrees, px, py)
        transform(matrix)
        matrix.release()
    }
    return this
}

/**水平/垂直翻转路径*/
fun Path.flip(scaleX: Float, scaleY: Float): Path {
    if (scaleX == 1f && scaleY == 1f) {
        return this
    }
    val pathBounds = acquireTempRectF()
    computePathBounds(pathBounds)
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

/**将[Path]进行转换
 * [bounds] 未旋转时的bounds, 用来实现缩放和平移
 * [rotate] 旋转角度, 配合[bounds]实现平移
 *
 * [transform]
 * */
fun Path.transform(bounds: RectF, rotate: Float): Path {
    val matrix = acquireTempMatrix()
    matrix.reset()
    val rotateBounds = acquireTempRectF()//旋转后的Bounds
    if (rotate.isRotated()) {
        matrix.setRotate(rotate, bounds.centerX(), bounds.centerY())
    }
    matrix.mapRectF(bounds, rotateBounds)

    //平移到左上角0,0, 然后缩放, 旋转
    var pathBounds = acquireTempRectF()
    pathBounds = computePathBounds(pathBounds)
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
    if (rotate.isRotated()) {
        matrix.postRotate(rotate, bounds.width() / 2, bounds.height() / 2)
    }

    val newPath = Path(this)
    newPath.transform(matrix)

    //再次偏移到目标位置中心点重合的位置
    pathBounds = computePathBounds(pathBounds)
    matrix.reset()
    matrix.setTranslate(
        rotateBounds.centerX() - pathBounds.centerX(), rotateBounds.centerY() - pathBounds.centerY()
    )
    newPath.transform(matrix)

    //release
    pathBounds.release()
    rotateBounds.release()
    matrix.release()

    return newPath
}

/**将[Path]进行缩放,旋转,并且平移到指定目标
 * [this] 未缩放旋转的原始路径数据
 * [bounds] 未旋转时的bounds, 用来实现缩放和平移
 * [rotate] 旋转角度, 配合[bounds]实现平移
 * */
fun List<Path>.transform(bounds: RectF, rotate: Float): List<Path> {
    val newPathList = mutableListOf<Path>()

    val matrix = acquireTempMatrix()
    matrix.reset()
    val rotateBounds = acquireTempRectF()//旋转后的Bounds
    if (rotate.isRotated()) {
        matrix.setRotate(rotate, bounds.centerX(), bounds.centerY())
    }
    matrix.mapRectF(bounds, rotateBounds)

    //平移到左上角0,0, 然后缩放, 旋转
    var pathBounds = acquireTempRectF()
    pathBounds = computePathBounds(pathBounds)
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
    if (rotate.isRotated()) {
        matrix.postRotate(rotate, bounds.width() / 2, bounds.height() / 2)
    }

    for (path in this) {
        val newPath = Path(path)
        newPath.transform(matrix)
        newPathList.add(newPath)
    }

    //再次偏移到目标位置中心点重合的位置
    pathBounds = newPathList.computePathBounds(pathBounds)
    matrix.reset()
    matrix.setTranslate(
        rotateBounds.centerX() - pathBounds.centerX(), rotateBounds.centerY() - pathBounds.centerY()
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

/**所有[Path]都进行变换
 * @return 返回变换后的新[Path]*/
fun List<Path>.transform(matrix: Matrix): List<Path> {
    val newPathList = mutableListOf<Path>()
    for (path in this) {
        val newPath = Path(path)
        newPath.transform(matrix)
        newPathList.add(newPath)
    }
    return newPathList
}

/**水平/垂直翻转路径*/
fun List<Path>.flip(scaleX: Float, scaleY: Float): List<Path> {
    if (scaleX == 1f && scaleY == 1f) {
        return this
    }
    val newPathList = mutableListOf<Path>()

    var pathBounds = acquireTempRectF()
    pathBounds = computePathBounds(pathBounds)

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

/**[translateToOrigin]*/
fun Path?.translateToOrigin(): Path? {
    this ?: return null
    return listOf(this).translateToOrigin()?.lastOrNull()
}

/**将所有[this]平移到相对于[0,0]的位置*/
fun List<Path>?.translateToOrigin(): List<Path>? {
    val pathList = this ?: return null
    val bounds = computePathBounds(acquireTempRectF())
    val dx = -bounds.left
    val dy = -bounds.top
    val result = mutableListOf<Path>()
    val matrix = acquireTempMatrix()
    matrix.setTranslate(dx, dy)
    for (path in pathList) {
        val newPath = Path()
        path.transform(matrix, newPath)
        result.add(newPath)
    }
    bounds.release()
    matrix.release()
    return result
}

/**
 * [scaleToSize]
 * [scaleToMm]
 * [scaleFromMm]
 * */
fun Path?.scaleToSize(newWidth: Float, newHeight: Float): Path? {
    this ?: return null
    return listOf(this).scaleToSize(newWidth, newHeight)?.lastOrNull()
}

/**将所有[this]缩放到指定的宽高*/
fun List<Path>?.scaleToSize(newWidth: Float, newHeight: Float): List<Path>? {
    val pathList = this ?: return null
    val bounds = computePathBounds()

    val oldWidth = bounds.width()
    val oldHeight = bounds.height()

    val sx = if (oldWidth == 0f) 1f else newWidth / oldWidth
    val sy = if (oldHeight == 0f) 1f else newHeight / oldHeight

    val result = mutableListOf<Path>()
    val matrix = acquireTempMatrix()
    matrix.setScale(sx, sy)
    for (path in pathList) {
        val newPath = Path()
        path.transform(matrix, newPath)
        result.add(newPath)
    }
    bounds.release()
    matrix.release()
    return result
}

/**
 * [scaleToSize]
 * [scaleToMm]
 * [scaleFromMm]
 * */
fun Path?.scaleToMm(unit: IValueUnit = IValueUnit.MM_UNIT): Path? {
    this ?: return null
    return listOf(this).scaleToMm(unit)?.lastOrNull()
}

/**将所有[this]缩放到mm单位*/
fun List<Path>?.scaleToMm(unit: IValueUnit = IValueUnit.MM_UNIT): List<Path>? {
    this ?: return null
    val scale = 1f.toUnitFromPixel(unit)
    return transform(scaleMatrix(scale, scale))
}

//--

/**
 * [scaleToSize]
 * [scaleToMm]
 * [scaleFromMm]
 * */
fun Path?.scaleFromMm(unit: IValueUnit = IValueUnit.MM_UNIT): Path? {
    this ?: return null
    return listOf(this).scaleFromMm(unit)?.lastOrNull()
}

/**将所有[this]从mm单位缩放到px单位*/
fun List<Path>?.scaleFromMm(unit: IValueUnit = IValueUnit.MM_UNIT): List<Path>? {
    this ?: return null
    val scale = 1f.toPixelFromUnit(unit)
    return transform(scaleMatrix(scale, scale))
}

fun Path?.toDrawable(
    overrideSize: Float? = null, paint: Paint = createPaint()
) = this?.toListOf().toDrawable(overrideSize, paint)

/**将路径转成可以绘制的[Drawable]
 *
 * [Drawable.toBitmap]
 *
 * [toBitmap]*/
fun List<Path>?.toDrawable(
    overrideSize: Float? = null, paint: Paint = createPaint()
): Drawable? {
    this ?: return null
    val bounds = computePathBounds(acquireTempRectF())
    val originWidth = bounds.width()
    val originHeight = bounds.height()
    val scaleMatrix = createOverrideMatrix(originWidth, originHeight, overrideSize)
    //目标输出的大小
    val scaleX = scaleMatrix.getScaleX()
    val scaleY = scaleMatrix.getScaleY()
    val width = originWidth * scaleX
    val height = originHeight * scaleY
    val drawMatrix = Matrix()
    drawMatrix.setScale(scaleX, scaleY, bounds.left, bounds.top)
    drawMatrix.postTranslate(-bounds.left, -bounds.top)
    val drawable = PictureRenderDrawable(Picture().apply {
        val canvas = beginRecording(max(1, width.ceilInt()), max(1, height.ceilInt()))
        /*canvas.translate(-bounds.left, -bounds.top)//平移到路径开始的原点
        canvas.concat(scaleMatrix)*/
        canvas.concat(drawMatrix)
        for (path in this@toDrawable) {//并没有改变原始数据, 直接绘制
            canvas.drawPath(path, paint)
        }
        //结束
        endRecording()
    })
    bounds.release()
    return drawable
}

fun Path?.toBitmap(color: Int, bgColor: Int = Color.TRANSPARENT) =
    this?.toListOf().toBitmap(null, createPaint(color), bgColor)

fun Path?.toBitmap(
    overrideSize: Float? = null,
    paint: Paint = createPaint(),
    bgColor: Int = Color.TRANSPARENT
) = this?.toListOf().toBitmap(overrideSize, paint, bgColor)

/**将路径转成[Bitmap]
 *
 * [toDrawable]*/
fun List<Path>?.toBitmap(
    overrideSize: Float? = null,
    paint: Paint = createPaint(),
    bgColor: Int = Color.TRANSPARENT
): Bitmap? {
    this ?: return null
    val bounds = computePathBounds(acquireTempRectF())
    val originWidth = bounds.width()
    val originHeight = bounds.height()
    val scaleMatrix = createOverrideMatrix(originWidth, originHeight, overrideSize)
    //目标输出的大小
    val width = originWidth * scaleMatrix.getScaleX()
    val height = originHeight * scaleMatrix.getScaleY()
    val bitmap = Bitmap.createBitmap(
        max(1, width.ceilInt()), max(1, height.ceilInt()), Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    if (bgColor != Color.TRANSPARENT) {
        canvas.drawColor(bgColor)
    }
    canvas.translate(-bounds.left, -bounds.top)//平移到路径开始的原点
    canvas.concat(scaleMatrix)
    for (path in this@toBitmap) {//并没有改变原始数据, 直接绘制
        canvas.drawPath(path, paint)
    }
    bounds.release()
    return bitmap
}

/**
 * [op] 如果为空, 则直接合并所有[Path]
 *
 * [android.graphics.Path.op]*/
fun List<Path>.op(op: Path.Op?): Path {
    val result = Path() //操作后的结果

    //op 操作
    for (path in this) {
        if (result.isEmpty) {
            result.set(path)
        } else if (op == null) {
            result.addPath(path) //直接合并
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (!result.op(path, op)) {
                    L.e("op error!")
                }
            }
        }
    }

    return result
}

/**
 * [fillType] 路径填充类型, 默认是[android.graphics.Path.FillType.WINDING]
 *
 * https://www.zhangxinxu.com/wordpress/2018/10/nonzero-evenodd-fill-mode-rule/
 * */
fun List<Path>.updateFillType(fillType: Path.FillType = Path.FillType.EVEN_ODD) {
    for (path in this) {
        path.fillType = fillType
    }
}

/**添加一个描边扇形到路径中
 * [size] 扇形的大小
 * [cx] [cy] 圆心
 * [r] 半径, 内圆半径, +size后, 是外圆半径
 * [startAngle] 扇形开始的角度
 * [sweepAngle] 扇形划过的角度*/
fun Path.addFillArc(
    size: Float,
    cx: Float,
    cy: Float,
    r: Float,
    startAngle: Float,
    sweepAngle: Float
): Path {
    rewind()
    if (sweepAngle.absoluteValue != 360f) {
        //扇形左上角坐标
        val ovalRect = acquireTempRectF()
        val lt = acquireTempPointF()
        val rt = acquireTempPointF()
        val lb = acquireTempPointF()
        val rb = acquireTempPointF()
        getPointOnCircle(cx, cy, r + size, startAngle, lt)
        getPointOnCircle(cx, cy, r + size, startAngle + sweepAngle, rt)
        getPointOnCircle(cx, cy, r, startAngle, lb)
        getPointOnCircle(cx, cy, r, startAngle + sweepAngle, rb)

        moveTo(lt.x, lt.y)
        ovalRect.set(cx - r - size, cy - r - size, cx + r + size, cy + r + size)
        arcTo(ovalRect, startAngle, sweepAngle)

        lineTo(rb.x, rb.y)
        ovalRect.set(cx - r, cy - r, cx + r, cy + r)
        arcTo(ovalRect, startAngle + sweepAngle, -sweepAngle)
        lineTo(lt.x, lt.y)

        lt.release()
        rt.release()
        lb.release()
        rb.release()
        ovalRect.release()
    } else {
        addCircle(cx, cy, r + size, Path.Direction.CW)
        addCircle(cx, cy, r, Path.Direction.CCW)
    }
    return this
}