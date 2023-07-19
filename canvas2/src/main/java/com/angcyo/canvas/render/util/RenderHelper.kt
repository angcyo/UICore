package com.angcyo.canvas.render.util

import android.graphics.Path
import android.graphics.RectF
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.hawk.LibHawkKeys
import com.angcyo.library.ex.computePathBounds
import com.angcyo.library.ex.translateToOrigin

/**
 * 一些工具助手方法
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/10
 */
object RenderHelper {

    internal val _boundsRect = RectF()

    /**计算[pathList]的包裹框*/
    fun computePathBounds(pathList: List<Path>?, bounds: RectF = _boundsRect): RectF {
        bounds.set(0f, 0f, 0f, 0f)
        pathList ?: return bounds
        pathList.computePathBounds(bounds, LibHawkKeys.enablePathBoundsExact)
        return bounds
    }

    /**将原始[pathList]数据, 转换成目标位置(旋转/缩放/倾斜)的数据*/
    @Pixel
    fun translateToRender(
        pathList: List<Path>?,
        renderProperty: CanvasRenderProperty?
    ): List<Path>? {
        val list = pathList ?: return null
        renderProperty ?: return pathList
        val newPathList = list.translateToOrigin() ?: return null
        val renderMatrix = renderProperty.getRenderMatrix()
        for (path in newPathList) {
            path.transform(renderMatrix)
        }
        return newPathList
    }
}