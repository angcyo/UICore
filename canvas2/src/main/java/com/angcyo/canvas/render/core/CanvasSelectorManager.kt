package com.angcyo.canvas.render.core

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import com.angcyo.canvas.render.core.component.BaseControlPoint
import com.angcyo.canvas.render.core.component.BaseTouchComponent
import com.angcyo.canvas.render.core.component.CanvasMoveSelectorComponent
import com.angcyo.canvas.render.core.component.CanvasSelectorComponent
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.data.TouchSelectorInfo
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.renderer.CanvasGroupRenderer
import com.angcyo.library.L
import com.angcyo.library.component.MainExecutor
import com.angcyo.library.ex.have
import com.angcyo.library.ex.isIntersect
import com.angcyo.library.ex.nowTime
import com.angcyo.library.ex.size
import com.angcyo.library.gesture.DoubleGestureDetector2

/**
 * 元素选择管理
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/17
 */
class CanvasSelectorManager(val delegate: CanvasRenderDelegate) : BaseTouchComponent(), IRenderer {

    /**选中元素边框绘制*/
    var selectorComponent = CanvasSelectorComponent(delegate)

    /**滑动多选*/
    var moveSelectorComponent = CanvasMoveSelectorComponent(this)

    /**是否激活多指多选元素, 需要在组件已经激活的情况下生效*/
    var enableTouchMultiSelect = true

    /**是否激活滑动多选, 需要在组件已经激活的情况下生效*/
    var enableMoveMultiSelect = true

    /**标识是否在选中的渲染器上按下*/
    var isTouchInSelectorRenderer = false

    /**双击元素检测*/
    val doubleGestureDetector = DoubleGestureDetector2(delegate.view.context) {
        val first = findRendererList(_tempPoint, true).firstOrNull()
        if (first != null && first == selectorComponent.rendererList.firstOrNull()) {
            L.d("双击渲染器:${first}")
            delegate.touchManager.scaleComponent.ignoreHandle = true //忽略双击放大画板
            delegate.dispatchDoubleTapItem(this, first)
        }
    }

    /**是否有选中的元素*/
    val isSelectorElement: Boolean
        get() = selectorComponent.isSelectorElement

    override var renderFlags: Int = 0xf

    private val _tempPoint = PointF()

    init {
        delegate.touchManager.touchListenerList.add(this)
        delegate.addCanvasRenderListener(object : BaseCanvasRenderListener() {
            override fun onElementRendererListChange(
                from: List<BaseRenderer>,
                to: List<BaseRenderer>,
                op: List<BaseRenderer>
            ) {
                if (op.isIntersect(selectorComponent.rendererList)) {
                    val list = selectorComponent.rendererList.toMutableList()
                    list.removeAll(op)
                    selectorComponent.resetSelectorRenderer(list, Reason.code)
                }
            }

            override fun onRendererFlagsChange(
                renderer: BaseRenderer,
                oldFlags: Int,
                newFlags: Int,
                reason: Reason
            ) {
                if (reason.reason == Reason.REASON_USER) {
                    if (newFlags.have(BaseRenderer.RENDERER_FLAG_REQUEST_PROPERTY)) {
                        if (selectorComponent.rendererList.contains(renderer)) {
                            if (!BaseControlPoint.isKeepGroupPropertyType(reason.controlType)) {
                                selectorComponent.updateGroupRenderProperty(reason, delegate)
                            }
                        }
                    } else if (reason.renderFlag.have(BaseRenderer.RENDERER_FLAG_UNLOCK) ||
                        reason.renderFlag.have(BaseRenderer.RENDERER_FLAG_VISIBLE)
                    ) {
                        if (selectorComponent.rendererList.contains(renderer)) {
                            selectorComponent.updateGroupRenderProperty(reason, delegate)
                        }
                    }
                }
            }
        })
    }

    //region---内部---

