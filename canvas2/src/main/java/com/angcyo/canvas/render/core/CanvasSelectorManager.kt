package com.angcyo.canvas.render.core

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.render.core.component.BaseTouchComponent
import com.angcyo.canvas.render.core.component.CanvasMoveSelectorComponent
import com.angcyo.canvas.render.core.component.CanvasSelectorComponent
import com.angcyo.canvas.render.data.TouchSelectorInfo
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.library.L
import com.angcyo.library.component.MainExecutor
import com.angcyo.library.ex.nowTime
import com.angcyo.library.ex.size

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

    /**是否有选中的元素*/
    val isSelectorElement: Boolean
        get() = selectorComponent.isSelectorElement

    private val _tempPoint = PointF()

    init {
        delegate.touchManager.touchListenerList.add(this)
    }

    //region---内部---

    override fun dispatchTouchEvent(event: MotionEvent) {
        super.dispatchTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isTouchInSelectorRenderer = false
                selectorComponent.showSizeRender(Reason.code, null)
                val selectorInfo = touchSelectorInfo
                touchSelectorInfo = null

                if (selectorInfo != null && selectorInfo.touchRendererList.size() > 1) {
                    L.i("选中多个元素...")
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
        if (delegate.touchManager.haveInterceptTarget) {
            //事件被拦截
            return
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

    override fun render(canvas: Canvas) {
        val renderViewBox = delegate.renderViewBox
        val renderBounds = renderViewBox.renderBounds
        canvas.withTranslation(renderBounds.left, renderBounds.top) {
            clipRect(0f, 0f, renderBounds.width(), renderBounds.height())
            if (isSelectorElement && selectorComponent.isEnable) {
                selectorComponent.render(this)
            }
            if (moveSelectorComponent.isEnable) {
                moveSelectorComponent.render(this)
            }
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

        if (selectorComponent.rendererContainsPoint(_tempPoint)) {
            //重复选中
            _onTouchDownSelectRenderer()
            return
        }

        val list = findRendererList(_tempPoint, true)

        L.i("TouchSelector:x:${_tempPoint.x} y:${_tempPoint.y} ev:${eventX} ey:${eventY} ${list.size()} ${_downPointList.size()}")

        if (_downPointList.size() > 1) {
            //多指touch
            if (list.isNotEmpty()) {
                selectorComponent.addSelectorRenderer(list.first())
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
                    selectorComponent.resetSelectorRenderer(first)
                }
                _onTouchDownSelectRenderer()
                delegate.refresh()
            }
        }
    }

    private var delayCancelRendererRunnable: Runnable? = null
    private var _delayCancelTime = 160L

    fun delayCancelSelectRenderer() {
        delayCancelRendererRunnable = Runnable {
            selectorComponent.resetSelectorRenderer(null)
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
    fun _onTouchDownSelectRenderer() {
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

    //endregion---操作---

}