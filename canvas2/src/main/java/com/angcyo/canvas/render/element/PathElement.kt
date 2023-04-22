package com.angcyo.canvas.render.element

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.state.IStateStack
import com.angcyo.canvas.render.state.PathStateStack
import com.angcyo.canvas.render.util.RenderHelper
import kotlin.math.max

/**
 * 路径元素
 *
 * [com.angcyo.canvas.render.util.RenderHelper]
 * [com.angcyo.canvas.render.util.RenderHelperKt.translateToOrigin(java.util.List<? extends android.graphics.Path>)]
 * [com.angcyo.canvas.render.util.RenderHelperKt.scaleToSize(java.util.List<? extends android.graphics.Path>, float, float)]
 *
 * [Path.translateToOrigin]
 * [Path.scaleToSize]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/16
 */
open class PathElement : BaseElement() {

    /**需要绘制的路径原始数据*/
    var pathList: List<Path>? = null

    init {
        paint.style = Paint.Style.STROKE
    }

    override fun createStateStack(): IStateStack = PathStateStack()

    override fun requestElementDrawable(
        renderer: BaseRenderer?,
        renderParams: RenderParams?
    ): Drawable? {
        pathList ?: return null
        val params = renderParams ?: RenderParams()
        val minWidth = max((renderParams ?: RenderParams()).drawMinWidth, paint.strokeWidth)
        val minHeight = max((renderParams ?: RenderParams()).drawMinHeight, paint.strokeWidth)
        params.drawMinWidth = minWidth
        params.drawMinHeight = minHeight
        return super.requestElementDrawable(renderer, params)
    }

    override fun getDrawPathList(): List<Path>? = pathList

    override fun onRenderInside(renderer: BaseRenderer?, canvas: Canvas, params: RenderParams) {
        paint.strokeWidth = 1f
        params.updateDrawPathPaintStrokeWidth(paint)
        renderPath(canvas, paint, false, getDrawPathList())
    }

    /**更新原始的[pathList]对象, 并保持可视化的宽高一致
     * [updateOriginWidthHeight]*/
    fun updateOriginPathList(pathList: List<Path>?, keepVisibleSize: Boolean = true) {
        this.pathList = pathList
        val bounds = RenderHelper.computePathBounds(pathList)
        updateOriginWidthHeight(bounds.width(), bounds.height(), keepVisibleSize)
    }
}