package com.angcyo.canvas.render.core.component

import android.graphics.RectF
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.data.SmartAssistantReferenceValue
import com.angcyo.canvas.render.renderer.BaseRenderer

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/27
 */
class SmartAssistantValueProvider(val delegate: CanvasRenderDelegate) :
    ISmartAssistantValueProvider {

    val translateXRefList = mutableListOf<SmartAssistantReferenceValue>()
    val translateYRefList = mutableListOf<SmartAssistantReferenceValue>()

    val rotateRefList = mutableListOf<SmartAssistantReferenceValue>()

    /**每隔15°推荐一次角度*/
    var rotateSmartAngle: Int = 15

    override fun getTranslateXRefValue(): List<SmartAssistantReferenceValue> {
        translateXRefList.clear()

        val axisManager = delegate.axisManager
        if (axisManager.enableRenderGrid) {
            axisManager.xAxisList.forEach { axisPoint ->
                if (axisPoint.isMasterRule) {
                    translateXRefList.add(
                        SmartAssistantReferenceValue(axisPoint.value, axisManager)
                    )
                }
            }
        }

        eachRenderer { renderer ->
            renderer.renderProperty?.getRenderBounds()?.let {
                addBoundsXValue(translateXRefList, it, renderer)
            }
        }

        val limitRenderer = delegate.renderManager.limitRenderer
        limitRenderer.limitList.forEach {
            addBoundsXValue(translateXRefList, it.bounds, limitRenderer)
        }

        return translateXRefList
    }

    override fun getTranslateYRefValue(): List<SmartAssistantReferenceValue> {
        translateYRefList.clear()

        val axisManager = delegate.axisManager
        if (axisManager.enableRenderGrid) {
            axisManager.yAxisList.forEach { axisPoint ->
                if (axisPoint.isMasterRule) {
                    translateYRefList.add(
                        SmartAssistantReferenceValue(axisPoint.value, axisManager)
                    )
                }
            }
        }

        eachRenderer { renderer ->
            renderer.renderProperty?.getRenderBounds()?.let {
                addBoundsYValue(translateYRefList, it, renderer)
            }
        }

        val limitRenderer = delegate.renderManager.limitRenderer
        limitRenderer.limitList.forEach {
            addBoundsYValue(translateYRefList, it.bounds, limitRenderer)
        }

        return translateYRefList
    }

    override fun getRotateRefValue(): List<SmartAssistantReferenceValue> {
        rotateRefList.clear()

        for (i in 0 until 360 step rotateSmartAngle) {
            rotateRefList.add(SmartAssistantReferenceValue(i.toFloat(), null))
        }
        eachRenderer { renderer ->
            renderer.renderProperty?.apply {
                if (angle != 0f) {
                    rotateRefList.add(
                        SmartAssistantReferenceValue(angle, renderer)
                    )
                }
            }
        }

        return rotateRefList
    }

    //region ---辅助方法---

    private fun eachRenderer(action: (BaseRenderer) -> Unit) {
        val selectorList = delegate.selectorManager.getSelectorRendererList(true, true)
        //delegate.renderManager.getAllElementRendererList(true, false).forEach { renderer ->
        delegate.renderManager.elementRendererList.forEach { renderer ->
            if (renderer.isVisible /*自身可见*/ &&
                renderer.isVisibleInRender(delegate, true, false) /*坐标系中可见*/ &&
                !selectorList.contains(renderer) /*不是选中的元素*/
            ) {
                action(renderer)
            }
        }
    }

    private fun addBoundsXValue(
        list: MutableList<SmartAssistantReferenceValue>,
        bounds: RectF?,
        obj: Any?
    ) {
        if (bounds != null) {
            list.add(
                SmartAssistantReferenceValue(bounds.left, obj)
            )
            list.add(
                SmartAssistantReferenceValue(bounds.centerX(), obj)
            )
            list.add(
                SmartAssistantReferenceValue(bounds.right, obj)
            )
        }
    }

    private fun addBoundsYValue(
        list: MutableList<SmartAssistantReferenceValue>,
        bounds: RectF?,
        obj: Any?
    ) {
        if (bounds == null) {
            return
        }
        list.add(SmartAssistantReferenceValue(bounds.top, obj))
        list.add(SmartAssistantReferenceValue(bounds.centerY(), obj))
        list.add(SmartAssistantReferenceValue(bounds.bottom, obj))
    }

    //endregion ---辅助方法---

}