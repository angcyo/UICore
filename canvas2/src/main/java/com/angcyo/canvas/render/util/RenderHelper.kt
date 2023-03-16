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

    private val _boundsRect = RectF()
    private val _matrix = Matrix()

    /**计算[pathList]的包裹框*/
    fun computePathBounds(pathList: List<Path>?, bounds: RectF = _boundsRect): RectF {
        bounds.set(0f, 0f, 0f, 0f)
        pathList ?: return bounds
        pathList.computePathBounds(bounds, LibHawkKeys.enablePathBoundsExact)
        return bounds
    }

    /**将所有[pathList]平移到[0,0]的位置*/
    fun translateToOrigin(pathList: List<Path>?): List<Path>? {
        pathList ?: return null
        val bounds = computePathBounds(pathList)
        val dx = -bounds.left
        val dy = -bounds.top
        if (dx == 0f && dy == 0f) {
            //不需要平移
            return pathList
        }
        val result = mutableListOf<Path>()
        _matrix.setTranslate(dx, dy)
        for (path in pathList) {
            val newPath = Path()
            path.transform(_matrix, newPath)
            result.add(newPath)
        }
        return result
    }

}