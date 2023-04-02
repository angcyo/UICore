package com.angcyo.canvas.render.core

import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.angcyo.canvas.render.core.component.CanvasSelectorComponent
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.element.IElement
import com.angcyo.canvas.render.renderer.*
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

    companion object {
        /**前进, 图层上移*/
        const val ARRANGE_FORWARD: Int = 1

        /**后退, 图层下移*/
        const val ARRANGE_BACKWARD: Int = 2

        /**置顶*/
        const val ARRANGE_FRONT: Int = 3

        /**置底*/
        const val ARRANGE_BACK: Int = 4
    }

    /**在[elementRendererList]之前绘制的渲染器集合*/
    val beforeRendererList = CopyOnWriteArrayList<BaseRenderer>()

    /**所有的元素渲染器集合*/
    val elementRendererList = CopyOnWriteArrayList<BaseRenderer>()

    /**在[elementRendererList]之后绘制的渲染器集合*/
    val afterRendererList = CopyOnWriteArrayList<BaseRenderer>()

    /**限制提示线的渲染器*/
    var limitRenderer = CanvasLimitRenderer(delegate)

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
        limitRenderer.renderOnView(canvas, params)
        monitorRenderer.renderOnView(canvas, params)
    }

    override fun renderOnInside(canvas: Canvas, params: RenderParams) {
        //---
        renderOnInside(canvas, beforeRendererList, params)
        renderOnInside(canvas, elementRendererList, params)
        renderOnInside(canvas, afterRendererList, params)
        //---
        limitRenderer.renderOnInside(canvas, params)
        monitorRenderer.renderOnInside(canvas, params)
    }

    override fun renderOnOutside(canvas: Canvas, params: RenderParams) {
        //---
        renderOnOutside(canvas, beforeRendererList, params)
        renderOnOutside(canvas, elementRendererList, params)
        renderOnOutside(canvas, afterRendererList, params)
        //---
        limitRenderer.renderOnOutside(canvas, params)
        monitorRenderer.renderOnOutside(canvas, params)
    }

    //region---操作---

    /**添加一个在[elementRendererList]之前绘制的渲染器
     * [bounds] 渲染的位置
     * [drawable] 需要渲染的数据*/
    fun addBeforeRendererList(bounds: RectF, drawable: Drawable?) {
        beforeRendererList.add(SimpleInsideRenderer(bounds, drawable))
        delegate.refresh()
    }

    /**添加一个渲染器*/
    fun addElementRenderer(
        renderer: BaseRenderer,
        selector: Boolean,
        reason: Reason,
        strategy: Strategy
    ) {
        addElementRenderer(listOf(renderer), selector, reason, strategy)
    }

    /**替换一个渲染器
     * [renderer] 需要被替换的渲染器, 会保持位置不变
     * [newRendererList] 新的渲染器集合*/
    fun replaceElementRenderer(
        renderer: BaseRenderer,
        newRendererList: List<BaseRenderer>,
        selector: Boolean,
        reason: Reason,
        strategy: Strategy
    ) {

        val originRendererList = mutableListOf<BaseRenderer>()
        if (renderer is CanvasSelectorComponent) {
            originRendererList.addAll(renderer.rendererList)
        } else {
            originRendererList.add(renderer)
        }

        val index = elementRendererList.indexOf(originRendererList.firstOrNull())
        if (index == -1) {
            //未找到旧的, 则直接添加新的
            addElementRenderer(newRendererList, selector, reason, strategy)
        } else {
            val from = elementRendererList.toList()
            elementRendererList.removeAll(originRendererList)
            elementRendererList.addAll(index, newRendererList)//在指定位置添加所有
            val to = elementRendererList.toList()

            changeElementRenderer(from, to, newRendererList, strategy)

            if (selector) {
                delegate.selectorManager.resetSelectorRenderer(newRendererList, reason)
            }
        }
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

        changeElementRenderer(from, to, list, strategy)

        if (selector) {
            delegate.selectorManager.resetSelectorRenderer(list, reason)
        }
    }

    /**改变集合渲染器从[from]到[to]
     * [list] 操作的源头数据结构*/
    fun changeElementRenderer(
        from: List<BaseRenderer>,
        to: List<BaseRenderer>,
        list: List<BaseRenderer>,
        strategy: Strategy
    ) {
        delegate.undoManager.addAndRedo(strategy, true, {
            elementRendererList.resetAll(from)
            delegate.dispatchElementRendererListChange(to, from, list)
            checkClearSelectorRenderer()
            delegate.refresh()
        }) {
            elementRendererList.resetAll(to)
            delegate.dispatchElementRendererListChange(from, to, list)
            checkClearSelectorRenderer()
            delegate.refresh()
        }
    }

    /**检查是否需要清除选中元素*/
    fun checkClearSelectorRenderer() {
        if (!delegate.selectorManager.isSelectorElement) {
            return
        }
        //当前选中的元素列表
        val selectorRendererList = delegate.selectorManager.getSelectorRendererList()
        if (elementRendererList.containsAll(selectorRendererList)) {
            //已有的渲染元素, 包含所有的选中元素, 则不进行取消
        } else {
            delegate.selectorManager.selectorComponent.clearSelectorRenderer(Reason.code)
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

    /**查找[subRenderer] 所对应的[CanvasGroupRenderer]*/
    fun findElementGroupRenderer(
        subRenderer: BaseRenderer,
        rendererList: List<BaseRenderer> = elementRendererList
    ): CanvasGroupRenderer? {
        for (renderer in rendererList) {
            if (renderer is CanvasGroupRenderer) {
                if (renderer == subRenderer) {
                    return renderer
                }
                return if (renderer.rendererList.contains(subRenderer)) {
                    renderer
                } else {
                    findElementGroupRenderer(subRenderer, renderer.rendererList)
                }
            }
        }
        return null
    }

    /**
     * 获取所有的元素渲染器
     * [dissolveGroup] 是否要拆组
     * [includeGroup] 是否要包含[CanvasGroupRenderer]自身, 只在[dissolveGroup]=true的情况下有效
     * [com.angcyo.canvas.render.core.CanvasSelectorManager.getSelectorRendererList]
     * */
    fun getAllElementRendererList(
        dissolveGroup: Boolean,
        includeGroup: Boolean
    ): List<BaseRenderer> {
        return if (dissolveGroup) {
            val result = mutableListOf<BaseRenderer>()
            for (renderer in elementRendererList) {
                result.addAll(renderer.getSingleRendererList(includeGroup))
            }
            result
        } else {
            elementRendererList
        }
    }

    /**获取所有渲染器对应的元素列表 */
    fun getAllSingleElementList(): List<IElement> {
        val result = mutableListOf<IElement>()
        val rendererList = getAllElementRendererList(true, false)
        for (renderer in rendererList) {
            result.addAll(renderer.getSingleElementList())
        }
        return result
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

    //region---排序操作---

    /**排序[elementRendererList] [rendererList] 最后的顺序结果*/
    fun arrangeElementSortWith(rendererList: List<BaseRenderer>, strategy: Strategy) {
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

    /**排序, 将[rendererList], 放到指定的位置[to] */
    fun arrangeElementSort(rendererList: List<BaseRenderer>, to: Int, strategy: Strategy) {
        val first = rendererList.firstOrNull() ?: return
        val last = rendererList.lastOrNull() ?: return

        val firstIndex = elementRendererList.indexOf(first)
        val lastIndex = elementRendererList.indexOf(last)

        if (firstIndex == -1 || lastIndex == -1) {
            return
        }

        elementRendererList.getOrNull(to) ?: return
        val newList = mutableListOf<BaseRenderer>()
        newList.addAll(elementRendererList)

        newList.removeAll(rendererList)
        if (to <= firstIndex) {
            //往前移动
            newList.addAll(to, rendererList)
        } else {
            //往后移
            newList.addAll(to - (rendererList.size - 1), rendererList)
        }

        arrangeElementSortWith(newList, strategy)
    }

    /**检查[renderer]是否可以执行指定的排序操作
     * [arrangeElement]*/
    fun elementCanArrange(renderer: BaseRenderer, type: Int): Boolean {
        val list = mutableListOf<BaseRenderer>()

        if (renderer is CanvasSelectorComponent) {
            list.addAll(renderer.rendererList)
        } else {
            list.add(renderer)
        }
        val first = list.firstOrNull() ?: return false
        val firstIndex = elementRendererList.indexOf(first)
        if (firstIndex == -1) {
            return false
        }

        val last = list.lastOrNull() ?: return false
        val lastIndex = elementRendererList.indexOf(last)
        if (lastIndex == -1) {
            return false
        }

        return when (type) {
            //后退, 图层下移
            ARRANGE_BACKWARD, ARRANGE_BACK -> firstIndex != 0
            //前进, 图层上移
            else -> lastIndex != elementRendererList.lastIndex
        }
    }

    /**安排排序
     * [elementCanArrange]
     * [arrangeElementSortWith]*/
    fun arrangeElement(renderer: BaseRenderer, type: Int, strategy: Strategy) {
        val list = mutableListOf<BaseRenderer>()

        if (renderer is CanvasSelectorComponent) {
            list.addAll(renderer.rendererList)
        } else {
            list.add(renderer)
        }
        val first = list.firstOrNull() ?: return
        val firstIndex = elementRendererList.indexOf(first)
        if (firstIndex == -1) {
            return
        }

        val last = list.lastOrNull() ?: return
        val lastIndex = elementRendererList.indexOf(last)
        if (lastIndex == -1) {
            return
        }

        val toIndex = when (type) {
            //前进, 图层上移
            ARRANGE_BACKWARD -> firstIndex - 1
            ARRANGE_FORWARD -> lastIndex + 1
            ARRANGE_BACK -> 0
            else -> elementRendererList.lastIndex
        }

        arrangeElementSort(list, toIndex, strategy)
    }

    //endregion---排序操作---


}