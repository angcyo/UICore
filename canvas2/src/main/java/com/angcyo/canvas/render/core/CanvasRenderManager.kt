package com.angcyo.canvas.render.core

import android.graphics.Canvas
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.renderer.CanvasMonitorRenderer
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.resetAll
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
    override fun renderOnView(canvas: Canvas, params: RenderParams) {
        //---
        renderOnView(canvas, beforeRendererList, params)
        renderOnView(canvas, elementRendererList, params)
        renderOnView(canvas, afterRendererList, params)
        //---
        monitorRenderer.renderOnView(canvas, params)
    }

    override fun renderOnInside(canvas: Canvas, params: RenderParams) {
        //---
        renderOnInside(canvas, beforeRendererList, params)
        renderOnInside(canvas, elementRendererList, params)
        renderOnInside(canvas, afterRendererList, params)
        //---
        monitorRenderer.renderOnInside(canvas, params)
    }

    override fun renderOnOutside(canvas: Canvas, params: RenderParams) {
        //---
        renderOnOutside(canvas, beforeRendererList, params)
        renderOnOutside(canvas, elementRendererList, params)
        renderOnOutside(canvas, afterRendererList, params)
        //---
        monitorRenderer.renderOnOutside(canvas, params)
    }

    //region---操作---

    /**添加一个渲染器*/
    fun addRenderer(render: BaseRenderer, selector: Boolean, strategy: Strategy) {
        addRenderer(listOf(render), selector, strategy)
    }

    /**添加一个集合渲染器
     * [selector] 是否要选中最新的元素*/
    fun addRenderer(list: List<BaseRenderer>, selector: Boolean, strategy: Strategy) {
        val from = elementRendererList.toList()
        elementRendererList.addAll(list)
        val to = elementRendererList.toList()

        delegate.undoManager.addAndRedo(strategy, true, {
            elementRendererList.resetAll(from)
            delegate.dispatchRendererListChange(to, from, list)
            delegate.refresh()
        }) {
            elementRendererList.resetAll(to)
            delegate.dispatchRendererListChange(from, to, list)
            delegate.refresh()
        }

        if (selector) {
            delegate.selectorManager.resetSelectorRenderer(list, Reason.init)
        }
    }

    /**删除一个渲染器*/
    fun removeRenderer(render: BaseRenderer, strategy: Strategy) {
        removeRenderer(listOf(render), strategy)
    }

    /**添加一个集合渲染器*/
    fun removeRenderer(list: List<BaseRenderer>, strategy: Strategy) {
        val from = elementRendererList.toList()
        elementRendererList.removeAll(list)
        val to = elementRendererList.toList()

        delegate.undoManager.addAndRedo(strategy, true, {
            elementRendererList.resetAll(from)
            delegate.dispatchRendererListChange(to, from, list)
            delegate.refresh()
        }) {
            elementRendererList.resetAll(to)
            delegate.dispatchRendererListChange(from, to, list)
            delegate.refresh()
        }
    }

    /**通过[uuid]查询对应的渲染器*/
    fun findRenderer(uuid: String): BaseRenderer? = elementRendererList.find { it.uuid == uuid }

    //endregion---操作---

}