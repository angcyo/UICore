package com.angcyo.canvas.render.core

import android.graphics.Canvas
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.element.IElement
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.renderer.CanvasMonitorRenderer
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.isChange
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
    fun addElementRenderer(
        render: BaseRenderer,
        selector: Boolean,
        reason: Reason,
        strategy: Strategy
    ) {
        addElementRenderer(listOf(render), selector, reason, strategy)
    }

    /**添加一个集合渲染器
     * [selector] 是否要选中最新的元素*/
    fun addElementRenderer(
        list: List<BaseRenderer>,
        selector: Boolean,
        reason: Reason,
        strategy: Strategy
    ) {
        val from = elementRendererList.toList()
        elementRendererList.addAll(list)
        val to = elementRendererList.toList()

        delegate.undoManager.addAndRedo(strategy, true, {
            elementRendererList.resetAll(from)
            delegate.dispatchElementRendererListChange(to, from, list)
            delegate.refresh()
        }) {
            elementRendererList.resetAll(to)
            delegate.dispatchElementRendererListChange(from, to, list)
            delegate.refresh()
        }

        if (selector) {
            delegate.selectorManager.resetSelectorRenderer(list, reason)
        }
    }

    /**移除所有渲染元素*/
    fun removeAllElementRenderer(strategy: Strategy) {
        removeElementRenderer(elementRendererList.toList(), strategy)
    }

    /**删除一个渲染器*/
    fun removeElementRenderer(render: BaseRenderer, strategy: Strategy) {
        removeElementRenderer(listOf(render), strategy)
    }

    /**添加一个集合渲染器*/
    fun removeElementRenderer(list: List<BaseRenderer>, strategy: Strategy) {
        val from = elementRendererList.toList()
        elementRendererList.removeAll(list)
        val to = elementRendererList.toList()

        delegate.undoManager.addAndRedo(strategy, true, {
            elementRendererList.resetAll(from)
            delegate.dispatchElementRendererListChange(to, from, list)
            delegate.refresh()
        }) {
            elementRendererList.resetAll(to)
            delegate.dispatchElementRendererListChange(from, to, list)
            delegate.refresh()
        }
    }

    /**重置所有元素渲染器*/
    fun resetElementRenderer(list: List<BaseRenderer>, strategy: Strategy) {
        val from = elementRendererList.toList()
        elementRendererList.resetAll(list)
        val to = elementRendererList.toList()

        delegate.undoManager.addAndRedo(strategy, true, {
            elementRendererList.resetAll(from)
            delegate.dispatchElementRendererListChange(to, from, list)
            delegate.refresh()
        }) {
            elementRendererList.resetAll(to)
            delegate.dispatchElementRendererListChange(from, to, list)
            delegate.refresh()
        }
    }

    /**通过[uuid]查询对应的渲染器*/
    fun findElementRenderer(uuid: String): BaseRenderer? =
        elementRendererList.find { it.uuid == uuid }

    /**
     * 获取所有的元素渲染器
     * [dissolveGroup] 是否要拆组
     * [com.angcyo.canvas.render.core.CanvasSelectorManager.getSelectorRendererList]
     * */
    fun getAllElementRendererList(dissolveGroup: Boolean): List<BaseRenderer> {
        return if (dissolveGroup) {
            val result = mutableListOf<BaseRenderer>()
            for (renderer in elementRendererList) {
                result.addAll(renderer.getRendererList())
            }
            result
        } else {
            elementRendererList
        }
    }

    /**获取所有渲染器对应的元素列表 */
    fun getAllElementList(): List<IElement> {
        val result = mutableListOf<IElement>()
        val rendererList = getAllElementRendererList(true)
        for (renderer in rendererList) {
            result.addAll(renderer.getElementList())
        }
        return result
    }

    /**排序[elementRendererList] [rendererList] 最后的顺序结果*/
    fun arrangeSort(rendererList: List<BaseRenderer>, strategy: Strategy) {
        val newList = rendererList.toList()
        val oldList = elementRendererList.toList()
        if (oldList.isChange(newList)) {
            //数据改变过
            delegate.undoManager.addAndRedo(strategy, true, {
                elementRendererList.resetAll(oldList)
                delegate.dispatchElementRendererListChange(newList, oldList, rendererList)
                delegate.refresh()
            }) {
                elementRendererList.resetAll(newList)
                delegate.dispatchElementRendererListChange(oldList, newList, rendererList)
                delegate.refresh()
            }
        }
    }

    /**更新渲染器的可见性*/
    fun updateRendererVisible(
        rendererList: List<BaseRenderer>,
        Visible: Boolean,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        for (renderer in rendererList) {
            renderer.updateVisible(Visible, reason, delegate)
        }
    }

    /**更新渲染器的锁定性*/
    fun updateRendererLock(
        rendererList: List<BaseRenderer>,
        lock: Boolean,
        reason: Reason,
        delegate: CanvasRenderDelegate?
    ) {
        for (renderer in rendererList) {
            renderer.updateLock(lock, reason, delegate)
        }
    }

    //endregion---操作---

}