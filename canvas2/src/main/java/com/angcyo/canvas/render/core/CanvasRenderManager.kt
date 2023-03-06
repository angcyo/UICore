package com.angcyo.canvas.render.core

import android.graphics.Canvas
import com.angcyo.canvas.render.data.RendererParams
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.renderer.CanvasMonitorRenderer
import com.angcyo.library.annotation.CallPoint
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 渲染元素的管理类, 管理所有界面上绘制相关的操作
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/11
 */
class CanvasRenderManager(val delegate: CanvasRenderDelegate) : BaseRenderDispatch(), IRenderer {

    /**在[elementRendererList]之前绘制的渲染器集合*/
    val beforeRendererList = CopyOnWriteArrayList<BaseRenderer>()

    /**所有的元素渲染器集合*/
    val elementRendererList = CopyOnWriteArrayList<BaseRenderer>()

    /**在[elementRendererList]之后绘制的渲染器集合*/
    val afterRendererList = CopyOnWriteArrayList<BaseRenderer>()

    /**监测信息绘制*/
    var monitorRenderer = CanvasMonitorRenderer(delegate)

    override var renderFlags: Int = 0xff

    init {
        /*renderFlags = renderFlags.remove(IRenderer.RENDERER_FLAG_ON_INSIDE)
            .remove(IRenderer.RENDERER_FLAG_ON_OUTSIDE)*/
    }

    /**渲染入口点*/
    @CallPoint
    override fun renderOnView(canvas: Canvas, params: RendererParams) {
        //---
        renderOnView(canvas, beforeRendererList, params)
        renderOnView(canvas, elementRendererList, params)
        renderOnView(canvas, afterRendererList, params)
        //---
        monitorRenderer.renderOnView(canvas, params)
    }

    override fun renderOnInside(canvas: Canvas, params: RendererParams) {
        //---
        renderOnInside(canvas, beforeRendererList, params)
        renderOnInside(canvas, elementRendererList, params)
        renderOnInside(canvas, afterRendererList, params)
        //---
        monitorRenderer.renderOnInside(canvas, params)
    }

    override fun renderOnOutside(canvas: Canvas, params: RendererParams) {
        //---
        renderOnOutside(canvas, beforeRendererList, params)
        renderOnOutside(canvas, elementRendererList, params)
        renderOnOutside(canvas, afterRendererList, params)
        //---
        monitorRenderer.renderOnOutside(canvas, params)
    }

}