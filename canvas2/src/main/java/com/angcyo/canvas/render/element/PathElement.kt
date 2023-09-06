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
 * [com.angcyo.library.ex.translateToOrigin]
 * [com.angcyo.library.ex.scaleToSize]
 *
 * [Path.translateToOrigin]
 * [Path.scaleToSize]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/16
 */
open class PathElement : BaseElement() {

    companion object {

        /**圆角矩形pathData*/
        fun roundRectPathData(
            x: Float,
            y: Float,
            w: Float,
            h: Float,
            rx: Float = 0f,
            ry: Float = rx,
        ): String {
            return if (rx > 0 && ry > 0) {
                val r = x + w
                val b = y + h
                buildString {
                    append("M${x + rx},${y}")
                    append("h${w - rx * 2}")
                    append("Q${r},${y} ${r},${y + ry}")
                    append("v${h - ry * 2}")
                    append("Q${r},${b} ${r - rx},${b}")
                    append("h-${w - rx * 2}")
                    append("Q${x},${b} ${x},${b - ry}")
                    append("v-${h - ry * 2}")
                    append("Q${x},${y} ${x + rx},${y}")
                    append("z")
                }
            } else {
                buildString {
                    append("M${x},${y}")
                    append("h${w}")
                    append("v${h}")
                    append("h-${w}")
                }
            }
        }

        /**椭圆/圆pathData
         * ellipse oval
         * [cx] [cy] 圆心坐标
         * [rx] [ry] 椭圆的半径
         * */
        fun ovalPathData(cx: Float, cy: Float, rx: Float, ry: Float = rx): String = buildString {
            val width = rx * 2
            val height = ry * 2
            val kappa = 0.5522848 // 4 * ((√(2) - 1) / 3)
            val ox = (width / 2.0) * kappa // control point offset horizontal
            val oy = (height / 2.0) * kappa // control point offset vertical
            //val xe = cx + width / 2.0 // x-end
            //val ye = cy + height / 2.0 // y-end

            append("M${cx - width / 2},${cy}")
            append("C${cx - width / 2},${cy - oy} ${cx - ox},${cy - height / 2} ${cx},${cy - height / 2}")
            append("C${cx + ox},${cy - height / 2} ${cx + width / 2},${cy - oy} ${cx + width / 2},${cy}")
            append("C${cx + width / 2},${cy + oy} ${cx + ox},${cy + height / 2} ${cx},${cy + height / 2}")
            append("C${cx - ox},${cy + height / 2} ${cx - width / 2},${cy + oy} ${cx - width / 2},${cy}")
            //append("Z")
        }
    }

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
        if (params.renderDst is Float) {
            val minWidth = max((renderParams ?: RenderParams()).drawMinWidth, paint.strokeWidth)
            val minHeight = max((renderParams ?: RenderParams()).drawMinHeight, paint.strokeWidth)
            params.drawMinWidth = minWidth
            params.drawMinHeight = minHeight
        }
        return super.requestElementDrawable(renderer, params)
    }

    override fun getDrawPathList(): List<Path>? = pathList

    override fun onRenderInside(renderer: BaseRenderer?, canvas: Canvas, params: RenderParams) {
        paint.strokeWidth = 1f
        params.updateDrawPathPaintStrokeWidth(paint)
        renderPath(canvas, paint, false, getDrawPathList(), params._renderMatrix)
    }

    /**更新原始的[pathList]对象, 并保持可视化的宽高一致
     * [updateRenderWidthHeight]*/
    fun updateOriginPathList(pathList: List<Path>?, keepVisibleSize: Boolean = true) {
        this.pathList = pathList
        val bounds = RenderHelper.computePathBounds(pathList)
        updateRenderWidthHeight(bounds.width(), bounds.height(), keepVisibleSize)
    }
}