package com.angcyo.canvas.render.core

import android.graphics.Canvas
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.renderer.CanvasMonitorRenderer
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.have
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 渲染元素的管理类, 管理所有界面上绘制相关的操作
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
class CanvasRenderManager(val delegate: CanvasRenderDelegate) : IRenderer {

    /**在[elementRendererList]之前绘制的渲染器集合*/
    val beforeRendererList = CopyOnWriteArrayList<BaseRenderer>()

    /**所有的元素渲染器集合*/
    val elementRendererList = CopyOnWriteArrayList<BaseRenderer>()

    /**在[elementRendererList]之后绘制的渲染器集合*/
    val afterRendererList = CopyOnWriteArrayList<BaseRenderer>()

    /**监测信息绘制*/
    var monitorRenderer = CanvasMonitorRenderer(delegate)

    /**渲染入口点*/
    @CallPoint
    override fun render(canvas: Canvas) {
        val renderViewBox = delegate.renderViewBox
        val renderBounds = renderViewBox.renderBounds
        val originPoint = renderViewBox.getOriginPoint()
        canvas.withTranslation(renderBounds.left, renderBounds.top) {
            clipRect(0f, 0f, renderBounds.width(), renderBounds.height())
            translate(originPoint.x, originPoint.y)
            //---
            for (renderer in beforeRendererList) {
                drawRenderer(canvas, renderer)
            }
            for (renderer in elementRendererList) {
                drawRenderer(canvas, renderer)
            }
            for (renderer in afterRendererList) {
                drawRenderer(canvas, renderer)
            }
        }
        //---
        monitorRenderer.render(canvas)
    }

    //绘制元素
    private fun drawRenderer(canvas: Canvas, renderer: BaseRenderer) {
        if (renderer.isVisible) {
            canvas.withSave {
                if (renderer.renderFlags.have(BaseRenderer.RENDERER_FLAG_BOX_MATRIX)) {
                    concat(delegate.renderViewBox.renderMatrix)
                }
                renderer.render(canvas)
            }
        }
    }

}