    override fun dispatchTouchEvent(event: MotionEvent) {
        super.dispatchTouchEvent(event)

        //双击检测
        doubleGestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isTouchInSelectorRenderer = false
                selectorComponent.showSizeRender(Reason.code, null)
                val selectorInfo = touchSelectorInfo
                touchSelectorInfo = null

                val size = selectorInfo?.touchRendererList.size()
                if (size > 1) {
                    delegate.dispatchSelectorRendererList(this, selectorInfo!!)
                    L.d("选中多个元素[${size}]")
                }
            }
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return super.onInterceptTouchEvent(event)
    }

    override fun handleTouchEvent(event: MotionEvent) {
        if (delegate.controlManager.touchControlPoint != null) {
            //在控制点上按下
            return
        }

        //是否是多指多选意图
        var isTouchMultiSelectIntent = false
        if (delegate.touchManager.haveInterceptTarget) {
            //事件被拦截
            if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                if (enableTouchMultiSelect && isTouchInSelectorRenderer) {
                    isTouchMultiSelectIntent = true
                }
            }
            if (!isTouchMultiSelectIntent) {
                return
            }
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                removeDelayCancelSelectRenderer()
                startTouchDownSelectElement(event)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                //多指按下
                removeDelayCancelSelectRenderer()
                if (enableTouchMultiSelect && isTouchInSelectorRenderer) {
                    startTouchDownSelectElement(event)
                }
            }
        }
        if (enableMoveMultiSelect) {
            if (touchSelectorInfo?.touchRendererList.isNullOrEmpty()) {
                //在空白区域滑动, 则进行滑动多选
                if (moveSelectorComponent.isEnable) {
                    moveSelectorComponent.dispatchTouchEvent(event)
                }
            }
        }
    }

    override fun renderOnOutside(canvas: Canvas, params: RenderParams) {
        if (isSelectorElement && selectorComponent.isEnable) {
            selectorComponent.renderOnOutside(canvas, params)
        }
        if (moveSelectorComponent.isEnable) {
            moveSelectorComponent.renderOnOutside(canvas, params)
        }
    }

    /**按下时, 选中的信息*/
    private var touchSelectorInfo: TouchSelectorInfo? = null

    /**记录按下的时间*/
    private var _touchDownTime = 0L

    /**开始按下选择元素, 当按下有多个元素被选中, 则在UP的时候进行通知回调*/
    private fun startTouchDownSelectElement(event: MotionEvent) {
        val eventX = event.getX(event.actionIndex)
        val eventY = event.getY(event.actionIndex)
        _tempPoint.set(eventX, eventY)
        delegate.renderViewBox.transformToInside(_tempPoint)

        if (isSelectorElement && selectorComponent.rendererContainsPoint(_tempPoint)) {
            //重复选中
            onSelfTouchDownSelectRenderer()
            return
        }

        val list = findRendererList(_tempPoint, true)

        L.d("TouchSelector:x:${_tempPoint.x} y:${_tempPoint.y} ev:${eventX} ey:${eventY} ${list.size()} ${_downPointList.size()}")

        if (_downPointList.size() > 1) {
            //多指touch
            if (list.isNotEmpty()) {
                selectorComponent.addSelectorRenderer(list.first(), Reason.preview)
                delegate.refresh()
            }
        } else {
            //单指touch
            if (list.isEmpty()) {
                //需要延迟取消选中
                isTouchInSelectorRenderer = false
                touchSelectorInfo = null
                val time = nowTime()
                if (time - _touchDownTime > _delayCancelTime) {
                    delayCancelSelectRenderer()
                }
                _touchDownTime = time
            } else {
                touchSelectorInfo = TouchSelectorInfo(PointF(eventX, eventY), list.toList())
                val first = list.first()
                if (selectorComponent.rendererList.contains(first)) {
                    //重复选中
                } else {
                    selectorComponent.resetSelectorRenderer(first, Reason.preview)
                }
                onSelfTouchDownSelectRenderer()
                delegate.refresh()
            }
        }
    }

    private var delayCancelRendererRunnable: Runnable? = null
    private var _delayCancelTime = 160L

    fun delayCancelSelectRenderer() {
        delayCancelRendererRunnable = Runnable {
            selectorComponent.resetSelectorRenderer(null, Reason.preview)
            delegate.refresh()
        }
        MainExecutor.delay(delayCancelRendererRunnable!!, _delayCancelTime)
    }

    /**移除取消选择的Runnable*/
    fun removeDelayCancelSelectRenderer() {
        delayCancelRendererRunnable?.let { MainExecutor.remove(it) }
        delayCancelRendererRunnable = null
    }

    /**在选中的元素上按下, 或者在一个新的元素上按下.
     * 此时, 应该需要进行移动元素操作*/
    private fun onSelfTouchDownSelectRenderer() {
        moveSelectorComponent.ignoreHandle = true
        isTouchInSelectorRenderer = true
        selectorComponent.showLocationRender(Reason.preview, null)

        //激活移动元素组件
        delegate.controlManager.translateControl.startControl(selectorComponent)
    }

    //endregion---内部---

    //region---操作---

    /**通过相对于画板原点的点[point], 查找画板内部符合条件的渲染器
     * [reverse] 是否要反序元素, true:最上层的元素优先, false:最下层的元素优先*/
    private fun findRendererList(point: PointF, reverse: Boolean): List<BaseRenderer> {
        val result = mutableListOf<BaseRenderer>()
        val elementRendererList = delegate.renderManager.elementRendererList
        val list = if (reverse) elementRendererList.reversed() else elementRendererList
        for (element in list) {
            if (element.isLock || !element.isVisible) {
                continue
            }
            if (element.rendererContainsPoint(point)) {
                result.add(element)
            }
        }
        return result
    }

    /**[findRendererList]
     * [rect] 相对于画板原点的坐标*/
    fun findRendererList(rect: RectF, reverse: Boolean): List<BaseRenderer> {
        val result = mutableListOf<BaseRenderer>()
        val elementRendererList = delegate.renderManager.elementRendererList
        val list = if (reverse) elementRendererList.reversed() else elementRendererList
        for (element in list) {
            if (element.isLock || !element.isVisible) {
                continue
            }
            if (element.rendererIntersectRect(rect)) {
                result.add(element)
            }
        }
        return result
    }

    /**[com.angcyo.canvas.render.core.component.CanvasSelectorComponent.resetSelectorRenderer]*/
    fun resetSelectorRenderer(list: List<BaseRenderer>, reason: Reason) {
        selectorComponent.resetSelectorRenderer(list, reason)
    }

    /**[com.angcyo.canvas.render.core.component.CanvasSelectorComponent.addSelectorRenderer]*/
    fun addSelectorRenderer(renderer: BaseRenderer, reason: Reason) {
        selectorComponent.addSelectorRenderer(renderer, reason)
    }

    /**[com.angcyo.canvas.render.core.component.CanvasSelectorComponent.removeSelectorRenderer]*/
    fun removeSelectorRenderer(renderer: BaseRenderer, reason: Reason) {
        selectorComponent.removeSelectorRenderer(renderer, reason)
    }

    /**获取选中的元素列表*/
    fun getSelectorRendererList(): List<BaseRenderer> = selectorComponent.rendererList

    /**获取选中的目标操作元素
     * 如果选择了多个元素, 则返回[selectorComponent]
     * 否则返回单个元素渲染器[com.angcyo.canvas.render.renderer.CanvasElementRenderer]或者群组渲染器[CanvasGroupRenderer]
     * */
    fun getTargetSelectorRenderer(): BaseRenderer? {
        if (selectorComponent.isSelectorElement) {
            return if (selectorComponent.rendererList.size() == 1) {
                selectorComponent.rendererList.lastOrNull()
            } else {
                selectorComponent
            }
        }
        return null
    }

    /**获取选中的元素列表,
     * [dissolveGroup] 是否要解组
     * [com.angcyo.canvas.core.renderer.GroupRenderer]
     * [com.angcyo.canvas.render.core.CanvasRenderManager.getAllElementRendererList]
     * */
    fun getSelectorRendererList(dissolveGroup: Boolean): List<BaseRenderer> {
        return if (dissolveGroup) {
            val result = mutableListOf<BaseRenderer>()
            for (renderer in selectorComponent.rendererList) {
                result.addAll(renderer.getRendererList())
            }
            result
        } else {
            selectorComponent.rendererList
        }
    }

    /**锁定缩放比
     * [com.angcyo.canvas.render.core.component.CanvasSelectorComponent.updateLockScaleRatio]*/
    fun updateLockScaleRatio(lock: Boolean, reason: Reason, delegate: CanvasRenderDelegate?) {
        this.delegate.controlManager.updateLockScaleRatio(lock, reason, delegate)
    }

    //endregion---操作---

}