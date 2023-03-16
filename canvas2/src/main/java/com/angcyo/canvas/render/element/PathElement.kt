package com.angcyo.canvas.render.element

import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.state.IStateStack
import com.angcyo.canvas.render.state.PathStateStack
import com.angcyo.canvas.render.util.RenderHelper

/**
 * 路径元素
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/16
 */
open class PathElement : BaseElement() {

    /**需要绘制的路径原始数据*/
    var pathList: List<Path>? = null

    init {
        paint.style = Paint.Style.STROKE
    }

    override fun createStateStack(renderer: BaseRenderer): IStateStack = PathStateStack(renderer)

    override fun requestElementRenderDrawable(renderParams: RenderParams?): Drawable? {
        val pathList = pathList ?: return null
        return createPathDrawable(pathList, paint, renderParams?.overrideSize)
    }

    /**更新原始的[pathList]对象, 并保持可视化的宽高一致
     * [updateOriginWidthHeight]*/
    fun updateOriginPathList(pathList: List<Path>?, keepVisibleSize: Boolean = true) {
        this.pathList = pathList
        val bounds = RenderHelper.computePathBounds(pathList)
        updateOriginWidthHeight(bounds.width(), bounds.height(), keepVisibleSize)
    }

}