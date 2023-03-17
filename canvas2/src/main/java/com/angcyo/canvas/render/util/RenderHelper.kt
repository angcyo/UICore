package com.angcyo.canvas.render.util

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import com.angcyo.library.component.hawk.LibHawkKeys
import com.angcyo.library.ex.computePathBounds

/**
 * 一些工具助手方法
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/10
 */
object RenderHelper {

    internal val _boundsRect = RectF()
    internal val _matrix = Matrix()

    /**计算[pathList]的包裹框*/
    fun computePathBounds(pathList: List<Path>?, bounds: RectF = _boundsRect): RectF {
        bounds.set(0f, 0f, 0f, 0f)
        pathList ?: return bounds
        pathList.computePathBounds(bounds, LibHawkKeys.enablePathBoundsExact)
        return bounds
    }
}

/**[translateToOrigin]*/
fun Path?.translateToOrigin(): Path? {
    this ?: return null
    return listOf(this).translateToOrigin()?.lastOrNull()
}

/**将所有[this]平移到[0,0]的位置*/
fun List<Path>?.translateToOrigin(): List<Path>? {
    val pathList = this ?: return null
    val bounds = RenderHelper.computePathBounds(pathList)
    val dx = -bounds.left
    val dy = -bounds.top
    val result = mutableListOf<Path>()
    RenderHelper._matrix.setTranslate(dx, dy)
    for (path in pathList) {
        val newPath = Path()
        path.transform(RenderHelper._matrix, newPath)
        result.add(newPath)
    }
    return result
}

/**[scaleToSize]*/
fun Path?.scaleToSize(newWidth: Float, newHeight: Float): Path? {
    this ?: return null
    return listOf(this).scaleToSize(newWidth, newHeight)?.lastOrNull()
}

/**将所有[this]缩放到指定的宽高*/
fun List<Path>?.scaleToSize(newWidth: Float, newHeight: Float): List<Path>? {
    val pathList = this ?: return null
    val bounds = RenderHelper.computePathBounds(pathList)

    val oldWidth = bounds.width()
    val oldHeight = bounds.height()

    val sx = if (oldWidth == 0f) 1f else newWidth / oldWidth
    val sy = if (oldHeight == 0f) 1f else newHeight / oldHeight

    val result = mutableListOf<Path>()
    RenderHelper._matrix.setScale(sx, sy)
    for (path in pathList) {
        val newPath = Path()
        path.transform(RenderHelper._matrix, newPath)
        result.add(newPath)
    }
    return result
}