package com.angcyo.canvas.render.renderer

import android.graphics.Canvas
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.IRenderer
import com.angcyo.canvas.render.data.LimitInfo
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.util.createRenderPaint

/**
 * 提示线的绘制
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/25
 */
class CanvasLimitRenderer(val delegate: CanvasRenderDelegate) : IRenderer {

    private val paint = createRenderPaint()

    /**需要绘制的数据列表*/
    val limitList = mutableListOf<LimitInfo>()

    override var renderFlags: Int = 0xf

    override fun renderOnInside(canvas: Canvas, params: RenderParams) {
        limitList.forEach {
            if (it.enableRender) {
                drawLimit(canvas, it)
            }
        }
    }

    /**绘制*/
    private fun drawLimit(canvas: Canvas, info: LimitInfo) {
        val path = info.path
        if (!path.isEmpty) {
            val renderViewBox = delegate.renderViewBox
            val scale = renderViewBox.getScaleX()
            if (info.strokeColor != null && info.strokeWidth > 0) {
                paint.style = android.graphics.Paint.Style.STROKE
                paint.color = info.strokeColor
                paint.strokeWidth = info.strokeWidth / scale //抵消坐标系的缩放
                canvas.drawPath(path, paint)
            }
            if (info.isFill && info.fillColor != null) {
                paint.style = android.graphics.Paint.Style.FILL
                paint.color = info.fillColor
                canvas.drawPath(path, paint)
            }
        }
    }

    /**find [LimitInfo]*/
    fun findLimitInfo(predicate: (LimitInfo) -> Boolean): LimitInfo? = limitList.find(predicate)

    /**重置所有渲染数据*/
    fun resetLimit(block: MutableList<LimitInfo>.() -> Unit) {
        clear()
        limitList.block()
        delegate.refresh()
    }

    /**添加一个渲染数据*/
    fun addLimit(block: MutableList<LimitInfo>.() -> Unit) {
        limitList.block()
        delegate.refresh()
    }

    /**清除限制框*/
    fun clear() {
        limitList.clear()
        delegate.refresh()
    }
}